package edu.uiowa.datacollection.facebook;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;

/**
 * This Manager handles all operations pertaining to the downloading of Facebook
 * conversations and their messages.
 * 
 * @author Tom
 * 
 */
public class MessageManager
{
	// --------------------------------------------------------------------------
	// Constants
	// --------------------------------------------------------------------------

	/**
	 * The error code passed by Facebook when the number of requests to the
	 * database pass 600 per 10 minutes, this number is not official and seems
	 * to have been found experimentally online.
	 */
	private static final int RATE_LIMIT_EXCEEDED_ERROR = 613;

	/**
	 * Number of milliseconds in a day, 1000 milliseconds to a second, 60
	 * seconds to a minute, 60 minutes to an hour, 24 hours to a day.
	 */
	private final long MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

	// --------------------------------------------------------------------------
	// Fields
	// --------------------------------------------------------------------------

	/**
	 * The list of all conversations
	 */
	private ArrayList<Conversation> conversations = new ArrayList<Conversation>();

	/**
	 * A hash set that will contain all of the user objects
	 */
	private HashSet<User> userSet = new HashSet<User>();

	// --------------------------------------------------------------------------
	// Constructor and Methods
	// --------------------------------------------------------------------------

	/**
	 * This creates a MessageManager, which is used for doing all operations on
	 * conversations.
	 * 
	 * @param conversations
	 *            old data that was loaded from JSON file. If it is null it
	 *            isn't used.
	 */
	public MessageManager(ArrayList<Conversation> conversations)
	{
		if (conversations != null)
			this.conversations = conversations;
	}

