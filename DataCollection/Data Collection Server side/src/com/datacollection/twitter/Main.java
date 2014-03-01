package com.datacollection.twitter;

import java.util.ArrayList;
import java.util.List;


public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataManager dm=new DataManager();
		//String userName="TengyuWang";
		//dm.loadAccessToken();
		//dm.loadAccessTokenFromDatabase(userName);
		dm.loadHardCodedToken(1);
		//dm.getUserAuthorization();
		/*dm.collectMentionsTimeLine(Long.valueOf(1));
		dm.collectUserTimeLine(Long.valueOf(1));
		dm.collectInstantMessages();
		dm.constructConversations();*/
		String url="http://172.23.0.45:8000/server/gettwittertoken/";
		//List<User> userList=dm.getUserFromServer();
		List<User> userList=dm.getUserFromServer(url);
		ArrayList<ArrayList<Conversation>> listconversationList=dm.collectData(userList);
		int n=listconversationList.size();
		for(int i=0;i<n;i++){
			List<Conversation> list=listconversationList.get(i);
			for(int j=0;j<list.size();j++){
				Conversation c=list.get(j);
				System.out.println(c.getJSONRepresentation().toString());
			}
			System.out.println();
			System.out.println("*************************************************************************************  ");
			System.out.println();
		}
		
	}

}
