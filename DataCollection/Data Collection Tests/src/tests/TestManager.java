package tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import datacollection.Conversation;
import datacollection.DataManager;
import datacollection.Message;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.TestUser;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONException;

public class TestManager
{
	private Facebook session;
	private HashMap<String, TestUser> testUsers = new HashMap<String, TestUser>();

	public TestManager(Facebook session)
	{
		this.session = session;
		session.setOAuthAccessToken(new AccessToken(
				"442864129167674|m5Ss-_eSF53XoKVdkyT_nkjEhj8"));

	}

	
	/**
	 * This method prompts the user to make two wall posts and then put a
	 * comment on each one, checking that wall post conversations and comment
	 * conversations are collected.
	 * @return
	 * @throws FacebookException
	 * @throws JSONException
	 */
	public TestResult wallPostTest() throws FacebookException, JSONException
	{
		TestResult result = new TestResult("Wall Post Test", System.out);
		
		// Create test users and data managers
		System.out.println("Creating test users");
		Scanner scan = new Scanner(System.in);
		TestUser user1 = createTestUser("Abby Doe");
		TestUser user2 = createTestUser("Bob Doe");

		System.out.println("Creating corresponding data managers");
		DataManager data1 = new DataManager(user1.getAccessToken());
		DataManager data2 = new DataManager(user2.getAccessToken());

		// make them friends
		System.out.println("Creating friendships");
		session.makeFriendTestUser(user1, user2);
		
		openLink(user1.getLoginUrl());
		String abbyToBob = "Hi Bob, this is Abby.";
		System.out.println("Please post the following message on Bob Doe's wall");
		System.out.println(abbyToBob);
		System.out.print("Enter done when finished. ");
		scan.nextLine();
		
		openLink(user2.getLoginUrl());
		String bobToAbby = "Hi Abby, this is Bob.";
		String comment1 = "Hi Abby, I'm commenting on your post.";
		System.out.println("Please post the following message on Abby Doe's wall");
		System.out.println(bobToAbby);
		System.out.println("Please post the following comment on the wall post from Abby");
		System.out.println(comment1);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		openLink(user1.getLoginUrl());
		String comment2 = "Hi Bob, I'm commenting on your post.";
		System.out.println("Please post the following comment on the wall post from Bob");
		System.out.println(comment2);
		System.out.print("Enter done when finished. ");
		scan.nextLine();
		
		
		
		data1.collectData(false, false, true, false);
		data2.collectData(false, false, true, false);
		
		
		
		scan.close();
		return result;
	}

	/**
	 * This method has one user post a Facebook status and then has three
	 * friends post comments on that status, checking status collection and
	 * comment conversation collection. 
	 * @return
	 * @throws FacebookException
	 * @throws JSONException
	 */
	public TestResult statusTest() throws FacebookException, JSONException
	{
		TestResult result = new TestResult("Status Test", System.out);

		System.out.println("Creating test users");
		// Create test users and data managers
		TestUser user1 = createTestUser("Abby Doe");
		TestUser user2 = createTestUser("Bob Doe");
		TestUser user3 = createTestUser("Cathy Doe");
		TestUser user4 = createTestUser("Doug Doe");

		System.out.println("Creating corresponding data managers");
		DataManager data1 = new DataManager(user1.getAccessToken());
		DataManager data2 = new DataManager(user2.getAccessToken());
		DataManager data3 = new DataManager(user3.getAccessToken());
		DataManager data4 = new DataManager(user4.getAccessToken());

		System.out.println("Creating friendships");
		// make them friends
		session.makeFriendTestUser(user1, user2);
		session.makeFriendTestUser(user1, user3);
		session.makeFriendTestUser(user1, user4);

		session.makeFriendTestUser(user2, user3);
		session.makeFriendTestUser(user2, user4);

		session.makeFriendTestUser(user3, user4);

		// User 1 will post a status
		System.out.println("User 1 is posting a status");
		String statusID = session.postStatusMessage(user1.getId(),
				"This is a test status posted by user 1");

		// // Have the other users comment
		session.setOAuthAccessToken(new AccessToken(user2.getAccessToken()));
		String testComment2 = "This is a test comment posted by user 2";
		session.commentPost(statusID, testComment2);

		session.setOAuthAccessToken(new AccessToken(user3.getAccessToken()));
		String testComment3 = "This is a test comment posted by user 3";
		session.commentPost(statusID, testComment3);

		session.setOAuthAccessToken(new AccessToken(user4.getAccessToken()));
		String testComment4 = "This is a test comment posted by user 4";
		session.commentPost(statusID, testComment4);
		

		data1.collectData(false, false, true, false);
		data2.collectData(false, false, true, false);
		data3.collectData(false, false, true, false);
		data4.collectData(false, false, true, false);
		
		
		
		return result;
	}

