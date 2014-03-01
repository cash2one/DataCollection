package com.datacollection.twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
import twitter4j.internal.org.json.JSONTokener;

import com.datacollection.util.SqlHelper;

public class DataManager {
	private List<User> userList=new ArrayList<User>();

	private Twitter twitter;

	private ArrayList<ArrayList<Conversation>> listConversationList;

	public DataManager() {
		twitter = TwitterFactory.getSingleton();
		/*twitter.setOAuthConsumer("8mga0W2Nfw7BQl0ixni5kA",
				"2HAU4vqSafst4GJcumXRNYJE1Ak1m469oQkfaorfkE");*/
		twitter.setOAuthConsumer("BaWtyknv1RwsU60jVccA", "EDopj7ySkVstUTD294ODgUlmhctGi3PBSkW2OljhhPY");
		listConversationList = new ArrayList<ArrayList<Conversation>>();
	}
	
	public List<User> getUserFromServer(){
		User a=new User("1953004938",2);
		a.setOauthToken("1953004938-t9XrnuScuq36s3U1q2uA6GC4nEY85VEYdcFbEDd");
		a.setTokenSecret("oaGfOjTttivK5JPtMlQZWc9QoOgPzfWpUNTXsTdDhA5kH");
		userList.add(a);
		
		User b=new User("2345736906",2);
		b.setOauthToken("2345736906-XI8iapyuLXwxO8gQlcU76wxl8iNncbLYX15vj1H");
		b.setTokenSecret("eCuhIAZPW9ewBy5A4w6Ln3NjEjA1Q68DSAuhZ1Y8aoXJs");
		userList.add(b);
		return userList;
	}
	
