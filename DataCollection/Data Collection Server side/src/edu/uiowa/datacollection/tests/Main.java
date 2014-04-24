package edu.uiowa.datacollection.tests;

import java.util.Scanner;

import twitter4j.TwitterException;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONException;

public class Main
{
	public static void main(String[] args)
	{
		runTests();
	}

	private static boolean runTest(String testName)
	{
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		System.out.print("Run " + testName + "? (y/n) : ");
		return (scan.next().toUpperCase().charAt(0) == 'Y');
	}

	private static void runTests()
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
			//"A user access token is required to request this resource."
			if (e.getErrorCode() == 102)
			{
				
			}
		}
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TwitterException e)
		{
			// rate Limit exceed
			if (e.getStatusCode() == 429)
			{
				System.out.println("***** ERROR: Rate limit exceeded. " + 
						"Please wait before running more twitter tests.");
			}
			else
				e.printStackTrace();
		}
	}
}
