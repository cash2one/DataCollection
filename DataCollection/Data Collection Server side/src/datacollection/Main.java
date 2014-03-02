package datacollection;

import java.io.IOException;

import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class Main
{
	public static String SERVER_URL = "http://128.255.45.52:7777/server/getfacetoken/";
	
	public static void main(String[] args) throws JSONException, FacebookException, IOException
	{
		JSONObject obj = JsonReader.readJsonFromUrl(SERVER_URL);
		
		JSONArray users = obj.getJSONArray("data");
		
		for (int i = 0; i < users.length(); i++)
		{
			JSONObject user = users.getJSONObject(i);
			System.out.println(user.toString(1));

			String accessToken = user.getString("token");
			String phoneNumber = user.getString("phone");
			JSONArray lastConvoTimes = user.getJSONArray("info");
			
			System.out.println("Access Token: " + accessToken);
			System.out.println("Phone Number: " + phoneNumber);
			
			DataManager manager = new DataManager(accessToken);
			manager.loadOldConversationTimes(lastConvoTimes);
			
			manager.collectData(false, // Collect message 
						  		false, //Limit to one month
						  		true, //Collect stream
						  		false); //Load old data
			
			manager.uploadData(phoneNumber);
		}
	}
}
