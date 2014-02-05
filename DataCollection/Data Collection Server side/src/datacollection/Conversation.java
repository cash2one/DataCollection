package datacollection;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

/**
 * The purpose of this class is to hold all data in a particular Facebook
 * conversation between two or more people. It also loads its messages using
 * a Facebook session and FQL requests.
 * @author Tom
 *
 */
public class Conversation
{
	//--------------------------------------------------------------------------
	//   Constants
	//--------------------------------------------------------------------------
	
	/**
	 * The error code passed by Facebook when the number of requests to the
	 * database pass 600 per 10 minutes, this number is not official and seems
	 * to have been found experimentally online.
	 */
	private static final int RATE_LIMIT_EXCEEDED_ERROR = 613;
	
	/**
	 * Number of milliseconds in a day, 1000 milliseconds to a second,
	 * 60 seconds to a minute, 60 minutes to an hour, 24 hours to a day.  
	 */
	private final long MILLISECONDS_IN_DAY = 1000 * 60* 60 * 24;
	
	//--------------------------------------------------------------------------
	//   Fields
	//--------------------------------------------------------------------------
		
	/**
	 * An ArrayList holding all of the conversation's messages. They are held
	 * with the oldest message at index 0, and the newest message at 
	 * index (size - 1)
	 */
	private ArrayList<Message> messages = new ArrayList<Message>();
	
	/**
	 * An array holding all of the conversations participants.
	 */
	private User[] participants;

	/**
	 * The unique ID that Facebook assigns to this conversation. 
	 */
	private String id;
	
	/**
	 * The number of messages in this conversation.
	 */
	private int messageCount;
	
	/**
	 * The time object that holds when the conversation was last updated.
	 */
	private GregorianCalendar updatedTime;
	
	/**
	 * The string that holds the most recent message in a conversation.
	 * It is also found in {@messages} at the end of the list.
	 */
	private String snippet;
	
	/**
	 * The variable containing the passed in JSON representation. This will be
	 * updated to hold the correct messages after messages have been updated.
	 */
	private JSONObject conversationJSON;
	

	//--------------------------------------------------------------------------
	//   Constructor and methods
	//--------------------------------------------------------------------------
	
