package edu.uiowa.datacollection.facebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.uiowa.datacollection.twitter.User;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

/**
 * This class handles the main data loading operations of the app. It loads,
 * saves, and deletes file data, as well as downloading Facebook Message and
 * Stream activity using a {@link edu.uiowa.datacollection.facebook.example.messagesiphon.MessageManager} and
 * a {@link edu.uiowa.datacollection.facebook.example.messagesiphon.StreamManager}.
 * 
 * @author Tom
 * 
 */
public class DataManager
{
	/**
	 * The URL of the server to post data to
	 */
	public static String SERVER_URL = "http://128.255.45.52:7777/server/postfacebook/";
	
	/**
	 * ArrayList holding the conversation objects.
	 */
	private ArrayList<Conversation> conversations = new ArrayList<Conversation>();

	/**
	 * ArrayList holding the stream objects.
	 */
	private ArrayList<StreamObject> streamObjects = new ArrayList<StreamObject>();

	/**
	 * The initialized Facebook session
	 */
	private Facebook session;

	/**
	 * The MessageManager that handles downloading messages and dealing with
	 * them.
	 */
	private MessageManager messageManager;

	/**
	 * The StreamManager that handles downloading stream data
	 */
	private StreamManager streamManager;

	/**
	 * The name ID matches. We only look up those that aren't already found.
	 */
	private HashMap<String, String> idNameMatches = new HashMap<String, String>();

	/**
	 * The phone number of the user whose data is being collected
	 */
	private String phoneNumber;
	
	/**
	 * This creates a DataManager object that will handle all of the app's data
	 * collection
	 * 
	 * @param accessToken
	 *            the string from the app that allows for Facebook access
	 */
	public DataManager(String accessToken, String phoneNumber)
	{
		AccessToken token = new AccessToken(accessToken, null);
		this.phoneNumber = phoneNumber;
		
		
		session = new FacebookFactory().getInstance();
		session.setOAuthAppId("442864129167674",
				"f2140fbb0148c5db21db0d07b92e6ade");
		session.setOAuthAccessToken(token);
	}

	/**
	 * This method allows for the collection of Facebook message and Stream
	 * data, and is the primary method of the class.
	 * 
	 * @param collectMessages
	 *            Should Facebook messages be collected
	 * @param limitToMonth
	 *            Should messages older than one month be ignored
	 * @param collectStream
	 *            Should stream data be collected
	 * @param loadOldData
	 *            Should old data be loaded
	 */
	public void collectData(boolean collectMessages, boolean limitToMonth,
			boolean collectStream)
	{
		// Initialize messageManager with loaded conversations if it loaded,
		// otherwise conversations is ignored by messageManager
		messageManager = new MessageManager(conversations);

		if (collectMessages)
		{
			// Load messages
			messageManager.loadConversations(limitToMonth, session);
			conversations = messageManager.getConversations();
		}

		if (collectStream)
		{
			// Load stream
			streamManager = new StreamManager();
			streamObjects = streamManager.loadStream(session);
		}
	}

	public JSONObject getJSONData()
	{
		JSONObject result = new JSONObject();
		JSONArray convData = new JSONArray();
		JSONArray streamData = new JSONArray();
		JSONArray participantData = new JSONArray();

		try
		{
			for (Conversation c : conversations)
				if (c.getJSONRepresentation() != null)
					convData.put(c.getJSONRepresentation());
			for (StreamObject so : streamObjects)
				streamData.put(so.getJSONRepresentation());
			for (String id : idNameMatches.keySet())
				participantData.put(new JSONObject().put("id", id).put("name",
						idNameMatches.get(id)));

			result.put("conversation_data", convData);
			result.put("stream_data", streamData);
			result.put("participant_data", participantData);
			result.put("user", phoneNumber);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This method saves off all loaded data concerning the Facebook messages
	 * and stream. It does not currently save off ID-Name pairs
	 */
	public void saveJSONData(String filename)
	{
		File file = new File(filename);

		try
		{
			FileOutputStream f = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(f);
			JSONObject result = getJSONData();

			pw.append(result.toString(1) + "\n");

			pw.flush();
			pw.close();
			f.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return The list of conversations loaded
	 */
	public ArrayList<Conversation> getConversations()
	{
		return conversations;
	}

	/**
	 * 
	 * @return The list of Stream objects loaded
	 */
	public ArrayList<StreamObject> getStreamObjects()
	{
		return streamObjects;
	}

	/**
	 * Method to load the Facebook ID - Name matches then and then to put them
	 * in all User instances
	 */
	public void collectParticipantInformation()
	{
		HashSet<edu.uiowa.datacollection.facebook.User> userSet = messageManager.getUserSet();

		// Don't download data we already have
		for (String id : idNameMatches.keySet())
			userSet.remove(new User(id, 1));

		HashMap<String, String> fqlQueries = new HashMap<String, String>();
		for (edu.uiowa.datacollection.facebook.User u : userSet)
			fqlQueries.put(u.getFacebookId(),
					"SELECT name FROM user WHERE uid = " + u.getFacebookId());

		try
		{
			Map<String, JSONArray> result = session.executeMultiFQL(fqlQueries);
			for (String idKey : result.keySet())
			{
				System.out.println(idKey + ", " + result.get(idKey));
				if (result.get(idKey).length() > 0) // There were results
				{
					idNameMatches.put(idKey, result.get(idKey).getJSONObject(0)
							.getString("name"));
				}
				else
				{
					System.out.println("Unable to get results for user id "
							+ idKey);
				}
			}
		}
		catch (FacebookException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		messageManager.populateNames(idNameMatches);
	}

	/**
	 * 
	 * @return The current Facebook session being used
	 */
	public Facebook getSession()
	{
		return session;
	}

	/**
	 * This method takes a list of all loaded conversations and their respective
	 * updated_time's and initializes the conversation list with those.
	 * This is done so that no already collected data is recollected.
	 * This is called prior to the main collectData method.
	 * @param lastConvoTimes A JSONArray full of objects with keys thread_id and
	 * updated_time
	 */
	public void loadOldConversationTimes(JSONArray lastConvoTimes)
	{
		for (int i = 0; i < lastConvoTimes.length(); i++)
		{
			JSONObject obj;
			try
			{
				obj = lastConvoTimes.getJSONObject(i);
				conversations.add(new Conversation(obj.getString("thread_id"), obj.getString("updated_time")));
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
	}
}
