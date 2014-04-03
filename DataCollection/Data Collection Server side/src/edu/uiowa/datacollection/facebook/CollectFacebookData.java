package edu.uiowa.datacollection.facebook;

import java.io.IOException;

import edu.uiowa.datacollection.util.JsonHelper;
import edu.uiowa.datacollection.util.PropertyHelper;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class CollectFacebookData
{
	//public static String SERVER_URL = "http://128.255.45.52:7777/server/getfacetoken/";
	
	public static void main(String[] args) throws JSONException, FacebookException, IOException
	{
		PropertyHelper ph=new PropertyHelper("dataCollection.properties");
		String SERVER_URL=ph.getURLAddress();
		JSONObject obj = JsonHelper.readJsonFromUrl(SERVER_URL+"getfacetoken/");
		
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
			
			DataManager manager = new DataManager(accessToken, phoneNumber);
			manager.loadOldConversationTimes(lastConvoTimes);
			
			manager.collectData(true, // Collect message 
						  		true, //Limit to one month
						  		true); //Collect stream
			
			JsonHelper.postJsonData(SERVER_URL, manager.getJSONData());
		}
	}
}
