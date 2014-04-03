package edu.uiowa.datacollection.facebook;

import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;


public class Comment
{
	private String fromID, text, commentID;
	
	/**
	 * A comment object that holds the information of a comment on a post
	 * @param commentJSON The FQL data on the comment
	 */
	public Comment(JSONObject commentJSON)
	{
		try
		{
			fromID = commentJSON.getString("fromid");
			text = commentJSON.getString("text");
			commentID = commentJSON.getString("id");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	public String getFromID()
	{
		return fromID;
	}

	public String getText()
	{
		return text;
	}

	public String getCommentID()
	{
		return commentID;
	}
}
