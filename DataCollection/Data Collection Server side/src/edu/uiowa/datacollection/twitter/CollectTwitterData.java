package edu.uiowa.datacollection.twitter;

import java.util.ArrayList;
import java.util.List;

public class CollectTwitterData
{

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
		DataManager dm = new DataManager();
		// String userName="TengyuWang";
		// dm.loadAccessToken();
		// dm.loadAccessTokenFromDatabase(userName);
		// dm.loadHardCodedToken(1);
		// dm.getUserAuthorization();
		/*
		 * dm.collectMentionsTimeLine(Long.valueOf(1));
		 * dm.collectUserTimeLine(Long.valueOf(1)); dm.collectInstantMessages();
		 * dm.constructConversations();
		 */
		String url = "http://128.255.45.52:7777/server/gettwittertoken/";
		String postUrl = "http://128.255.45.52:7777/server/posttwitter/";
		// List<User> userList=dm.getUserFromServer();
		List<User> userList = dm.getUserFromServer(url);
		ArrayList<ArrayList<Conversation>> listconversationList = dm
				.collectData(userList);
		dm.excutePost(postUrl, listconversationList);
		int n = listconversationList.size();
		for (int i = 0; i < n; i++)
		{
			List<Conversation> list = listconversationList.get(i);
			for (int j = 0; j < list.size(); j++)
			{
				Conversation c = list.get(j);
				System.out.println(c.getJSONRepresentation().toString(1));
			}
			System.out.println();
			System.out
					.println("*************************************************************************************  ");
			System.out.println();
		}

	}

}
