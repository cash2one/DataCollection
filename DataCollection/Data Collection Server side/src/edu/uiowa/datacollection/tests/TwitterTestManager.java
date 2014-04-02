package edu.uiowa.datacollection.tests;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import edu.uiowa.datacollection.twitter.Conversation;
import edu.uiowa.datacollection.twitter.DataManager;
import edu.uiowa.datacollection.twitter.Message;
import edu.uiowa.datacollection.twitter.User;

@SuppressWarnings("resource")
public class TwitterTestManager
{
	private Twitter tSession;

	public static final String TWITTER_CONSUMER_ID = "BaWtyknv1RwsU60jVccA";
	public static final String TWITTER_CONSUMER_SECRET = "EDopj7ySkVstUTD294ODgUlmhctGi3PBSkW2OljhhPY";
	
	public static final String TEST_USER_1_TOKEN = "2372806093-4fojaLeleu5rSTkG4RroR3Q3Oc4XpGCnG7TcEvr";
	public static final String TEST_USER_1_SECRET = "vsq98jacFislGChkRLDlxqDHWwjsN0MckyxMWowCZS8vz";
	public static final long TEST_USER_1_ID = 2372806093l;
	public static final String TEST_USER_1_SCREEN_NAME = "AbbyDoeTest1";
	public static final twitter4j.auth.AccessToken TEST_USER_1 = 
			new twitter4j.auth.AccessToken(TEST_USER_1_TOKEN, TEST_USER_1_SECRET, TEST_USER_1_ID);
	
	
	public static final String TEST_USER_2_TOKEN = "2372805192-Tv2hkVFU2Z7XDOKiOeXCwJLaFOznmAWX9u7SDb7";
	public static final String TEST_USER_2_SECRET = "HJoFEcFHe2yDvmYMJvZL6jwfBQiXlqjjKz40aUv59y9FO";
	public static final long TEST_USER_2_ID = 2372805192l;
	public static final String TEST_USER_2_SCREEN_NAME = "BobDoeTest2";
	public static final twitter4j.auth.AccessToken TEST_USER_2 = 
			new twitter4j.auth.AccessToken(TEST_USER_2_TOKEN, TEST_USER_2_SECRET, TEST_USER_2_ID);
	
	public static final String TEST_USER_3_TOKEN = "2372821087-9LA7Pbtvn7EbKCXIw3SrPvZyCJJUThRfYJJxV0i";
	public static final String TEST_USER_3_SECRET = "iocZrOEXwVRNCR1nk33LJY5iHBGlDT5hod0ghhhQd6Vij";
	public static final long TEST_USER_3_ID = 2372821087l;
	public static final String TEST_USER_3_SCREEN_NAME = "CathyDoeTest3";
	public static final twitter4j.auth.AccessToken TEST_USER_3 = 
			new twitter4j.auth.AccessToken(TEST_USER_3_TOKEN, TEST_USER_3_SECRET, TEST_USER_3_ID);
	
//	AbbyDoeTestUser1@outlook.com - 1qaz1qaz - AbbyDoeTest1
//	BobDoeTestUser2@outlook.com - 1qaz1qaz - BobDoeTest2
//	CathyDoeTestUser3@outlook.com - 1qaz1qaz - CathyDoeTest3

	
	public TwitterTestManager()
	{
		this.tSession = openTwitterSession();		
	}
	
	public TestResult twitterRetweetTest() throws TwitterException
	{
		TestResult result = new TestResult("Twitter Retweet Test", System.out);
		result.begin();
		
		clearTwitter();
		
		Scanner scan = new Scanner(System.in);
		
		
		//Have Abby post a status
		String status1Text = "This is a popular status, I hope you retweet it.";
		tSession.setOAuthAccessToken(TEST_USER_1);
		Status status = tSession.updateStatus(status1Text);
		
		
		tSession.setOAuthAccessToken(TEST_USER_2);
		tSession.retweetStatus(status.getId());

		tSession.setOAuthAccessToken(TEST_USER_3);
		tSession.retweetStatus(status.getId());
		
		
		System.out.println("To see the messages sent, login as Abby");
		System.out.println("Username: AbbyDoeTestUser1");
		System.out.println("Password: 1qaz1qaz");
		openLink("http://twitter.com");

		
		User abby = new User("", "" + TEST_USER_1_ID, "");
		abby.setOauthToken(TEST_USER_1_TOKEN);
		abby.setTokenSecret(TEST_USER_1_SECRET);
		abby.setDirectMessageSinceID(System.currentTimeMillis() - 1000 * 60 * 60);
		abby.setSentDirectMessageSinceID(System.currentTimeMillis() - 1000 * 60 * 60);
		DataManager dm = new DataManager(abby);
		
		List<Message> retweets = dm.collectRetweetsOfMe();
		
		for (Message m : retweets)
			System.out.println(m.getText());
		
		System.out.print("Enter 'done' to finish. ");
		scan.nextLine();
		
		clearTwitter();
		
		return result;
	}
	