	/**
	 * This creates a conversation object that holds all of the information
	 * in a Facebook conversation between two or more users.
	 * @param conversationJSON either the FQL JSON results or previously loaded conversation JSON object
	 */
	public Conversation(JSONObject conversationJSON)
	{
		this.conversationJSON = conversationJSON;
		try
		{
			id = conversationJSON.getString("thread_id");
			
			//The updated field time is in seconds, we need it in milliseconds
			updatedTime = new GregorianCalendar();
			updatedTime.setTimeInMillis(conversationJSON.getLong("updated_time") * 1000);
			
			
			messageCount = conversationJSON.getInt("message_count");
			
			
			//The recipients field holds all of the participants Facebook ID's
			JSONArray recip = conversationJSON.getJSONArray("recipients");
			participants = new User[recip.length()];
			snippet = conversationJSON.getString("snippet");
			
			
			for (int i = 0; i < recip.length(); i++)
				participants[i] = new User("" + recip.getLong(i));
			
			
			if (conversationJSON.has("messages"))
			{
				JSONArray messArray = conversationJSON.getJSONArray("messages");
				for (int k = 0; k < messArray.length(); k++)
					messages.add(new Message(messArray.getJSONObject(k)));
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is used to collect all of the messages for a conversation.
	 * It does this using an FQL query. It collects 40 messages, then changes
	 * the offset by 40, gets 40 more, and so on until all messages are collected.
	 * 40 was chosen because it is the largest number that FQL respects 
	 * concerning FQL queries. Even if a number like 50 or 60 is used, only 40
	 * results are returned.
	 * 
	 * @param lastUpdatedTime The time object that this conversation was last
	 * updated. It is passed in if this conversation was previously on record,
	 * otherwise it is null.
	 * @param session The Facebook session used to send an FQL query
	 * @param restrictToOneMonthBack If set to true the query will stop if the
	 * message timestamps are more than a month old.
	 */
	public void loadMessages(GregorianCalendar lastUpdatedTime, 
							 Facebook session,
							 boolean restrictToOneMonthBack)
	{
		//To keep our old to new ordering with FQL returning results new to old
		//we record the old size before updates, and then slide new messages in.
		int startIndex = messages.size();
		
		
		int offset = 0;
		boolean newMessagesLoaded = false;
		int offsetIncrement = 40;
		boolean errorOccurred = false;
		
		while (!newMessagesLoaded)
		{
			errorOccurred = false; 
			//The FQL query used. It returns messages newest to oldest.
String fqlQuery = "SELECT message_id, thread_id, author_id, body, created_time, " + 
		"attachment FROM message WHERE thread_id=" + id + 
		" ORDER BY created_time DESC LIMIT 40 OFFSET " + offset;
			
			JSONArray results;
			try
			{
				results = session.executeFQL(fqlQuery);
				
				//Load the new messages, check if we're done yet
				newMessagesLoaded = handleMessageQueryResponse(results, 
															   lastUpdatedTime,
															   restrictToOneMonthBack,
															   startIndex);
			}
			catch (FacebookException e)
			{
				if (e.getErrorCode() == RATE_LIMIT_EXCEEDED_ERROR)
				{
					//We exceeded the rate at which we can access the API
					//so we wait 3 minutes
					try
					{
						System.out.println("API Rate limit exceeded, waiting 3 minutes");
						Thread.sleep(1000 * 60 * 3);
					}
					catch (InterruptedException e1)
					{
					}
					errorOccurred = true;
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			
			//If an error did occur, we did not collect the data we wanted
			//and need to try again.
			if (!errorOccurred)
				offset += offsetIncrement;
		}
		
		System.out.println("Updated with " + messages.size() + " new messages");
	}
	
	/**
	 * This method handles inserting the JSON data from the FQL query into
	 * the messages array list.
	 * 
	 * @param response The FQL query results
	 * @param lastUpdatedTime The same value as passed to loadMessages
	 * @param restrictToOneMonthBack The same value as passed to loadMessages
	 * @param startIndex The size of the messages ArrayList before any updates
	 * @return newMessagesLoaded - whether to continue updating.
	 * @throws JSONException
	 */
	private boolean handleMessageQueryResponse(JSONArray results,
											   GregorianCalendar lastUpdatedTime,
											   boolean restrictToOneMonthBack,
											   int startIndex) throws JSONException
	{
		//Our "Finished" variable, it is set to true when we have finished
		//loading new messages
		boolean newMessagesLoaded = false;
		
		//Extract the JSON messages from our response
		JSONArray messageArray = results;
		
		//If there were no messages, we hit the end of the conversation.
		if (messageArray.length() == 0)
			newMessagesLoaded = true;
		
		//Used if we are restricting to one month back
		GregorianCalendar todaysDate = new GregorianCalendar();
		
		for (int i = 0; i < messageArray.length() && !newMessagesLoaded; i++)
		{
			JSONObject jsonMessage = messageArray.getJSONObject(i);
			Message message = new Message(jsonMessage);
			
			if (restrictToOneMonthBack)
			{
				//If the timestamp is more than 30 days old and we're restricting
				//it to one month, finish loading messages.
				if (todaysDate.getTime().getTime() - message.getTimestamp().getTime().getTime() > MILLISECONDS_IN_DAY * 30)
					newMessagesLoaded = true;
			}
			
			//If we have no previous messages or the current message is older
			//than the last updated time
			if (lastUpdatedTime == null || message.getTimestamp().after(lastUpdatedTime))
			{
				/*
				 * The messages returned by FQL are in order from
				 * newest to oldest, so we put messages at the old length
				 * index so that we keep the oldest->newest order
				 * in our list.
				 * 
				 *           0 1 2 3 4 5 6
				 * Old list: Z Y X W V U T
				 * startIndex = 7
				 * 
				 * FQL results: A B C
				 *                      inserted at position 7
				 * list: Z Y X W V U T  A
				 * list: Z Y X W V U T  B A
				 * list: Z Y X W V U T  C B A
				 * Time order was preserved.
				 */
				messages.add(startIndex, message);
			}
			//The current message happened at or before our already loaded time
			else if (message.getTimestamp().before(lastUpdatedTime) || 
					 message.getTimestamp().equals(lastUpdatedTime))
			{
				newMessagesLoaded = true;
			}
		}
		
		return newMessagesLoaded;
	}
	
	/**
	 * @return The string representation of the conversation object, the thread ID.
	 */
	public String toString()
	{
		return id;
	}

	/**
	 * 
	 * @return The list of all messages
	 */
	public ArrayList<Message> getMessages()
	{
		return messages;
	}

	/**
	 * 
	 * @return List of all conversation participants
	 */
	public User[] getParticipants()
	{
		return participants;
	}
	
	/**
	 * 
	 * @return String showing conversation participants
	 */
	public String getParticipantString()
	{
		String result = "";
		for (int i = 0; i < participants.length; i++)
		{
			result += participants[i].toString();
			if (i != participants.length - 1)
				result += ", ";
		}
		return result;
	}

	/**
	 * 
	 * @return The unique Facebook thread ID
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * 
	 * @return The number of messages in the conversation
	 */
	public int getMessageCount()
	{
		return messageCount;
	}
	
	/**
	 * 
	 * @return When the conversation was last updated.
	 */
	public GregorianCalendar getUpdatedTime()
	{
		return updatedTime;
	}
	
	/**
	 * 
	 * @return A JSON representation of the conversation, to be stored on file.
	 */
	public JSONObject getJSONRepresentation()
	{
		JSONArray mess = new JSONArray();
		for (Message m : messages)
		{
			mess.put(m.getJSONRepresentation());
		}
		
		JSONObject result = null;
		try
		{
			result = conversationJSON.put("messages", mess);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		};
		return result;
	}
	
	/**
	 * 
	 * @return The last message of the conversation
	 */
	public String getSnippet()
	{
		return snippet;
	}
	
	/**
	 * 
	 * @param messages The messages of the conversation.
	 */
	public void setMessages(ArrayList<Message> messages)
	{
		this.messages = messages;
	}
}
