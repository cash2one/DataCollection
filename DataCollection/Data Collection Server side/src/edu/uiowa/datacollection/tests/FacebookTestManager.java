package edu.uiowa.datacollection.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import edu.uiowa.datacollection.facebook.Comment;
import edu.uiowa.datacollection.facebook.Conversation;
import edu.uiowa.datacollection.facebook.DataManager;
import edu.uiowa.datacollection.facebook.Message;
import edu.uiowa.datacollection.facebook.StreamObject;
import edu.uiowa.datacollection.facebook.User;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.TestUser;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONException;

@SuppressWarnings("resource")
public class FacebookTestManager
{
	private Facebook fSession;
	private HashMap<String, TestUser> testUsers = new HashMap<String, TestUser>();

	public static final AccessToken APP_ACCESS_TOKEN = new AccessToken(
			"442864129167674|m5Ss-_eSF53XoKVdkyT_nkjEhj8");
	public static final String FACEBOOK_APP_ID = "442864129167674";
	public static final String FACEBOOK_APP_SECRET = "f2140fbb0148c5db21db0d07b92e6ade";

	public FacebookTestManager()
	{
		this.fSession = new FacebookFactory().getInstance();

		resetFacebookSession();
	}

	/**
	 * This method prompts the user to make two wall posts and then put a
	 * comment on each one, checking that wall post conversations and comment
	 * conversations are collected.
	 * 
	 * @return
	 * @throws FacebookException
	 * @throws JSONException
	 */
	public TestResult facebookWallPostTest() throws FacebookException,
			JSONException
	{
		TestResult result = new TestResult("Wall Post Test", System.out);
		result.begin();

		// Create test users and data managers
		System.out.println("Creating test users");
		Scanner scan = new Scanner(System.in);
		TestUser user1 = createTestUser("Abby Doe");
		TestUser user2 = createTestUser("Bob Doe");

		System.out.println("Creating corresponding data managers");
		DataManager data1 = new DataManager(user1.getAccessToken(), "");
		DataManager data2 = new DataManager(user2.getAccessToken(), "");

		// make them friends
		System.out.println("Creating friendships");
		fSession.makeFriendTestUser(user1, user2);

		openLink(user1.getLoginUrl());
		String abbyToBob = "Hi Bob, this is Abby.";
		System.out
				.println("Please post the following message on Bob Doe's wall");
		System.out.println(abbyToBob);
		System.out.print("Enter done when finished. ");
		scan.nextLine();
		System.out.println();

		data1.collectData(false, false, true);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		openLink(user2.getLoginUrl());
		String bobToAbby = "Hi Abby, this is Bob.";
		String comment1 = "Hi Abby, I'm commenting on your post.";
		System.out
				.println("Please post the following message on Abby Doe's wall");
		System.out.println(bobToAbby);
		System.out
				.println("Please post the following comment on the wall post from Abby");
		System.out.println(comment1);
		System.out.print("Enter done when finished. ");
		scan.nextLine();
		System.out.println();

		data2.collectData(false, false, true);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		result.addResult("Abby collected she wrote on someones wall", data1
				.getStreamObjects().size() > 0, true);

		StreamObject postFromAbby = null;
		for (StreamObject so : data2.getStreamObjects())
			if (so.getComments().size() > 0)
				postFromAbby = so;
		result.addResult(
				"Bob collected Abby's post to her wall and the comment",
				postFromAbby != null
						&& postFromAbby.getComments().get(0).getText()
								.equals(comment1)
						&& postFromAbby.getJSONRepresentation()
								.getString("message").equals(abbyToBob), true);

		clearTestUsers();

		return result;
	}

