package com.datacollection.twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class DataManager {

	private Twitter twitter;

	private List<Conversation> conversationList;

	private List<Message> msgList;

	private List<Message> statusList;

	public DataManager() {
		twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer("8mga0W2Nfw7BQl0ixni5kA",
				"2HAU4vqSafst4GJcumXRNYJE1Ak1m469oQkfaorfkE");
		conversationList = new ArrayList<Conversation>();
		msgList = new ArrayList<Message>();
		statusList = new ArrayList<Message>();
	}

	public void getUserAuthorization() throws Exception {
		RequestToken requestToken = null;
		requestToken = twitter.getOAuthRequestToken();
		AccessToken accessToken = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (null == accessToken && requestToken != null) {
			System.out
					.println("Open the following URL and grant access to your account:");
			System.out.println(requestToken.getAuthorizationURL());
			System.out
					.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
			String pin;
			pin = br.readLine();
			try {
				if (pin.length() > 0) {
					accessToken = twitter
							.getOAuthAccessToken(requestToken, pin);
				} else {
					accessToken = twitter.getOAuthAccessToken();
				}
			} catch (TwitterException te) {
				if (401 == te.getStatusCode()) {
					System.out.println("Unable to get the access token.");
				} else {
					te.printStackTrace();
				}
			}
		}
		storeAccessToken(twitter.verifyCredentials().getId(), accessToken);
	}

	/**
	 * Collect User TimeLine, each request can return at most 200 timelines.
	 * This method can return at most 3200 recent user timelines. RateLimit: 180
	 * requests per user per 15 minutes.
	 * 
	 */
	public void collectUserTimeLine(Long sinceId) {
		Paging paging = new Paging();
		int count = 100;
		paging.setCount(count);
		paging.setSinceId(sinceId);
		ResponseList<Status> rl = null;
		boolean collectAllData = false;
		while (!collectAllData) {
			try {
				ResponseList<Status> tl = twitter.getUserTimeline(paging);
				if (tl.size() == 0) {
					collectAllData = true;
				} else {
					if (rl == null)
						rl = tl;
					else
						rl.addAll(tl);
					paging.setMaxId(tl.get(tl.size() - 1).getId() - 1);
				}
			} catch (TwitterException e) {
				// rate Limit exceed
				if (e.getStatusCode() == 429) {
					// Actually it should not wait, it should continue to handle
					// other users' requests.
					System.err
							.println("Rate Limit exceeds.Thread sleep for 3 minutes.");
					try {
						Thread.sleep(1000 * 60 * 3);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

				} else
					e.printStackTrace();
			}
		}
		if (rl != null) {
			for (int i = 0; i < rl.size(); i++) {
				Message msg = new Message(rl.get(i));
				statusList.add(msg);
			}
		}
	}

	/**
	 * Collect Mentions TimeLine, each request can return at most 200 timelines.
	 * This method can return at most 800 recent mention timelines. RateLimit:
	 * 15 requests per user per 15 minutes.
	 */
	public void collectMentionsTimeLine(long sinceId) {
		Paging paging = new Paging();
		int count = 100;
		paging.setCount(count);
		paging.setSinceId(sinceId);
		ResponseList<Status> rl = null;
		boolean collectAllData = false;
		while (!collectAllData) {
			try {
				ResponseList<Status> tl = twitter.getMentionsTimeline(paging);
				if (tl.size() == 0) {
					collectAllData = true;
				} else {
					if (rl == null)
						rl = tl;
					else
						rl.addAll(tl);
					paging.setMaxId(tl.get(tl.size() - 1).getId() - 1);
				}
			} catch (TwitterException e) {
				// rate Limit exceed
				if (e.getStatusCode() == 429) {
					try {
						Thread.sleep(1000 * 60 * 3);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} else
					e.printStackTrace();
			}
		}
		if (rl != null) {
			for (int i = 0; i < rl.size(); i++) {
				Message msg = new Message(rl.get(i));
				statusList.add(msg);
			}
		}
	}

	/**
	 * Collect direct messages, each request can return at most 200 Messages.
	 * This method can return at most 800 recent direct messages. RateLimit: 15
	 * requests per user per 15 minutes.
	 */
	public void collectInstantMessages() {
		Paging paging = new Paging();
		paging.setCount(100);
		ResponseList<DirectMessage> rl = null;
		try {
			rl = twitter.getDirectMessages(paging);
			rl.addAll(twitter.getSentDirectMessages(paging));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if (rl != null) {
			for (int i = 0; i < rl.size(); i++) {
				Message msg = new Message(rl.get(i));
				msgList.add(msg);
			}
		}
	}

	/**
	 * Collect the timeline which does not appear in User_TimeLine and
	 * Mention_TimeLine. RateLimit: 180 requests per user per 15 minutes.
	 */
	public void collectExtraTimeLine(List<Conversation> conversList) {

		for (int i = 0; i < conversList.size(); i++) {
			Conversation c = conversList.get(i);
			while (c.getMessageList().get(0).getInReplytoMessageID() != null) {
				try {
					Status s = twitter.showStatus(Long.parseLong(c
							.getMessageList().get(0).getInReplytoMessageID()));
					Message msg = new Message(s);
					c.getMessageList().add(0, msg);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TwitterException e) {
					// it is possible the original status has been deleted
					// e.printStackTrace();
					if (e.getStatusCode() == 404) {
						System.err.println("The original Status: "
								+ c.getMessageList().get(0)
										.getInReplytoMessageID()
								+ " no longer exists.");
						break;
					}
					// Rate Limit exceeds.
					else if (e.getStatusCode() == 429) {
						try {
							Thread.sleep(1000 * 60 * 3);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void collectData() {
		Long sinceId = (long) 1;
		collectUserTimeLine(sinceId);

		collectMentionsTimeLine(sinceId);

		collectInstantMessages();
	}

	public void constructConversations() {
		/*
		 * Contruct Conversations for status Message.
		 */
		Collections.sort(statusList);
		List<Conversation> statusConversationList = new ArrayList<Conversation>();
		for (int i = 0; i < statusList.size(); i++) {
			Message m = statusList.get(i);
			boolean startNewConversation = true;
			for (int j = 0; j < statusConversationList.size(); j++) {
				Conversation c = statusConversationList.get(j);
				List<String> mIDList = c.getMessageIDList();
				if (mIDList.contains(m.getInReplytoMessageID())) {
					startNewConversation = false;
					if (mIDList.get(mIDList.size() - 1).equals(
							m.getInReplytoMessageID())) {
						c.addMessage(m);
						break;
					} else {
						/*
						 * This is the case that: A tweet has already been put
						 * into a conversation. But it appears in more than 1
						 * conversation.
						 */
						Conversation newC = new Conversation(1);
						for (int w = 0; w < mIDList.size(); w++) {
							newC.addMessage(c.getMessageList().get(w));
							if (mIDList.get(w)
									.equals(m.getInReplytoMessageID())) {
								newC.addMessage(m);
								statusConversationList.add(newC);
								break;
							}
						}
						break;// break to avoid infinte loop.
					}
				}
			}
			if (startNewConversation) {
				Conversation newC = new Conversation(1);
				newC.addMessage(m);
				statusConversationList.add(newC);
			}
		}
		collectExtraTimeLine(statusConversationList);
		conversationList.addAll(statusConversationList);

		/*
		 * Contruct Conversations for instant Message.
		 */
		Collections.sort(msgList);
		List<Conversation> directConversation = new ArrayList<Conversation>();
		for (int i = 0; i < msgList.size(); i++) {
			Message m = msgList.get(i);
			boolean startNewConversation = true;
			for (int j = 0; j < directConversation.size(); j++) {
				Conversation c = directConversation.get(j);
				if (c.getParticipantList().contains(m.getSender())
						&& c.getParticipantList().contains(
								m.getRecipients().get(0))) {
					c.addMessage(m);
					startNewConversation = false;
				}
			}
			if (startNewConversation) {
				Conversation newC = new Conversation(1);
				newC.addMessage(m);
				directConversation.add(newC);
			}
		}
		conversationList.addAll(directConversation);

		for (int i = 0; i < conversationList.size(); i++) {
			conversationList.get(i).setStartEndTime();
		}
	}

	public void storeAccessToken(long id, AccessToken accessToken) {
		Writer writer = null;
		String token = accessToken.getToken();
		String tokenSecret = accessToken.getTokenSecret();
		StringBuffer sb = new StringBuffer();
		sb.append(id).append(" ").append(token).append(" ").append(tokenSecret);
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("accessToken.txt"), "utf-8"));
			writer.write(sb.toString());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadAccessToken() {
		AccessToken accessToken = getAccessToken();
		twitter.setOAuthAccessToken(accessToken);
	}

	public AccessToken getAccessToken() {
		File fileName = new File("accessToken.txt");
		Scanner inFile;
		String token = null;
		String tokenSecret = null;
		long id = 0;
		try {
			inFile = new Scanner(fileName);
			id = inFile.nextLong();
			token = inFile.next();
			tokenSecret = inFile.next();
			inFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new AccessToken(token, tokenSecret, id);
	}

	public List<Conversation> getConversationList() {
		return conversationList;
	}

	public List<Message> getMessageList() {
		return msgList;
	}

}
