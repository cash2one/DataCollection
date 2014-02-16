package com.datacollection.twitter;


public class Main {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DataManager dm=new DataManager();
		dm.loadAccessToken();
		//dm.getUserAuthorization();
		dm.collectMentionsTimeLine(Long.valueOf(1));
		dm.collectUserTimeLine(Long.valueOf(1));
		dm.collectInstantMessages();
		dm.constructConversations();
		int n=dm.getConversationList().size();
		for(int i=0;i<n;i++){
			Conversation c=dm.getConversationList().get(i);
			System.out.println(c.toString());
		}
		
	}

}
