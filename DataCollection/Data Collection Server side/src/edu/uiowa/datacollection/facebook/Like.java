package edu.uiowa.datacollection.facebook;

import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;


public class Like
{
	private String userID;
	
	/**
	 * A like object that holds the information of a like on a post
	 * @param likeJSON The FQL data on the like
	 */
	public Like(JSONObject likeJSON)
	{
		try
		{
			userID = likeJSON.getString("user_id");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public String getUserID()
	{
		return userID;
	}
}