	public List<User> getUserFromServer(final String urlAddress){
		Thread t=new Thread(new Runnable(){

			@Override
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet method = new HttpGet(urlAddress);
				HttpResponse response=null;
				try {
					response = client.execute(method);
				} catch (ClientProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				BufferedReader reader=null;
				try {
					StringBuilder builder = new StringBuilder();
					reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					for (String line = null; (line = reader.readLine()) != null;) {
					    builder.append(line).append("\n");
					}
					System.out.println(builder.toString());
					JSONTokener tokener = new JSONTokener(builder.toString());
					JSONObject jsonObject = new JSONObject(tokener);
					JSONArray jsonArray = jsonObject.getJSONArray("data");
					for(int i=0;i<jsonArray.length();i++){
						JSONObject userToken=jsonArray.getJSONObject(i);
						User u=new User(userToken.getString("twitter_id"),2);
						u.setOauthToken(userToken.getString("twitter_token"));
						u.setTokenSecret(userToken.getString("twitter_secret"));
						userList.add(u);
					}
					
					
				} catch (IllegalStateException
						| IOException | JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			
			}
			
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userList;
	}
	
	public void loadAccessToken(User user){
		twitter.setOAuthAccessToken(new AccessToken(user.getOauthToken(), user
				.getTokenSecret(), Long.parseLong(user.getTweetId())));
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
	public List<Message> collectUserTimeLine(Long sinceId) {
		List<Message> statusList=new ArrayList<Message>();
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
		return statusList;
	}

	/**
	 * Collect Mentions TimeLine, each request can return at most 200 timelines.
	 * This method can return at most 800 recent mention timelines. RateLimit:
	 * 15 requests per user per 15 minutes.
	 */
	public List<Message> collectMentionsTimeLine(long sinceId) {
		Paging paging = new Paging();
		int count = 100;
		paging.setCount(count);
		paging.setSinceId(sinceId);
		ResponseList<Status> rl = null;
		boolean collectAllData = false;
		List<Message> statusList=new ArrayList<Message>();
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
		return statusList;
	}

	/**
	 * Collect direct messages, each request can return at most 200 Messages.
	 * This method can return at most 800 recent direct messages. RateLimit: 15
	 * requests per user per 15 minutes.
	 */
	public List<Message> collectInstantMessages() {
		List<Message> msgList=new ArrayList<Message>();
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
		return msgList;
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

	public ArrayList<ArrayList<Conversation>> collectData(List<User> userList) {
		Long sinceId = (long) 1;
		for(User user:userList){
			this.loadAccessToken(user);
			List<Message> usertimeline=collectUserTimeLine(sinceId);
	
			List<Message> mentiontimeline=collectMentionsTimeLine(sinceId);
	
			List<Message> msgList=collectInstantMessages();
			
			ArrayList<Conversation> conversList=constructConversations(usertimeline,mentiontimeline,msgList);
			listConversationList.add(conversList);
		}
		return listConversationList;
	}

	public ArrayList<Conversation> constructConversations(List<Message> usertimeline,List<Message> mentiontimeline, List<Message> msgList) {
		/*
		 * Contruct Conversations for status Message.
		 */
		ArrayList<Conversation> conversationList=new ArrayList<Conversation>();
		List<Message> statusList=new ArrayList<Message>();
		statusList.addAll(usertimeline);
		statusList.addAll(mentiontimeline);
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
		return conversationList;
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
	
	public void getAccessTokenFromServer(){
		String token = null;
		String tokenSecret = null;
		AccessToken accessToken=new AccessToken(token,tokenSecret);
		twitter.setOAuthAccessToken(accessToken);
	}
	public void loadHardCodedToken(int i){
		long []id=new long[10];
		String token[]=new String[10];
		String tokenSecret[]=new String[10];
		id[0]=Long.parseLong("2345736906");
		token[0]= "2345736906-XI8iapyuLXwxO8gQlcU76wxl8iNncbLYX15vj1H";
		tokenSecret[0]= "eCuhIAZPW9ewBy5A4w6Ln3NjEjA1Q68DSAuhZ1Y8aoXJs";
		id[1]=Long.parseLong("1953004938"); 
		token[1]= "1953004938-t9XrnuScuq36s3U1q2uA6GC4nEY85VEYdcFbEDd"; 
		tokenSecret[1]="oaGfOjTttivK5JPtMlQZWc9QoOgPzfWpUNTXsTdDhA5kH";
		id[2]=Long.parseLong("2345802558");
		token[2]="2345802558-lKz6s635ic3SRn8iGyyLJYL1sIlUHNtskHHPl96";
		tokenSecret[3]="GrEWpWWxgMaxF3dEPjuZpcsdkXq9u6VhjMWIGyBHTzblB";
		twitter.setOAuthAccessToken(new AccessToken(token[i], tokenSecret[i], id[i]));
	}
	
	public void loadAccessTokenFromDatabase(String screenName){
		SqlHelper sqlHelper=new SqlHelper();
		String sql="SELECT ID,TOKEN,TOKENSECRET FROM User WHERE SCREEN_NAME= \'"+screenName+"\'";
		ResultSet rs=sqlHelper.executeQuery(sql);
		Long id = null;
	    String  token = null;
	    String tokenSecret  = null;
		try {
			while ( rs.next() ) {
			     id = rs.getLong("ID");
			     token = rs.getString("TOKEN");
			     tokenSecret  = rs.getString("TOKENSECRET");
			     
			  }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		twitter.setOAuthAccessToken(new AccessToken(token, tokenSecret, id));
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

	public ArrayList<ArrayList<Conversation>> getListConversationList() {
		return listConversationList;
	}

	public String excutePost(String targetURL, List<JSONObject> conversList)
	  {
	    URL url;
	    HttpURLConnection connection = null;
	    String urlParameters=null;
	    try {
	      //Create connection
	      url = new URL(targetURL);
	      urlParameters=conversList.toString();
	      connection = (HttpURLConnection)url.openConnection();
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Content-Type", 
	           "application/x-www-form-urlencoded");
				
	      connection.setRequestProperty("Content-Length", "" + 
	               Integer.toString(urlParameters.getBytes().length));
	      connection.setRequestProperty("Content-Language", "en-US");  
				
	      connection.setUseCaches (false);
	      connection.setDoInput(true);
	      connection.setDoOutput(true);

	      //Send request
	      DataOutputStream wr = new DataOutputStream (
	                  connection.getOutputStream ());
	      wr.writeBytes (urlParameters);
	      wr.flush ();
	      wr.close ();

	      //Get Response	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      while((line = rd.readLine()) != null) {
	        response.append(line);
	        response.append('\r');
	      }
	      rd.close();
	      return response.toString();

	    } catch (Exception e) {

	      e.printStackTrace();
	      return null;

	    } finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
	    }
	  }

}
