package edu.uiowa.datacollection.tests;

import java.util.List;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.TestUser;
import facebook4j.internal.org.json.JSONException;

public class Main
{
	public static void main(String[] args) throws JSONException
	{
		Facebook session = new FacebookFactory().getInstance();
		

		TestManager testManager = new TestManager(session, null);
		try
		{
//			System.out.println(testManager.imConversationTest());
			List<TestUser> users = session.getTestUsers("442864129167674");
			for (TestUser tu : users)
				session.deleteTestUser(tu.getId());
			
//			System.out.println(testManager.imGroupConversationTest());
			users = session.getTestUsers("442864129167674");
			for (TestUser tu : users)
				session.deleteTestUser(tu.getId());
			
//			System.out.println(testManager.statusTest());
			users = session.getTestUsers("442864129167674");
			for (TestUser tu : users)
				session.deleteTestUser(tu.getId());
			
			System.out.println(testManager.wallPostTest());
			users = session.getTestUsers("442864129167674");
			for (TestUser tu : users)
				session.deleteTestUser(tu.getId());
		}
		catch (FacebookException e)
		{
			e.printStackTrace();
		}

	}

}
