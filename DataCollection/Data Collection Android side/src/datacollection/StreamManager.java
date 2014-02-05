package datacollection;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;

/**
 * This Manager handles all Stream downloading operations.
 * @author Tom
 *
 */
public class StreamManager
{
	/**
	 * The error code passed by Facebook when the number of requests to the
	 * database pass 600 per 10 minutes, this number is not official and seems
	 * to have been found experimentally online.
	 */
	private static final int RATE_LIMIT_EXCEEDED_ERROR = 613;

	
	/**
	 * ArrayList holding all StreamObject data
	 */
	private ArrayList<StreamObject> streamObjects = new ArrayList<StreamObject>();
	
	
	/**
	 * This creates a new StreamManager, which allows for loading of data from
	 * the FQL stream table.
	 */
	public StreamManager()
	{
		
	}
	
	/**
	 * The primary method of this class, this loads stream data.
	 * @param session The authenticated Facebook session used for FQL requests
	 * @return the loaded StreamObjects
	 */
	public ArrayList<StreamObject> loadStream(Session session)
	{
		int offset = 0;
		
		//Again, 40 is used as the offsetIncrement because larger numbers are
		//unreliable. See Conversation or MessageManager for more details
		int offsetIncrement = 40;

		boolean streamLoaded = false;

		while (!streamLoaded)
		{
			//FQL query
			String fqlQuery = "SELECT message, description, type, post_id, "
					+ "actor_id, source_id, comment_info, like_info, created_time, "
					+ "updated_time, tagged_ids, message_tags, comments, likes FROM stream "
					+ "WHERE source_id = me() AND created_time > strtotime(\"-1 week\") "
					+ "LIMIT 40 offset " + offset;

			Bundle params = new Bundle();
			params.putString("q", fqlQuery);

			Request request = new Request(session, // Facebook session
					"/fql", // fql specified, not graph search
					params, // Bundled parameters, mapped "q" and the query
							// string
					HttpMethod.GET, // Type of request
					new Request.Callback() // Blank callback
					{
						public void onCompleted(Response response)
						{
						}
					});

			try
			{
				Response response = Request.executeBatchAsync(request).get()
						.get(0);

				// Check for errors
				if (response.getError() != null)
				{
					if (response.getError().getErrorCode() == RATE_LIMIT_EXCEEDED_ERROR)
					{
						// We have exceeded the rate at which we can make
						// requests, wait for three minutes.
						Log.i("INFO",
								"Waiting 3 minutes before trying again, we have exceeded the API rate limit");
						Thread.sleep(1000 * 60 * 3);
					}
				}
				else
				{
					//Extract the JSON data
					JSONArray streamList = response.getGraphObject()
							.getInnerJSONObject().getJSONArray("data");

					//If there is only blank JSON data, we've loaded everything
					if (streamList.length() == 0)
						streamLoaded = true;

					for (int i = 0; i < streamList.length(); i++)
					{
						streamObjects.add(new StreamObject(streamList
								.getJSONObject(i)));
					}
				}
			}
			catch (InterruptedException e)
			{
				Log.i("", "Interrupted Exception");
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				Log.i("", "Execution Exception");
				e.printStackTrace();
			}
			catch (JSONException e)
			{
				Log.i("", "JSON Exception");
				e.printStackTrace();
			}

			offset += offsetIncrement;
		}
		
		return streamObjects;
	}
	
	/**
	 * 
	 * @return the list of loaded StreamObjects
	 */
	public ArrayList<StreamObject> getStreamObjects()
	{
		return streamObjects;
	}
}
