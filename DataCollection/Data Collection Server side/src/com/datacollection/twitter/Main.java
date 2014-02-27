package com.datacollection.twitter;

import java.util.List;


public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataManager dm=new DataManager();
		String userName="TengyuWang";
		//dm.loadAccessToken();
		dm.loadAccessTokenFromDatabase(userName);
		//dm.getUserAuthorization();
		/*dm.collectMentionsTimeLine(Long.valueOf(1));
		dm.collectUserTimeLine(Long.valueOf(1));
		dm.collectInstantMessages();
		dm.constructConversations();*/
		List<Conversation> conversationList=dm.collectData();

		int n=conversationList.size();
		for(int i=0;i<n;i++){
			Conversation c=conversationList.get(i);
			System.out.println(c.getJSONRepresentation().toString());
		}
		
	}

}
