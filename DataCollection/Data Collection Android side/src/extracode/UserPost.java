package extracode;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class UserPost extends UserActivity
{
	//Post id
	private String postID;
	
	//The content of the post
	private String content;

	
	public UserPost(Element tbodyContent)
	{		
		/*
		 * Get the post id by looking for the timestamp, as it has a link.
		 * The timestamp class has an id of _k3z, and so its searchable by that.
		 * We then extract the post id from the href attribute.
		 */
		Element timePostInfo = tbodyContent.select("a[class=_k3z]").get(0);
		String hrefString = timePostInfo.attr("href");
		postID = hrefString.substring(hrefString.lastIndexOf("/") + 1);
		
		
		/*
		 * The h5 element with a class name of uiStreamMessage contains
		 * the text displayed, what the user posted.
		 */
		Elements h5 = tbodyContent.select("h5[class=uiStreamMessage]");
		content = h5.get(0).text();
		
		jsonRepresentation = new JSONObject();
		
		try
		{
			jsonRepresentation.put("id", postID);
			jsonRepresentation.put("user_content", content);
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

	@Override
	public JSONObject getJSONRepresentation()
	{
		return jsonRepresentation;
	}
}