package datacollection;

import java.util.Iterator;

import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;


/**
 * This class holds all the data of a particular Stream event.
 * @author Tom
 *
 */
public class StreamObject
{
	/**
	 * The JSON data for this stream object
	 */
	private JSONObject jsonObject;
	
	/**
	 * The Facebook ID of this object
	 */
	private String postID;

	/**
	 * This creates a StreamObject which holds a result from the stream FQL table.
	 * @param object The FQL JSON data
	 */
	public StreamObject(JSONObject object)
	{
		this.jsonObject = object;
		try
		{
			this.postID = jsonObject.getString("post_id");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public String toString()
	{
		String result = "";
		
		//Eclipse doesn't like casting jsonObject.keys()
		@SuppressWarnings("unchecked")
		Iterator<Object> i = jsonObject.keys();
		
		//Loop through each key in our JSON object and match it with its value,
		//one per line in our result string
		while (i.hasNext())
		{
			String s = (String) i.next();
			try
			{
				result += s + " : " + jsonObject.get(s) + "\n";
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 
	 * @return The JSON data for this object
	 */
	public JSONObject getJSONRepresentation()
	{
		return jsonObject;
	}

	/**
	 * 
	 * @return This StreamObject's Facebook ID
	 */
	public String getPostID()
	{
		return postID;
	}
}
