package edu.uiowa.datacollection.facebook;

import java.util.ArrayList;
import java.util.Iterator;

import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

/**
 * This class holds all the data of a particular Stream event.
 * 
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

	private JSONArray commentArray;

	private ArrayList<Comment> comments = new ArrayList<Comment>();

	private JSONArray likeArray;

	private ArrayList<Like> likes = new ArrayList<Like>();

	/**
	 * This creates a StreamObject which holds a result from the stream FQL
	 * table.
	 * 
	 * @param object
	 *            The FQL JSON data
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
			System.out.println("ERROR: JSON improperly formatted.");
			System.out.println(e.getMessage());
		}
	}

	public void setCommentData(JSONArray commentArray)
	{
		for (int i = 0; i < commentArray.length(); i++)
		{
			try
			{
				comments.add(new Comment(commentArray.getJSONObject(i)));
			}
			catch (JSONException e)
			{
				System.out.println("ERROR: JSON improperly formatted.");
				System.out.println(e.getMessage());
			}
		}

		this.commentArray = commentArray;
	}
	
	public void setLikeData(JSONArray likeArray)
	{
		for (int i = 0; i < likeArray.length(); i++)
		{
			try
			{
				likes.add(new Like(likeArray.getJSONObject(i)));
			}
			catch (JSONException e)
			{
				System.out.println("ERROR: JSON improperly formatted.");
				System.out.println(e.getMessage());
			}
		}

		this.likeArray = likeArray;
	}

	public String toString()
	{
		String result = "";

		// Eclipse doesn't like casting jsonObject.keys()
		@SuppressWarnings("unchecked")
		Iterator<Object> i = jsonObject.keys();

		// Loop through each key in our JSON object and match it with its value,
		// one per line in our result string
		while (i.hasNext())
		{
			String s = (String) i.next();
			try
			{
				result += s + " : " + jsonObject.get(s) + "\n";
			}
			catch (JSONException e)
			{
				System.out.println("ERROR: JSON improperly formatted.");
				System.out.println(e.getMessage());
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
		try
		{
			jsonObject.put("Comments", commentArray);
			jsonObject.put("Likes", likeArray);
		}
		catch (JSONException e)
		{
			System.out.println("ERROR: JSON improperly formatted.");
			System.out.println(e.getMessage());
		}
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

	public ArrayList<Comment> getComments()
	{
		return comments;
	}
	
	public ArrayList<Like> getLikes()
	{
		return likes;
	}
	
	public boolean hasUserLiked(String userID)
	{
		for (Like like : likes)
			if (like.getUserID().equals(userID))
				return true;
		
		return false;
	}
}