	/**
	 * This is a test to determine if the Twitter DataManager can collect
	 * tweets that mention our targeted user.
	 * @return The result of the tests
	 * @throws TwitterException
	 */
	public TestResult twitterMentionsTest() throws TwitterException
	{
		TestResult result = new TestResult("Twitter Mentions Test", System.out);
		result.begin();
		
		clearTwitter();
		
		
		//Have Bob and Cathy mention Abby
		tSession.setOAuthAccessToken(TEST_USER_2);
		String status1Text = "@" + TEST_USER_1_SCREEN_NAME + " I am bullying you.";
		Status status1 = tSession.updateStatus(status1Text);
		
		tSession.setOAuthAccessToken(TEST_USER_3);
		String status2Text = "@" + TEST_USER_1_SCREEN_NAME + " I am bullying you.";
		Status status2 = tSession.updateStatus(status2Text);
		
		
		
		
		System.out.println("To see the messages sent, login as Abby");
		System.out.println("Username: AbbyDoeTestUser1");
		System.out.println("Password: 1qaz1qaz");
		openLink("http://twitter.com");

		User abby = new User("", "" + TEST_USER_1_ID, "");
		abby.setOauthToken(TEST_USER_1_TOKEN);
		abby.setTokenSecret(TEST_USER_1_SECRET);
		abby.setMentionTimeLineSinceID(System.currentTimeMillis() - 1000 * 60 * 60);
		DataManager dm = new DataManager(abby);
		
		//1000 = 1 sec, *60 = 1 min, *60 = 1 hour, all in last hour
		long hourAgo = System.currentTimeMillis() - 1000 * 60 * 60;
		List<Message> mentionMessages = dm.collectMentionsTimeLine(hourAgo);
		
		//Check that we collected bob's mention, it will either be first or second
		//depending on server speed
		result.addResult("Bob's mention collected", 
				(mentionMessages.get(0).getText().equals(status1.getText()) &&
				 mentionMessages.get(0).getSender().getTweetId().equals("" + TEST_USER_2_ID)) ||
				(mentionMessages.get(1).getText().equals(status1.getText()) &&
				 mentionMessages.get(1).getSender().getTweetId().equals("" + TEST_USER_2_ID)), true);
		//Check that we collected cathy's mention, it will either be first or second
		//depending on server speed
		result.addResult("Cathy's mention collected", 
				(mentionMessages.get(0).getText().equals(status2.getText()) &&
				 mentionMessages.get(0).getSender().getTweetId().equals("" + TEST_USER_3_ID)) ||
				(mentionMessages.get(1).getText().equals(status2.getText()) &&
				 mentionMessages.get(1).getSender().getTweetId().equals("" + TEST_USER_3_ID)), true);
		
		
		
		clearTwitter();
		
		return result;
	}
	
