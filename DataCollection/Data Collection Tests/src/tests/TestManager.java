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
import facebook4j.PostUpdate;
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

	public TestResult wallPostTest() throws FacebookException, JSONException
	{
		TestResult result = new TestResult("Wall Post Test", System.out);
		
		// Create test users and data managers
		System.out.println("Creating test users");
		TestUser user1 = createTestUser("Abby Doe");
		TestUser user2 = createTestUser("Bob Doe");

		System.out.println("Creating corresponding data managers");
		DataManager data1 = new DataManager(user1.getAccessToken());
		DataManager data2 = new DataManager(user2.getAccessToken());

		// make them friends
		System.out.println("Creating friendships");
		session.makeFriendTestUser(user1, user2);

		// Have user 1 poast on user 2s wall, have user 2 respond
		session.setOAuthAccessToken(new AccessToken(user1.getAccessToken()));
		session.postFeed(user2.getId(), new PostUpdate(
				"User 1 is posting on User 2s timeline"));
		data1.collectData(false, false, true, false);
		System.out.println(data1.getStreamObjects());

		// Collect data

		System.out.println(data1.getStreamObjects().get(0));
		System.out.println(data2.getStreamObjects().get(0));

		return result;
	}

	@SuppressWarnings("unused")
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
		data2.collectData(false, false, true, false);
		System.out.println(data2.getStreamObjects().get(0)
				.getJSONRepresentation().getString("description"));
		result.addResult(
				"User 2 comment on User 1 post is picked up by User 2", data2
						.getStreamObjects().get(0).getJSONRepresentation()
						.getString("description").contains(testComment2), true);

		return result;
	}

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
