package datacollection;

import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;


public class Comment
{
	private String fromID, text, commentID;
	
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
