package edu.uiowa.datacollection.twitter;

import java.util.ArrayList;

import edu.uiowa.datacollection.util.JsonHelper;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONObject;

public class CollectTwitterData
{

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
		/*String url = "http://128.255.45.52:7777/server/gettwittertoken/";
		String postUrl = "http://128.255.45.52:7777/server/posttwitter/";*/
		String url = "http://127.0.0.1:8002/DataCollection/gettwittertoken/";
		String postUrl = "http://127.0.0.1:8002/DataCollection/posttwitter/";
		
		JSONObject obj = JsonHelper.readJsonFromUrl(url);
		JSONArray userList = obj.getJSONArray("data");
		for (int i = 0; i < userList.length(); i++)
		{
			JSONObject userToken = userList.getJSONObject(i);
			User u = new User(userToken.getString("twitter_id"), 2);
			u.setOauthToken(userToken.getString("twitter_token"));
			u.setTokenSecret(userToken.getString("twitter_secret"));
			u.setUserTimeLineSinceID(userToken.getLong("userTimeLineSinceID"));
			u.setMentionTimeLineSinceID(userToken.getLong("mentionTimeLineSinceID"));
			u.setDirectMessageSinceID(userToken.getLong("directMsgSinceID"));
			u.setSentDirectMessageSinceID(userToken.getLong("sentDirectMsgSinceID"));
			DataManager dm = new DataManager(u);
			ArrayList<Conversation> conversationList = dm
					.collectData();
			JsonHelper.postJsonData(postUrl, dm.getJsonData(conversationList));
			for (int j = 0; j < conversationList.size(); j++)
			{
				Conversation c = conversationList.get(j);
				System.out.println(c.getJSONRepresentation().toString(1));
			}
			System.out.println();
			System.out
					.println("*************************************************************************************  ");
		}

	}

}
