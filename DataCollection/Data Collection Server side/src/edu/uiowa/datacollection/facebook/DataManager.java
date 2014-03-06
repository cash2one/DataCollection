package edu.uiowa.datacollection.facebook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;


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
@SuppressWarnings("deprecation")
public class DataManager
{
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
	 * This creates a DataManager object that will handle all of the app's data
	 * collection
	 * 
	 * @param accessToken
	 *            the string from the app that allows for Facebook access
	 */
	public DataManager(String accessToken)
	{
		AccessToken token = new AccessToken(accessToken, null);

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
			boolean collectStream, boolean loadOldData)
	{
		if (loadOldData)
			loadOldJSONData();
		
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
	public void saveJSONData()
	{
		File file = new File("conversations.json");

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

	/***
	 * Helper method for loading JSON data
	 * 
	 * @param rd
	 *            BufferedReader to the JSON file
	 * @return the String jsonText
	 * @throws IOException
	 */
	private String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1)
		{
			sb.append((char) cp);
		}
		return sb.toString();
	}

	/**
	 * This method loads all previously saved JSON data
	 */
	public void loadOldJSONData()
	{
		File file = new File("conversations.json");

		try
		{
			// Open the file
			BufferedReader rd = new BufferedReader(new FileReader(file));

			// Read all of the data into a string
			String jsonText = readAll(rd);

			// Convert the string to JSON data
			JSONObject loadedData = new JSONObject(jsonText);

			// Load conversation data
			if (loadedData.has("conversation_data"))
			{
				JSONArray convos = loadedData.getJSONArray("conversation_data");
				for (int i = 0; i < convos.length(); i++)
				{
					JSONObject obj = convos.getJSONObject(i);
					conversations.add(new Conversation(obj));
				}
			}
			else
			{
				System.out.println("NOTE: No conversation data loaded");
			}

			// If stream data is to be loaded, load it here.
			// The concern I have with that is the possibility of updates
			// such as likes and comments, that we might always want to freshly
			// load the stream
			if (loadedData.has("stream_data"))
			{
				// TODO: handle loading stream posts
			}
			else
			{
				System.out.println("NOTE: No stream data loaded");
			}

			// Load the id name matches
			if (loadedData.has("participant_data"))
			{
				JSONArray users = loadedData.getJSONArray("participant_data");
				for (int i = 0; i < users.length(); i++)
				{
					JSONObject obj = users.getJSONObject(i);
					idNameMatches.put(obj.getString("id"),
							obj.getString("name"));
				}
			}
			else
			{
				System.out.println("NOTE: No participant data loaded");
			}

			rd.close();
		}
		catch (FileNotFoundException e)
		{
			System.out
					.println("******* File not found. Disregard this if you haven't run the program before.");
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
	 * Method to delete saved data
	 */
	public void deleteOldData()
	{
		File file = new File("conversations.json");
		file.delete();
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

	public void uploadData(String phoneNumber)
	{
		JSONObject obj = getJSONData();
		try
		{
			obj.put("user", phoneNumber);
		}
		catch (JSONException e1)
		{
			e1.printStackTrace();
		}
		
		
		try
		{
			HttpPost post = new HttpPost(SERVER_URL);
			post.setEntity(new ByteArrayEntity(obj.toString().getBytes()));
			HttpResponse resp = null;
			@SuppressWarnings({ "resource" })
			HttpClient httpclient = new DefaultHttpClient();
			try
			{
				resp = httpclient.execute(post);
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			System.out.println(resp);
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}

	}

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
