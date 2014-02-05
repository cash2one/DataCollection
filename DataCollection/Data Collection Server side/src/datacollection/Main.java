package datacollection;

import java.util.Scanner;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONException;

public class Main
{
	public static void main(String[] args) throws JSONException, FacebookException
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter your access token: ");
		String accessToken = scan.nextLine();
		DataManager manager = new DataManager(accessToken);
		
		manager.collectData(false, // Collect message 
					  		false, //Limit to one month
					  		true, //Collect stream
					  		false); //Load old data
		
		for (StreamObject so : manager.getStreamObjects())
		{
			Facebook session = manager.getSession();
			System.out.println(session.executeFQL("SELECT fromid, text, id, username FROM comment WHERE post_id = \"" + so.getPostID() + "\"").toString(1));
		}
		
		
		
		
		scan.close();
	}
}
