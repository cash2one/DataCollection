package edu.uiowa.datacollection.tests;

import twitter4j.TwitterException;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONException;

public class Main
{
	public static void main(String[] args) throws JSONException
	{
		TestManager testManager = new TestManager();
		try
		{
			System.out.println(testManager.twitterDirectMessageTest());
			
			System.out.println(testManager.facebokIMConversationTest());
			
			System.out.println(testManager.facebookIMGroupConversationTest());
			
			System.out.println(testManager.facebookStatusTest());
			
			System.out.println(testManager.facebookWallPostTest());
		}
		catch (FacebookException e)
		{
			e.printStackTrace();
		}
		catch (TwitterException e)
		{
			e.printStackTrace();
		}

	}

}
