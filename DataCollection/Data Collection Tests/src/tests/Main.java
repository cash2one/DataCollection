package tests;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.internal.org.json.JSONException;

public class Main
{
	public static void main(String[] args) throws JSONException
	{
		Facebook session = new FacebookFactory().getInstance();
		session.setOAuthAppId("442864129167674",
				"f2140fbb0148c5db21db0d07b92e6ade");

		TestManager testManager = new TestManager(session);
		try
		{
//			System.out.println(testManager.imConversationTest());
			
			System.out.println(testManager.imGroupConversationTest());
			
//			System.out.println(testManager.statusTest());
			
//			System.out.println(testManager.wallPostTest());
		}
		catch (FacebookException e)
		{
			e.printStackTrace();
		}

	}

}