	/**
	 * This method has one user post a Facebook status and then has three
	 * friends post comments on that status, checking status collection and
	 * comment conversation collection.
	 * 
	 * @return
	 * @throws FacebookException
	 * @throws JSONException
	 */
	public TestResult facebookStatusTest() throws FacebookException,
			JSONException
	{
		TestResult result = new TestResult("Status Test", System.out);
		result.begin();

		System.out.println("Creating test users");
		// Create test users and data managers
		TestUser user1 = createTestUser("Abby Doe");
		TestUser user2 = createTestUser("Bob Doe");
		TestUser user3 = createTestUser("Cathy Doe");
		TestUser user4 = createTestUser("Doug Doe");

		System.out.println("Creating corresponding data managers");
		DataManager data1 = new DataManager(user1.getAccessToken(), "");
		DataManager data2 = new DataManager(user2.getAccessToken(), "");
		DataManager data3 = new DataManager(user3.getAccessToken(), "");
		DataManager data4 = new DataManager(user4.getAccessToken(), "");

		System.out.println("Creating friendships");
		// make them friends
		fSession.makeFriendTestUser(user1, user2);
		fSession.makeFriendTestUser(user1, user3);
		fSession.makeFriendTestUser(user1, user4);

		fSession.makeFriendTestUser(user2, user3);
		fSession.makeFriendTestUser(user2, user4);

		fSession.makeFriendTestUser(user3, user4);

		// User 1 will post a status
		System.out.println("User 1 is posting a status");
		String statusText = "This is a test status posted by user 1";
		String statusID = fSession.postStatusMessage(user1.getId(), statusText);

		// // Have the other users comment
		fSession.setOAuthAccessToken(new AccessToken(user2.getAccessToken()));
		String testComment2 = "This is a test comment posted by user 2";
		fSession.commentPost(statusID, testComment2);
		fSession.likePost(statusID);

		fSession.setOAuthAccessToken(new AccessToken(user3.getAccessToken()));
		String testComment3 = "This is a test comment posted by user 3";
		fSession.commentPost(statusID, testComment3);
		fSession.likePost(statusID);

		fSession.setOAuthAccessToken(new AccessToken(user4.getAccessToken()));
		String testComment4 = "This is a test comment posted by user 4";
		fSession.commentPost(statusID, testComment4);
		fSession.likePost(statusID);

		data1.collectData(false, false, true);
		data2.collectData(false, false, true);
		data3.collectData(false, false, true);
		data4.collectData(false, false, true);

		StreamObject status = data1.getStreamObjects().get(0);
		ArrayList<Comment> comments = status.getComments();

		result.addResult("Post ID is correctly obtained",
				statusID.equals(status.getPostID()), true);

		String message = status.getJSONRepresentation().getString("message");
		result.addResult("Status text correctly obtained",
				message.equals(statusText), true);

		result.addResult("First comment correct", comments.get(0).getText()
				.equals(testComment2)
				&& comments.get(0).getFromID().equals(user2.getId()), true);
		result.addResult("Second comment correct", comments.get(1).getText()
				.equals(testComment3)
				&& comments.get(1).getFromID().equals(user3.getId()), true);
		result.addResult("Third comment correct", comments.get(2).getText()
				.equals(testComment4)
				&& comments.get(2).getFromID().equals(user4.getId()), true);
		
		
		result.addResult("Collected likes on posts", 
				status.hasUserLiked(user2.getId()) &&
				status.hasUserLiked(user3.getId()) &&
				status.hasUserLiked(user4.getId()), true);

		resetFacebookSession();
		clearTestUsers();

		return result;
	}

	/**
	 * This method has three friends create 3 conversations A-B, B-C, A-C and
	 * then checks that all messages are collected.
	 * 
	 * @return
	 * @throws FacebookException
	 */
	public TestResult facebokIMConversationTest() throws FacebookException
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
		DataManager data1 = new DataManager(user1.getAccessToken(), "");
		DataManager data2 = new DataManager(user2.getAccessToken(), "");
		DataManager data3 = new DataManager(user3.getAccessToken(), "");

		System.out.println("Making the users friends");
		fSession.makeFriendTestUser(user1, user2);
		fSession.makeFriendTestUser(user1, user3);
		fSession.makeFriendTestUser(user2, user3);