	/**
	 * /** This method is used to collect all of the conversations. It does this
	 * using an FQL query. It collects 40 messages, then changes the offset by
	 * 40, gets 40 more, and so on until all conversations are collected. 40 was
	 * chosen because it is the largest number that FQL respects concerning FQL
	 * queries. Even if a number like 50 or 60 is used, only 40 results are
	 * returned.
	 * 
	 * @param limitToOneMonth
	 *            whether to only load conversations that have taken place
	 *            within the last month
	 * @param session
	 *            The facebook session from which FQL requests can be sent
	 * @throws FacebookTokenExpiredError
	 * @throws FacebookUnhandledException
	 */
	public void loadConversations(boolean limitToOneMonth, Facebook session)
			throws FacebookTokenExpiredError, FacebookUnhandledException
	{
		int offset = 0;
		int offsetIncrement = 40;

		boolean conversationsLoaded = false;
		boolean oldDataExists = conversations.size() > 0;
		boolean errorOccurred = false;

		while (!conversationsLoaded)
		{
			errorOccurred = false;
			String fqlQuery = "SELECT thread_id, message_count, updated_time, "
					+ "snippet_author, snippet, recipients FROM thread WHERE folder_id = 0 "
					+ "ORDER BY updated_time DESC LIMIT 40 OFFSET " + offset;

			try
			{
				// Extract the JSON information from the response
				JSONArray jsonConvos = session.executeFQL(fqlQuery);

				// If there was an empty JSON result, we have all the
				// conversations
				if (jsonConvos.length() == 0)
					conversationsLoaded = true;

				if (oldDataExists)
				{
					// There is loaded data so we have to handle loading
					// new data carefully
					conversationsLoaded = updateOldData(jsonConvos, session,
							limitToOneMonth);
				}
				else
				{
					// There was no previously loaded data, so we
					// just add conversations into conversations
					for (int i = 0; i < jsonConvos.length(); i++)
					{
						Conversation c = new Conversation(
								jsonConvos.getJSONObject(i));

						// Null is used because there is no previously loaded
						// conversation to get a timestamp from
						c.loadMessages(null, session, limitToOneMonth);

						conversations.add(c);

						if (limitToOneMonth)
						{
							GregorianCalendar todaysData = new GregorianCalendar();

							// If the timestamp is more than 30 days old and
							// we're restricting
							// it to one month, finish loading conversations.
							if (todaysData.getTime().getTime()
									- c.getUpdatedTime().getTime().getTime() > MILLISECONDS_IN_DAY * 30)
							{
								conversationsLoaded = true;
								break;
							}
						}
					}
				}
			}
			catch (JSONException e)
			{
				System.out.println("ERROR: JSON improperly formatted.");
				System.out.println(e.getMessage());
			}
			catch (FacebookException e)
			{
				if (e.getErrorCode() == RATE_LIMIT_EXCEEDED_ERROR)
				{
					// We exceeded the rate at which we can access the API
					// so we wait 3 minutes
					try
					{
						System.out
								.println("API Rate limit exceeded, waiting 3 minutes");
						Thread.sleep(1000 * 60 * 3);
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
					errorOccurred = true;
				}
				else if (e.getErrorCode() == FacebookTokenExpiredError.TOKEN_EXPIRED_ERROR)
				{
					throw new FacebookTokenExpiredError(e);
				}
				else
				{
					throw new FacebookUnhandledException(e);
				}
			}

			// If an error did occur, we did not collect the data we wanted
			// and need to try again.
			if (!errorOccurred)
				offset += offsetIncrement;
		}
	}

	/**
	 * This method is called if there was previous conversation data loaded. It
	 * ensures new conversations are put in the correct place in the
	 * conversations list.
	 * 
	 * @param jsonConvos
	 *            The JSON data that holds a set of 40 conversations
	 * @param session
	 *            The Facebook session that allows for FQL requests
	 * @param limitToOneMonth
	 *            Boolean determining if conversations older than one month
	 *            should be loaded
	 * @return If all conversations are loaded
	 * @throws JSONException
	 * @throws FacebookTokenExpiredError
	 * @throws FacebookUnhandledException
	 */
	private boolean updateOldData(JSONArray jsonConvos, Facebook session,
			boolean limitToOneMonth) throws JSONException,
			FacebookTokenExpiredError, FacebookUnhandledException
	{
		boolean conversationsLoaded = false;
		// The outer loop goes through each of the new conversations that
		// are in the JSONArray jsonConvos
		for (int i = 0; i < jsonConvos.length() && !conversationsLoaded; i++)
		{
			// We've selected a new conversation
			Conversation c = new Conversation(jsonConvos.getJSONObject(i));

			// Now we loop through all of our existing conversations
			// to find where to put it.
			// In conversations the newest messages are at the front of the
			// list.
			for (int k = 0; k < conversations.size(); k++)
			{

				// Our selected conversation is newer than the existing
				// conversation
				if (c.getUpdatedTime().after(
						conversations.get(k).getUpdatedTime()))
				{
					Conversation oldConversation = null;
					GregorianCalendar oldUpdatedTime = null;

					// If our selected conversation has the same ID, then it is
					// just an updated version of it. Because we haven't yet
					// loaded c's messages, we give it all of the k'th
					// conversation's
					// messages.
					if (c.getId().equals(conversations.get(k).getId()))
					{
						oldUpdatedTime = conversations.get(k).getUpdatedTime();

						// Old conversation is now what c replaced in
						// conversations
						oldConversation = conversations.set(k, c);

						// We give the old messages
						conversations.get(k).setMessages(
								oldConversation.getMessages());

						// We use the old latest updated time to set a baseline
						// for loading the new messages of the conversation.
						conversations.get(k).loadMessages(oldUpdatedTime,
								session, limitToOneMonth);

						// break out of the inner loop, back to a new JSON
						// conversation
						break;
					}
					// We've found the correct place chronologically in the list
					// for our new conversation, so we put it in. We then go
					// through
					// the rest of the list to remove any conversation with the
					// same
					// thread ID. If there was one, we do the same thing as
					// above,
					// and transfer the messages and use the old updated time.
					else
					{
						// Add the new conversation in the correct place
						conversations.add(k, c);

						// Loop through and remove any conversation with the
						// same ID
						for (int j = k + 1; j < conversations.size();)
						{
							if (conversations.get(j).getId().equals(c.getId()))
							{
								// Store the old conversation and old time
								oldConversation = conversations.remove(j);
								oldUpdatedTime = oldConversation
										.getUpdatedTime();
							}
							else
								j++;
						}

						// If oldConversation isn't null we removed an old copy
						// and so we need to transfer the messages
						if (oldConversation != null)
						{
							conversations.get(k).setMessages(
									oldConversation.getMessages());
						}

						// Now we load the messages. If oldUpdatedTime is null,
						// there was no previous record of the conversation, and
						// so we load all of it. Otherwise we baseline off that
						// time.
						conversations.get(k).loadMessages(oldUpdatedTime,
								session, limitToOneMonth);

						// break out of the inner loop, back to a new JSON
						// conversation
						break;
					}
				}
				// This can only happen if the previous condition evaluated to
				// false
				// c.getUpdatedTime().after(conversations.get(k).getUpdatedTime())
				// and so the timestamps must be equal, therefore there are no
				// updates.
				// If this happens, all later conversation in our JSON list will
				// be
				// older than the current one, and so we move to the end of both
				// for loops and break, we have finished loading.
				else if (c.getId().equals(conversations.get(k).getId()))
				{
					i = jsonConvos.length();
					k = conversations.size();
					conversationsLoaded = true;
					break;
				}
				// If we've reached the end, load all of c's messages
				// and add it to the end of the list.
				else if (k == conversations.size() - 1)
				{
					System.out.println("debug statement");
					c.loadMessages(null, session, limitToOneMonth);
					conversations.add(c);
				}
			}
		}

		return conversationsLoaded;
	}

	/**
	 * 
	 * @return a list of all conversations loaded
	 */
	public ArrayList<Conversation> getConversations()
	{
		return conversations;
	}

	/**
	 * 
	 * @return a hashset of all users in all of the messages
	 */
	public HashSet<User> getUserSet()
	{
		// Populate the hashset of all users
		for (Conversation c : conversations)
			for (User u : c.getParticipants())
				userSet.add(u);

		return userSet;
	}

	/**
	 * This method puts real names on all of the conversation participants
	 * 
	 * @param idNameMatches
	 *            A hashset containing all of the facebook ID to real name
	 *            matches
	 */
	public void populateNames(HashMap<String, String> idNameMatches)
	{
		for (Conversation c : conversations)
		{
			for (User u : c.getParticipants())
			{
				u.setName(idNameMatches.get(u.getFacebookId()));
			}

			for (Message m : c.getMessages())
			{
				m.populateNames(idNameMatches);
			}
		}
	}
}
