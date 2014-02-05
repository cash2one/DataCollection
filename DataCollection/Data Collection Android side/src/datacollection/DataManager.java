package datacollection;

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
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;

/**
 * This class handles the main data loading operations of the app. It loads, saves,
 * and deletes file data, as well as downloading Facebook Message and Stream activity
 * using a {@link datacollection.MessageManager} and a {@link datacollection.StreamManager}.
 * @author Tom
 *
 */
public class DataManager
{
	/**
	 * ArrayList holding the conversation objects.
	 */
	private ArrayList<Conversation> conversations = new ArrayList<Conversation>();
	
	/**
	 * ArrayList holding the stream objects.
	 */
	private ArrayList<StreamObject> streamObjects = new ArrayList<StreamObject>();
	
	/**
	 * The initialized Facebook session from MainActivity
	 */
	private Session session;
	
	/**
	 * The MessageManager that handles downloading messages and dealing with them.
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
	 * This creates a DataManager object that will handle all of the app's
	 * data collection
	 * @param session an initialized and authenticated Facebook session
	 */
	public DataManager(Session session)
	{
		this.session = session;
	}
	
	/**
	 * This method allows for the collection of Facebook message and Stream
	 * data, and is the primary method of the class.
	 * @param collectMessages Should Facebook messages be collected
	 * @param limitToMonth Should messages older than one month be ignored
	 * @param collectStream Should stream data be collected
	 * @param loadOldData Should old data be loaded
	 */
	public void collectData(boolean collectMessages, 
							boolean limitToMonth, 
							boolean collectStream,
							boolean loadOldData)
	{
		if (loadOldData)
			loadOldJSONData();
		
		//Initialize messageManager with loaded conversations if it loaded,
		//otherwise conversations is ignored by messageManager
		messageManager = new MessageManager(conversations);
		
		if (collectMessages)
		{
			//Load messages
			messageManager.loadConversations(limitToMonth, session);
			conversations = messageManager.getConversations();
		}
		
		
		if (collectStream)
		{
			//Load stream
			streamManager = new StreamManager();
			streamObjects = streamManager.loadStream(session);
		}
	}	
	
	/**
	 * This method saves off all loaded data concerning the Facebook messages
	 * and stream. It does not currently save off ID-Name pairs
	 */
	public void saveJSONData()
	{
		Log.i("", "" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
		File file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "conversations.json");
		
		JSONObject result = new JSONObject();
		JSONArray convData = new JSONArray();
		JSONArray streamData = new JSONArray();
		JSONArray participantData = new JSONArray();
		
        try 
        {
    		for (Conversation c : conversations)
    			convData.put(c.getJSONRepresentation());
    		for (StreamObject so : streamObjects)
    			streamData.put(so.getJSONRepresentation());
    		for (String id : idNameMatches.keySet())
    			participantData.put(new JSONObject().put("id", id).put("name", idNameMatches.get(id)));
        	
    		
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            
			result.put("conversation_data", convData);
            result.put("stream_data", streamData);
            result.put("participant_data", participantData);
			
            pw.append(result.toString(1) + "\n");
            
            
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            Log.e("File Error", "******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the manifest?", e);
        } catch (IOException e) {
            e.printStackTrace();
        }
		catch (JSONException e)
		{
			e.printStackTrace();
		}	
	}
	
	/***
	 * Helper method for loading JSON data
	 * @param rd BufferedReader to the JSON file
	 * @return the String jsonText
	 * @throws IOException
	 */
	private String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	    	sb.append((char) cp);
	    }
	    return sb.toString();
	}
	
	/**
	 * This method loads all previously saved JSON data
	 */
	public void loadOldJSONData()
	{
		//TODO: find new file location to save
		Log.i("", "" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
		File file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "conversations.json");
		
        try 
        {           
        	//Open the file
            BufferedReader rd = new BufferedReader(new FileReader(file));
            
            //Read all of the data into a string
		    String jsonText = readAll(rd);
		    
		    //Convert the string to JSON data
		    JSONObject loadedData = new JSONObject(jsonText);
		    
		    //Load conversation data
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
		    	Log.i("", "NOTE: No conversation data loaded");
		    }
		    
		    
		    //If stream data is to be loaded, load it here.
		    //The concern I have with that is the possibility of updates
		    //such as likes and comments, that we might always want to freshly
		    //load the stream
		    if (loadedData.has("stream_data"))
		    {
		    	//TODO: handle loading stream posts
		    }
		    else
		    {
		    	Log.i("", "NOTE: No stream data loaded");
		    }
            
		    
		    //Load the id name matches
		    if (loadedData.has("participant_data"))
		    {
		    	JSONArray users = loadedData.getJSONArray("participant_data");
	            for (int i = 0; i < users.length(); i++)
	            {
	            	JSONObject obj = users.getJSONObject(i);
	            	idNameMatches.put(obj.getString("id"), obj.getString("name"));
	            }
		    }
		    else
		    {
		    	Log.i("", "NOTE: No participant data loaded");
		    }
            
            
            rd.close();
        } catch (FileNotFoundException e) {
            Log.i("", "******* File not found. Disregard this if you haven't run the program before.");
        } catch (IOException e) {
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
		File file = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "conversations.json");
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
		HashSet<User> userSet = messageManager.getUserSet();
		
		//Don't download data we already have
		for (String id : idNameMatches.keySet())
			userSet.remove(new User(id));
		
		User[] userArray = userSet.toArray(new User[userSet.size()]);
		
		for (int i = 0; i < userArray.length; i += 40)
		{			
			RequestBatch rb = new RequestBatch();
			for (int k = i; k < i + 40 && k < userArray.length; k++)
			{
				Bundle params = new Bundle();
				params.putString("fields", "id,name");
				Request request = new Request(session, //Facebook session
						  "/" + userArray[k].getId(),
						  params,
						  HttpMethod.GET, //Type of request
						  new Request.Callback() //Blank callback
						  { public void onCompleted(Response response) {} }
				);
				rb.add(request);
			}
			
			List<Response> responses = null;
			try
			{
				responses = rb.executeAsync().get();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
			
			
			for (int j = 0; j < responses.size(); j++)
			{
				Response r = responses.get(j);
				if (r.getError() == null)
				{
					JSONObject obj = r.getGraphObject().getInnerJSONObject();
					try
					{
						idNameMatches.put(obj.getString("id"), obj.getString("name"));
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					//Error here because of some permissions error - not friends and can't view it
					Log.i("","Unable to load information on Facebook ID: " + userArray[i + j]);
				}
			}
		}
		
		messageManager.populateNames(idNameMatches);
	}
}