	/**
	 * This method has three friends create 3 conversations A-B, B-C, A-C
	 * and then checks that all messages are collected.
	 * @return
	 * @throws FacebookException
	 */
	public TestResult imConversationTest() throws FacebookException
	{
		TestResult result = new TestResult("IM Conversation Test", System.out);
		result.begin();
		Scanner scan = new Scanner(System.in);

		/*
		 * Create 2 friends and check from each of their perspectives that the
		 * conversation is recorded properly.
		 */
		System.out.println("Creating test users");
		TestUser user1 = createTestUser("Abby Doe");
		TestUser user2 = createTestUser("Bob Doe");
		TestUser user3 = createTestUser("Cathy Doe");
		DataManager data1 = new DataManager(user1.getAccessToken());
		DataManager data2 = new DataManager(user2.getAccessToken());
		DataManager data3 = new DataManager(user3.getAccessToken());

		System.out.println("Making the users friends");
		session.makeFriendTestUser(user1, user2);
		session.makeFriendTestUser(user1, user3);
		session.makeFriendTestUser(user2, user3);

		openLink(user1.getLoginUrl());
		String abbyToBob = "Hi Bob, this is Abby.";
		String abbyToCathy = "Hi Cathy, this is Abby.";
		System.out.println("Please send the following message to your friend Bob Doe");
		System.out.println(abbyToBob);
		System.out.println("Please send the following message to your friend Cathy Doe");
		System.out.println(abbyToCathy);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		openLink(user2.getLoginUrl());
		String bobToAbby = "Hi Abby, this is Bob.";
		String bobToCathy = "Hi Cathy, this is Bob.";
		System.out.println("Please send the following message to your friend Abby Doe");
		System.out.println(bobToAbby);
		System.out.println("Please send the following message to your friend Cathy Doe");
		System.out.println(bobToCathy);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		openLink(user3.getLoginUrl());
		String cathyToAbby = "Hi Abby, this is Cathy.";
		String cathyToBob = "Hi Bob, this is Cathy.";
		System.out.println("Please send the following message to your friend Abby Doe");
		System.out.println(cathyToAbby);
		System.out.println("Please send the following message to your friend Bob Doe");
		System.out.println(cathyToBob);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data1.collectData(true, false, false, false);
		data2.collectData(true, false, false, false);
		data3.collectData(true, false, false, false);

		// Check that Abby had a conversation with bob and cathy
		ArrayList<Conversation> data1Con = data1.getConversations();
		for (Conversation c : data1Con)
		{
			if (c.hasParticipant(user1.getId())
					&& c.hasParticipant(user2.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Abby and Bob conversation",
						messages.size() == 2 && 
						messages.get(0).getMessage().equals(abbyToBob) && 
						messages.get(1).getMessage().equals(bobToAbby), 
						true);
			}
			if (c.hasParticipant(user1.getId())
					&& c.hasParticipant(user3.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Abby and Cathy conversation",
						messages.size() == 2 && 
						messages.get(0).getMessage().equals(abbyToCathy) && 
						messages.get(1).getMessage().equals(cathyToAbby), 
						true);
			}
		}
		
		
		ArrayList<Conversation> data2Con = data2.getConversations();
		for (Conversation c : data2Con)
		{
			if (c.hasParticipant(user2.getId())
					&& c.hasParticipant(user1.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Bob and Abby conversation",
						messages.size() == 2 && 
						messages.get(0).getMessage().equals(abbyToBob) && 
						messages.get(1).getMessage().equals(bobToAbby), 
						true);
			}
			if (c.hasParticipant(user2.getId())
					&& c.hasParticipant(user3.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Bob and Cathy conversation",
						messages.size() == 2 && 
						messages.get(0).getMessage().equals(bobToCathy) && 
						messages.get(1).getMessage().equals(cathyToBob), 
						true);
			}
		}
		
		ArrayList<Conversation> data3Con = data3.getConversations();
		for (Conversation c : data3Con)
		{
			if (c.hasParticipant(user3.getId())
					&& c.hasParticipant(user1.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Cathy and Abby conversation",
						messages.size() == 2 && 
						messages.get(0).getMessage().equals(abbyToCathy) && 
						messages.get(1).getMessage().equals(cathyToAbby), 
						true);
			}
			if (c.hasParticipant(user2.getId())
					&& c.hasParticipant(user3.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Cathy and Bob conversation",
						messages.size() == 2 && 
						messages.get(0).getMessage().equals(bobToCathy) && 
						messages.get(1).getMessage().equals(cathyToBob), 
						true);
			}
		}

		scan.close();
		return result;
	}
	
