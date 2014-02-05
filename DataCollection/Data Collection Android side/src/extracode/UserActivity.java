package extracode;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public abstract class UserActivity
{
	protected JSONObject jsonRepresentation = new JSONObject();

	/**
	 * This method allows the program to do a graph search on the post ID
	 * and supply additional information.
	 * @param data The JSON data from a graph query
	 */
	public void addJSONData(JSONObject data)
	{
		try
		{
			jsonRepresentation.put("graph_results", data);
		}
		catch (JSONException e)
		{
			Log.e("JSON Error", "JSON Data formatted incorrectly", e);
		}
	}

	public abstract JSONObject getJSONRepresentation();

	public abstract String getPostID();

}