		openLink(user1.getLoginUrl());
		String abbyToBob = "Hi Bob, this is Abby.";
		String abbyToCathy = "Hi Cathy, this is Abby.";
		System.out
				.println("Please send the following message to your friend Bob Doe");
		System.out.println(abbyToBob);
		System.out
				.println("Please send the following message to your friend Cathy Doe");
		System.out.println(abbyToCathy);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data1.collectData(true, false, false);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		openLink(user2.getLoginUrl());
		String bobToAbby = "Hi Abby, this is Bob.";
		String bobToCathy = "Hi Cathy, this is Bob.";
		System.out
				.println("Please send the following message to your friend Abby Doe");
		System.out.println(bobToAbby);
		System.out
				.println("Please send the following message to your friend Cathy Doe");
		System.out.println(bobToCathy);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data2.collectData(true, false, false);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		openLink(user3.getLoginUrl());
		String cathyToAbby = "Hi Abby, this is Cathy.";
		String cathyToBob = "Hi Bob, this is Cathy.";
		System.out
				.println("Please send the following message to your friend Abby Doe");
		System.out.println(cathyToAbby);
		System.out
				.println("Please send the following message to your friend Bob Doe");
		System.out.println(cathyToBob);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data3.collectData(true, false, false);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		data2.saveJSONData("conversations.json");

		// Check that Abby had a conversation with bob and cathy
		// Because of how the Facebook test users we updated we can only
		// get the data before we log out of facebook with Abby.
		ArrayList<Conversation> data1Con = data1.getConversations();
		for (Conversation c : data1Con)
		{
			if (hasParticipant(c, user1.getId())
					&& hasParticipant(c, user2.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Abby and Bob conversation",
						messages.size() == 1
								&& messages.get(0).getMessage()
										.equals(abbyToBob), true);
			}
			if (hasParticipant(c, user1.getId())
					&& hasParticipant(c, user3.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Abby and Cathy conversation",
						messages.size() == 1
								&& messages.get(0).getMessage()
										.equals(abbyToCathy), true);
			}
		}

		// We can get two way for abby and bob, but only one way for bob/cathy
		ArrayList<Conversation> data2Con = data2.getConversations();
		for (Conversation c : data2Con)
		{
			if (hasParticipant(c, user2.getId())
					&& hasParticipant(c, user1.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Bob and Abby conversation",
						messages.size() == 2
								&& messages.get(0).getMessage()
										.equals(abbyToBob)
								&& messages.get(1).getMessage()
										.equals(bobToAbby), true);
			}
			if (hasParticipant(c, user2.getId())
					&& hasParticipant(c, user3.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Bob and Cathy conversation",
						messages.size() == 1
								&& messages.get(0).getMessage()
										.equals(bobToCathy), true);
			}
		}

		// We can get two way for both
		ArrayList<Conversation> data3Con = data3.getConversations();
		for (Conversation c : data3Con)
		{
			if (hasParticipant(c, user3.getId())
					&& hasParticipant(c, user1.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Cathy and Abby conversation",
						messages.size() == 2
								&& messages.get(0).getMessage()
										.equals(abbyToCathy)
								&& messages.get(1).getMessage()
										.equals(cathyToAbby), true);
			}
			if (hasParticipant(c, user2.getId())
					&& hasParticipant(c, user3.getId()))
			{
				ArrayList<Message> messages = c.getMessages();
				result.addResult(
						"Cathy and Bob conversation",
						messages.size() == 2
								&& messages.get(0).getMessage()
										.equals(bobToCathy)
								&& messages.get(1).getMessage()
										.equals(cathyToBob), true);
			}
		}

		clearTestUsers();

