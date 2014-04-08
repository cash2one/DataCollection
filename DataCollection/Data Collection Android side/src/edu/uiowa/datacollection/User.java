package edu.uiowa.datacollection;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

/**
 * phone number of the phone the app is on. The number is set in the user
 * creation app.
 * 
 */
public class User
{
	private String user;

	public User(Context context)
	{
		String user = null;
		String filename = "phoneNumber";
		FileInputStream InputStream;

		try
		{
			InputStream = context.openFileInput(filename);
			InputStreamReader inputStreamReader = new InputStreamReader(
					InputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			user = bufferedReader.readLine();
			InputStream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Log.i("test", user);
		this.user = user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getUser()
	{
		return this.user;
	}

}
