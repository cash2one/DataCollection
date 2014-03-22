package edu.uiowa.datacollection.tests;

import java.util.Scanner;

import twitter4j.TwitterException;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONException;

public class Main
{
	public static void main(String[] args) throws JSONException
	{
		FacebookTestManager fbTests = new FacebookTestManager();
		TwitterTestManager twTests = new TwitterTestManager();
		try
		{
			if (runTest("Twitter Retweet Test"))
				System.out.println(twTests.twitterRetweetTest());
			
			if (runTest("Twitter Mentions Test"))
				System.out.println(twTests.twitterMentionsTest());
			
			if (runTest("Twitter Status Test"))
				System.out.println(twTests.twitterStatusTest());
			
			if (runTest("Twitter Direct Message Test"))
				System.out.println(twTests.twitterDirectMessageTest());
			
			if (runTest("Facebook IM Conversation Test"))
				System.out.println(fbTests.facebokIMConversationTest());
			
			if (runTest("Facebook IM Group Conversation Test"))
				System.out.println(fbTests.facebookIMGroupConversationTest());
			
			if (runTest("Facebook Status Test"))
				System.out.println(fbTests.facebookStatusTest());
			
			if (runTest("Facebook Wall Post Test"))
				System.out.println(fbTests.facebookWallPostTest());
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

	private static boolean runTest(String testName)
	{
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.print("Run " + testName + "? (y/n) : ");
		return (scan.next().toUpperCase().charAt(0) == 'Y');
	}

}
