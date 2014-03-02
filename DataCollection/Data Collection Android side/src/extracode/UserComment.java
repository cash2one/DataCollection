package extracode;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class UserComment extends UserActivity
{	
	//Post id
	private String postID;
	
	//The content of the post
	private String content;
	
	public UserComment(Element tbodyContent)
	{		
		/*
		 * Get the post id by looking for the timestamp, as it has a link.
		 * The timestamp class has an id of _k3z, and so its searchable by that.
		 * We then extract the post id from the href attribute.
		 */
		Element timePostInfo = tbodyContent.select("a[class=_k3z]").get(0);
		String hrefString = timePostInfo.attr("href");
		if (hrefString.charAt(hrefString.length() - 1) == '/')
			hrefString = hrefString.substring(0, hrefString.length() - 1); 
		postID = hrefString.substring(hrefString.lastIndexOf("/") + 1);
				
		/*
		 * If the post id has the word photo in it we need to treat it differently.
		 * This is the format it will take:
photo.php?fbid=10201827587842107&set=a.4799909726369.2185796.1554827349&type=1
		 * To get the post we start after the first equals and move right until
		 * we hit a non numeric character.
		 */
		if (postID.contains("photo.php?fbid="))
		{
			String temp = postID.substring(postID.indexOf("=") + 1);
			int index = -1;
			for (int i = 0; i < temp.length() && index == -1; i++)
				if (!Character.isDigit(temp.charAt(i)))
					index = i;
			postID = temp.substring(0, index);
		}
		
		
		Elements h5 = tbodyContent.select("h5[class=uiStreamMessage]");
		content = h5.get(0).text();
		
		try
		{
			jsonRepresentation.put("id", postID);
			jsonRepresentation.put("user_comment", content);
		}
		catch (JSONException e)
		{
			Log.e("JSON Error", "JSON Data formatted incorrectly", e);
		}
	}


	public String getPostID()
	{
		return postID;
	}


	public String getContent()
	{
		return content;
	}
	
	public String toString()
	{
		return postID + " - " + content;
	}
	
	public JSONObject getJSONRepresentation()
	{
		return jsonRepresentation;
	}
}
