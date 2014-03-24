package edu.uiowa.datacollection.tests;

import java.io.IOException;

import edu.uiowa.datacollection.util.JsonHelper;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

@SuppressWarnings("resource")
public class SMSTestManager
{
	public SMSTestManager()
	{
		
	}
	
	public TestResult smsConversationTest()
	{
		TestResult result = new TestResult("SMS Conversation Test", System.out);
		
		openLink("http://www.pinger.com/tfw/");
		
		System.out.println("Login to Pinger");
		System.out.println("Username: AbbyDoeTest1");
		System.out.println("Password: 1qaz1qaz");
		System.out.println();
		
		System.out.println("Send the following message to the number of the test phone.");
		String message1 = "Test Message 1";
		System.out.println("From Computer: " + message1);
		String message2 = "Test Message 2";
		System.out.println("Send the following reply from the phone.");
		System.out.println("From phone: " + message2);
		String message3 = "Test Message 3";
		System.out.println("From Computer: " + message3);
		String message4 = "Test Message 4";
		System.out.println("From phone: " + message4);
		
		//TODO: Modify to SMS url
		System.out.println();
		System.out.println("Run the SMS collection program");
		String SERVER_URL = "http://128.255.45.52:7777/server/getfacetoken/";
		JSONObject obj;
		try
		{
			obj = JsonHelper.readJsonFromUrl(SERVER_URL);
			
			JSONArray users = obj.getJSONArray("data");
		}
		catch (IOException | JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return result;
	}
	
	

	
	/**
	 * This function opens the given url in the user's default browser. If it
	 * cannot, it prints the link to open.
	 * 
	 * @param link
	 *            The link to open.
	 */
	private void openLink(String link)
	{
		try
		{
			java.awt.Desktop.getDesktop().browse(java.net.URI.create(link));
		}
		catch (IOException e)
		{
			System.out.println("Opening browser failed. Please open link: "
					+ link);
		}
	}
}