	/**
	 * This is a test to determine if the Twitter DataManager can collect
	 * statuses of our targeted user as well as comment tweets
	 * @return The result of the test
	 * @throws TwitterException
	 */
	public TestResult twitterStatusTest() throws TwitterException
	{
		TestResult result = new TestResult("Twitter Status Test", System.out);
		result.begin();
		
		clearTwitter();
		
		String statusText = "This is Abby posting a status.";
		tSession.setOAuthAccessToken(TEST_USER_1);
		Status status = tSession.updateStatus(statusText);

		String reply1Text = "@" + TEST_USER_1_SCREEN_NAME + " Reply to status";
		tSession.setOAuthAccessToken(TEST_USER_2);
		Status reply1 = tSession.updateStatus(new StatusUpdate(reply1Text).inReplyToStatusId(status.getId()));
		
		String reply2Text = "@" + TEST_USER_1_SCREEN_NAME + " Reply to status";
		tSession.setOAuthAccessToken(TEST_USER_3);
		Status reply2 = tSession.updateStatus(new StatusUpdate(reply2Text).inReplyToStatusId(status.getId()));

		System.out.println("To see the messages sent, login as Abby");
		System.out.println("Username: AbbyDoeTestUser1");
		System.out.println("Password: 1qaz1qaz");
		openLink("http://twitter.com");

		
		User abby = new User("", "" + TEST_USER_1_ID, "");
		abby.setOauthToken(TEST_USER_1_TOKEN);
		abby.setTokenSecret(TEST_USER_1_SECRET);
		abby.setUserTimeLineSinceID(System.currentTimeMillis() - 1000 * 60 * 60);
		abby.setMentionTimeLineSinceID(System.currentTimeMillis() - 1000 * 60 * 60);
		DataManager dm = new DataManager(abby);
		
		//1000 = 1 sec, *60 = 1 min, *60 = 1 hour, all in last hour
		long hourAgo = System.currentTimeMillis() - 1000 * 60 * 60;
		List<Message> userMessages = dm.collectUserTimeLine(hourAgo);
		List<Message> mentionMessages = dm.collectMentionsTimeLine(hourAgo);
		
		result.addResult("Abby status collected", 
				userMessages.get(0).getText().equals(statusText) &&
				userMessages.get(0).getSender().getTweetId().equals("" + TEST_USER_1_ID), true);
		
		//Check that we collected bob's reply, it will either be first or second
		//depending on server speed
		result.addResult("Bob's reply collected", 
				(mentionMessages.get(0).getText().equals(reply1.getText()) &&
				 mentionMessages.get(0).getSender().getTweetId().equals("" + TEST_USER_2_ID)) ||
				(mentionMessages.get(1).getText().equals(reply1.getText()) &&
				 mentionMessages.get(1).getSender().getTweetId().equals("" + TEST_USER_2_ID)), true);
		//Check that we collected cathy's reply, it will either be first or second
		//depending on server speed
		result.addResult("Cathy's reply collected", 
				(mentionMessages.get(0).getText().equals(reply2.getText()) &&
				 mentionMessages.get(0).getSender().getTweetId().equals("" + TEST_USER_3_ID)) ||
				(mentionMessages.get(1).getText().equals(reply2.getText()) &&
				 mentionMessages.get(1).getSender().getTweetId().equals("" + TEST_USER_3_ID)), true);
		
		
		
		clearTwitter();
		
		return result;
	}
	
	/**
	 * This clears all data from our twitter test users
	 * @throws TwitterException
	 */
	private void clearTwitter() throws TwitterException
	{
		tSession.setOAuthAccessToken(TEST_USER_1);
		List<DirectMessage> messages1 = tSession.getDirectMessages();
		messages1.addAll(tSession.getSentDirectMessages());
		for (DirectMessage dm : messages1)
			tSession.destroyDirectMessage(dm.getId());

		tSession.setOAuthAccessToken(TEST_USER_2);
		List<DirectMessage> messages2 = tSession.getDirectMessages();
		messages2.addAll(tSession.getSentDirectMessages());
		for (DirectMessage dm : messages2)
			tSession.destroyDirectMessage(dm.getId());

		tSession.setOAuthAccessToken(TEST_USER_2);
		List<DirectMessage> message3 = tSession.getDirectMessages();
		message3.addAll(tSession.getSentDirectMessages());
		for (DirectMessage dm : message3)
			tSession.destroyDirectMessage(dm.getId());
		
		
		
		tSession.setOAuthAccessToken(TEST_USER_1);
		List<Status> status1 = tSession.getUserTimeline();
		for (Status status : status1)
		{
			tSession.destroyStatus(status.getId());
		}
		
		tSession.setOAuthAccessToken(TEST_USER_2);
		List<Status> status2 = tSession.getUserTimeline();
		for (Status status : status2)
		{
			tSession.destroyStatus(status.getId());
		}
		
		tSession.setOAuthAccessToken(TEST_USER_3);
		List<Status> status3 = tSession.getUserTimeline();
		for (Status status : status3)
		{
			tSession.destroyStatus(status.getId());
		}
	}
	
