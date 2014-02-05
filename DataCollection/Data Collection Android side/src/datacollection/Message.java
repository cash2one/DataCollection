package datacollection;

import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;


import android.util.Log;

/**
 * This class holds the information of one message in a Facebook conversation;
 * its author, the timestamp, its id, and the message.
 * @author Tom
 *
 */
public class Message implements Comparable<Message>
{
	/**
	 * The user sending this message.
	 */
	private User from;
	
	/**
	 * The time at which this message was sent.
	 */
	private GregorianCalendar timestamp;
	
	/**
	 * The message sent.
	 */
	private String message;
	
	/**
	 * The Facebook ID identifying this message
	 */
	private String id;
	
	/** 
	 * The JSON object representing this message.
	 */
	private JSONObject messageJSON;
	
	
	/**
	 * Creates a message object, held in Conversations
	 * @param messageJSON The JSON object from which to construct this message
	 */
	public Message(JSONObject messageJSON)
	{
		this.messageJSON = messageJSON;
		try
		{
			id = messageJSON.getString("message_id");
			
			//author_id is an int
			from = new User("" + messageJSON.getLong("author_id"));
			
			message = messageJSON.getString("body");
			
			//created_time is in seconds, we need it in milliseconds
			timestamp = new GregorianCalendar();
			timestamp.setTimeInMillis(messageJSON.getLong("created_time") * 1000);
			
		}
		catch (JSONException e)
		{
			Log.e("JSON Error", "Message JSON not correctly formatted", e);
		}
	}
	
	/**
	 * Overrides Object's equals method, determined by the Facebook ID
	 */
	public boolean equals(Object other)
	{
		if (other instanceof Message)
		{
			if (((Message)other).getId().equals(id))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return The timestamp of the message
	 */
	public GregorianCalendar getTimestamp()
	{
		return timestamp;
	}

	@Override
	/**
	 * Compares the mesasges, based on the timestamp
	 */
	public int compareTo(Message another)
	{
		return timestamp.compareTo(another.getTimestamp());
	}
	
	/**
	 * Returns the string representation, the Facebook id
	 */
	public String toString()
	{
		return id;
	}
	
	/**
	 * Used primarily for debugging
	 * @return Who the message is from and what it said.
	 */
	public String text()
	{
		return from + " : " + message;
	}

	/**
	 * 
	 * @return The Facebook ID of the message
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * 
	 * @return The user the message is from
	 */
	public User getFrom()
	{
		return from;
	}

	/**
	 * 
	 * @return The text of the message
	 */
	public String getMessage()
	{
		return message;
	}
	
	/**
	 * 
	 * @return The JSON representation of the message, to be saved on file.
	 */
	public JSONObject getJSONRepresentation()
	{
		return messageJSON;
	}
	
	/**
	 * This method puts real names on all of the message participants
	 * @param idNameMatches A hashset containing all of the facebook ID to real
	 * name matches
	 */
	public void populateNames(HashMap<String, String> idNameMatches)
	{
		from.setName(idNameMatches.get(from.getId()));
	}
}