	/**
	 * This function tests collecting group Facebook messages by creating one
	 * and having three friends sending messages to each other. We then check
	 * to make sure that the messages are collected by each person's data
	 * manager.
	 * @return
	 * @throws FacebookException
	 */
	public TestResult imGroupConversationTest() throws FacebookException
	{
		TestResult result = new TestResult("IM Group Conversation Test", System.out);
		result.begin();
		Scanner scan = new Scanner(System.in);

		/*
		 * Create 2 friends and check from each of their perspectives that the
		 * conversation is recorded properly.
		 */
		System.out.println("Creating test users");
		TestUser user1 = createTestUser("Abby Doe");
		TestUser user2 = createTestUser("Bob Doe");
		TestUser user3 = createTestUser("Cathy Doe");
		DataManager data1 = new DataManager(user1.getAccessToken());
		DataManager data2 = new DataManager(user2.getAccessToken());
		DataManager data3 = new DataManager(user3.getAccessToken());

		System.out.println("Making the users friends");
		session.makeFriendTestUser(user1, user2);
		session.makeFriendTestUser(user1, user3);
		session.makeFriendTestUser(user2, user3);

		openLink(user1.getLoginUrl());
		String message1 = "Hi guys, this is Abby.";
		System.out.println("Please create a group message with Bob and Cathy");
		System.out.println(message1);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		openLink(user2.getLoginUrl());
		String message2 = "Hi guys, this is Bob.";
		System.out.println("Please send this message in the group conversation");
		System.out.println(message2);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		openLink(user3.getLoginUrl());
		String message3 = "Hi guys, this is Cathy.";
		System.out.println("Please send this message in the group conversation");
		System.out.println(message3);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data1.collectData(true, false, false, false);
		data2.collectData(true, false, false, false);
		data3.collectData(true, false, false, false);

		// Check that Abby had a conversation with bob and cathy
		ArrayList<Conversation> data1Con = data1.getConversations();
		Conversation groupConvo1 = data1Con.get(0);
		result.addResult("Abby's ID, convo has Bob and Cathy",
				groupConvo1.hasParticipant(user2.getId()) && 
				groupConvo1.hasParticipant(user3.getId()), true);
		
		result.addResult("Abby's ID, convo has all three messages",
				groupConvo1.getMessages().get(0).getMessage().equals(message1) &&
				groupConvo1.getMessages().get(1).getMessage().equals(message2) &&
				groupConvo1.getMessages().get(2).getMessage().equals(message3), true);
		

		ArrayList<Conversation> data2Con = data1.getConversations();
		Conversation groupConvo2 = data2Con.get(0);
		result.addResult("Bob's ID, convo has Abby and Cathy",
				groupConvo2.hasParticipant(user1.getId()) && 
				groupConvo2.hasParticipant(user3.getId()), true);
		result.addResult("Bob's ID, convo has all three messages",
				groupConvo2.getMessages().get(0).getMessage().equals(message1) &&
				groupConvo2.getMessages().get(1).getMessage().equals(message2) &&
				groupConvo2.getMessages().get(2).getMessage().equals(message3), true);

		

		ArrayList<Conversation> data3Con = data1.getConversations();
		Conversation groupConvo3 = data3Con.get(0);
		result.addResult("Cathy's ID, convo has Abby and Cathy",
				groupConvo3.hasParticipant(user1.getId()) && 
				groupConvo3.hasParticipant(user2.getId()), true);
		result.addResult("Cathy's ID, convo has all three messages",
				groupConvo3.getMessages().get(0).getMessage().equals(message1) &&
				groupConvo3.getMessages().get(1).getMessage().equals(message2) &&
				groupConvo3.getMessages().get(2).getMessage().equals(message3), true);

		scan.close();
		return result;
	}

	private void openLink(String link)
	{
		try
		{
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(link));
		}
		catch (IOException e)
		{
			System.out.println("Opening browser failed. Please open link: "
					+ link);
		}
	}

	private TestUser createTestUser(String username)
	{
		try
		{
			TestUser tu = session.createTestUser("442864129167674", // App ID
					username, // Name of test User
					"en_US", // Locale
					"read_mailbox,read_stream,publish_stream");
			testUsers.put(username, tu);
		}
		catch (FacebookException e)
		{
			e.printStackTrace();
		}
		return testUsers.get(username);
	}
}