	/**
	 * This tests that direct messages between two users are collected by
	 * using Abby and Bob and having them send messages back and forth.
	 * 
	 * @return
	 * @throws TwitterException
	 */
	public TestResult twitterDirectMessageTest() throws TwitterException
	{
		TestResult result = new TestResult("Twitter Direct Message Test", System.out);
		result.begin();
		
		clearTwitter();
		Scanner scan = new Scanner(System.in);
		
		tSession.setOAuthAccessToken(TEST_USER_1);
		
		sendDirectMessage(TEST_USER_1, TEST_USER_2_ID, "message1");
		sendDirectMessage(TEST_USER_2, TEST_USER_1_ID, "message2");
		sendDirectMessage(TEST_USER_1, TEST_USER_2_ID, "message3");
		sendDirectMessage(TEST_USER_2, TEST_USER_1_ID, "message4");
		sendDirectMessage(TEST_USER_1, TEST_USER_2_ID, "message5");
		sendDirectMessage(TEST_USER_2, TEST_USER_1_ID, "message6");

		sendDirectMessage(TEST_USER_1, TEST_USER_3_ID, "message12");
		sendDirectMessage(TEST_USER_3, TEST_USER_1_ID, "message22");
		sendDirectMessage(TEST_USER_1, TEST_USER_3_ID, "message32");
		sendDirectMessage(TEST_USER_3, TEST_USER_1_ID, "message42");
		sendDirectMessage(TEST_USER_1, TEST_USER_3_ID, "message52");
		sendDirectMessage(TEST_USER_3, TEST_USER_1_ID, "message62");
		

		System.out.println("To see the messages sent, login as Abby");
		System.out.println("Username: AbbyDoeTestUser1");
		System.out.println("Password: 1qaz1qaz");
		openLink("http://twitter.com");
		
		User abby = new User("", "" + TEST_USER_1_ID, "");
		abby.setOauthToken(TEST_USER_1_TOKEN);
		abby.setTokenSecret(TEST_USER_1_SECRET);
		abby.setDirectMessageSinceID(System.currentTimeMillis() - 1000 * 60 * 60);
		abby.setSentDirectMessageSinceID(System.currentTimeMillis() - 1000 * 60 * 60);
		System.out.println(abby.getDirectMessageSinceID());
		DataManager dm = new DataManager(abby);
		
		List<Conversation> convos = dm.collectDirectConversations();
		for (Conversation c : convos)
		{
			for (Message m : c.getMessageList())
				System.out.println(m.getText() + " , " + m.getCreateTime());
			System.out.println();
		}
		result.addResult("Conversation with Abby and Bob retrieved",
						 containsUser(convos.get(0), TEST_USER_1_ID) &&
						 containsUser(convos.get(0), TEST_USER_2_ID),
						 true);
		result.addResult("Conversation with Abby and Cathy retrieved",
				 containsUser(convos.get(1), TEST_USER_1_ID) &&
				 containsUser(convos.get(1), TEST_USER_3_ID),
				 true);
//
//		result.addResult("Last message retrieved",
//				directMessages.get(0).getText().equals(message4) &&
//				directMessages.get(0).getSender().getTweetId().equals("" + TEST_USER_2_ID),
//				true);
////		System.out.println(directMessages.get(1).getText() + "\n" + message3 + "\n" + directMessages.get(1).getSender().getTweetId() + "\n" + TEST_USER_1_ID); 
//		result.addResult("Third message retrieved",
//				directMessages.get(1).getText().equals(message3) &&
//				directMessages.get(1).getSender().getTweetId().equals("" + TEST_USER_1_ID),
//				true);
//		result.addResult("Second message retrieved",
//				directMessages.get(2).getText().equals(message2) &&
//				directMessages.get(2).getSender().getTweetId().equals("" + TEST_USER_2_ID),
//				true);
//		result.addResult("First message retrieved",
//				directMessages.get(3).getText().equals(message1) &&
//				directMessages.get(3).getSender().getTweetId().equals("" + TEST_USER_1_ID),
//				true);
		
		System.out.print("Enter 'done' to finish. ");
		scan.nextLine();
		
		clearTwitter();
		
		return result;
	}
	
	/**
	 * This function sends a direct message from user1 using their accessToken
	 * to userID
	 * @param userFrom The user sending the message
	 * @param userIdTo The recipient of the message
	 * @param message The message to send
	 * @throws TwitterException
	 */
	private void sendDirectMessage(AccessToken userFrom,
			long userIdTo, String message) throws TwitterException
	{
		tSession.setOAuthAccessToken(userFrom);
		tSession.sendDirectMessage(userIdTo, message);
	}

	/**
	 * Opens our twitter session
	 * @return
	 */
	public Twitter openTwitterSession()
	{
		Twitter result = TwitterFactory.getSingleton();
		result.setOAuthConsumer(TWITTER_CONSUMER_ID, TWITTER_CONSUMER_SECRET);
		
		return result;
	}
	
	private boolean containsUser(Conversation convo, long userID)
	{
		for (User user : convo.getParticipantList())
			if (user.getTweetId().equals(String.valueOf(userID)))
				return true;
		return false;
	}
	

	/**
	 * This function opens the given url in the user's default browser. If it
	 * cannot, it prints the link to open.
	 * 
	 * @param link
	 *            The link to open.
	 */
	private void openLink(String link)
	{
		try
		{
			System.out.println("Opening link... " + link);
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(link));
		}
		catch (IOException e)
		{
			System.out.println("Opening browser failed. Please open link: "
					+ link);
		}
	}
}
