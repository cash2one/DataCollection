package tests;

import java.util.Scanner;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONException;

public class Main
{
	public static void main(String[] args) throws JSONException
	{
//		Scanner scan = new Scanner(System.in);
//		System.out.print("Enter your access token: ");
//		String accessToken = scan.nextLine();
//		scan.close();
//
//		AccessToken token = new AccessToken(accessToken, null);

		Facebook session = new FacebookFactory().getInstance();
		session.setOAuthAppId("442864129167674",
				"f2140fbb0148c5db21db0d07b92e6ade");
//		session.setOAuthAccessToken(token);

		TestManager testManager = new TestManager(session);
		try
		{
			//This can't be tested without email
			System.out.println(testManager.imConversationTest());
			
//			System.out.println(testManager.statusTest());
			
			//This cannot be tested without real accounts and human interaction
			//because Facebook does not allow apps to post on friend's walls
			//without a dialog.
//			System.out.println(testManager.wallPostTest());
		}
		catch (FacebookException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