		return result;
	}

	/**
	 * This function tests collecting group Facebook messages by creating one
	 * and having three friends sending messages to each other. We then check to
	 * make sure that the messages are collected by each person's data manager.
	 * 
	 * @return
	 * @throws FacebookException
	 */
	public TestResult facebookIMGroupConversationTest()
			throws FacebookException
	{
		TestResult result = new TestResult("IM Group Conversation Test",
				System.out);
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
		DataManager data1 = new DataManager(user1.getAccessToken(), "");
		DataManager data2 = new DataManager(user2.getAccessToken(), "");
		DataManager data3 = new DataManager(user3.getAccessToken(), "");

		System.out.println("Making the users friends");
		fSession.makeFriendTestUser(user1, user2);
		fSession.makeFriendTestUser(user1, user3);
		fSession.makeFriendTestUser(user2, user3);

		openLink(user1.getLoginUrl());
		String message1 = "Hi guys, this is Abby.";
		System.out.println("Please create a group message with Bob and Cathy");
		System.out.println(message1);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data1.collectData(true, false, false);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		openLink(user2.getLoginUrl());
		String message2 = "Hi guys, this is Bob.";
		System.out
				.println("Please send this message in the group conversation");
		System.out.println(message2);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data2.collectData(true, false, false);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		openLink(user3.getLoginUrl());
		String message3 = "Hi guys, this is Cathy.";
		System.out
				.println("Please send this message in the group conversation");
		System.out.println(message3);
		System.out.print("Enter done when finished. ");
		scan.nextLine();

		data3.collectData(true, false, false);
		System.out
				.print("Log out of the test user and back into your real profile. Enter done when finished. ");
		scan.nextLine();

		// Check that Abby had a conversation with bob and cathy
		ArrayList<Conversation> data1Con = data1.getConversations();
		Conversation groupConvo1 = data1Con.get(0);
		result.addResult(
				"Abby's ID, convo has Bob and Cathy",
				hasParticipant(groupConvo1, user2.getId())
						&& hasParticipant(groupConvo1, user3.getId()), true);

		// Only her message because of how Facebook test users have to log out
		result.addResult("Abby's ID, convo has her sent message", groupConvo1
				.getMessages().get(0).getMessage().equals(message1), true);

		ArrayList<Conversation> data2Con = data2.getConversations();
		Conversation groupConvo2 = data2Con.get(0);
		result.addResult(
				"Bob's ID, convo has Abby and Cathy",
				hasParticipant(groupConvo2, user1.getId())
						&& hasParticipant(groupConvo2, user3.getId()), true);

		// Only the first two because of how Facebook test users have to log out
		result.addResult("Bob's ID, convo has the first two messages",
				groupConvo2.getMessages().get(0).getMessage().equals(message1)
						&& groupConvo2.getMessages().get(1).getMessage()
								.equals(message2), true);

		ArrayList<Conversation> data3Con = data3.getConversations();
		Conversation groupConvo3 = data3Con.get(0);
		result.addResult(
				"Cathy's ID, convo has Abby and Bob",
				hasParticipant(groupConvo3, user1.getId())
						&& hasParticipant(groupConvo3, user2.getId()), true);
		result.addResult("Cathy's ID, convo has all three messages",
				groupConvo3.getMessages().get(0).getMessage().equals(message1)
						&& groupConvo3.getMessages().get(1).getMessage()
								.equals(message2)
						&& groupConvo3.getMessages().get(2).getMessage()
								.equals(message3), true);

		clearTestUsers();

		return result;
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

	/**
	 * Creates a facebook TestUser with a given username
	 * 
	 * @param username
	 *            the new TestUser's name
	 * @return
	 */
	private TestUser createTestUser(String username)
	{
		try
		{
			TestUser tu = fSession.createTestUser("442864129167674", // App ID
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

	/**
	 * This method deletes all current Facebook test users
	 */
	private void clearTestUsers()
	{
		List<TestUser> users;
		try
		{
			users = fSession.getTestUsers("442864129167674");
			System.out.println("Deleting test users");
			for (TestUser tu : users)
			{
				if (tu.getAccessToken() != null)
				{
					fSession.setOAuthAccessToken(new AccessToken(tu.getAccessToken()));
					fSession.deleteTestUser(tu.getId());
				}
			}
			resetFacebookSession();
		}
		catch (FacebookException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Helper function to determine if a given facebook conversation has a given
	 * user id.
	 * 
	 * @param c
	 *            Facebook IM conversation
	 * @param id
	 *            Facebook User id
	 * @return If the user corresponding with id participated in Conversation c
	 */
	private boolean hasParticipant(Conversation c, String id)
	{
		for (User u : c.getParticipants())
			if (u.getFacebookId().equals(id))
				return true;
		return false;
	}

	/**
	 * This function resets our facebook session from one with authenticated
	 * user to one with an authenticated app
	 */
	private void resetFacebookSession()
	{
		try
		{
			fSession.setOAuthAppId(FACEBOOK_APP_ID, FACEBOOK_APP_SECRET);
		}
		catch (IllegalStateException e)
		{
			// App id / secret pair already set
		}
		fSession.setOAuthAccessToken(APP_ACCESS_TOKEN);
	}

}
