package edu.uiowa.datacollection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

/**
 * SMS returns all sms messages in outbox and inbox
 * 
 */

public class Sms
{
	/*
	 * Directly queries the sqlite tables where sms messages are saved. From
	 * these tables it gets: messages ID = unquie number given to each message,
	 * address = phone number of the person the message it to or from. Not the
	 * number of the phone this is running person = key to look up the person in
	 * the users contact tables body = text of the message data = data it was
	 * received or sent type = sent or received
	 */
	public List<MessageU> getSms(Context context, User theUser)
	{
		final String SMS_URI_ALL = "content://sms/";
		String user = theUser.getUser();
		List<MessageU> messageList = new ArrayList<MessageU>();
		try
		{
			Uri uri = Uri.parse(SMS_URI_ALL);
			String[] projection = new String[] { "_id", "address", "person",
					"body", "date", "type" };
			Cursor cur = context.getContentResolver().query(uri, projection,
					null, null, "date desc");

			if (cur.moveToFirst())
			{
				int index_id = cur.getColumnIndex("_id");
				int index_Address = cur.getColumnIndex("address");
				int index_Body = cur.getColumnIndex("body");
				int index_Date = cur.getColumnIndex("date");
				int index_Type = cur.getColumnIndex("type");
				String mID;
				String Address;
				String Body;
				long Date;
				int Type;
				Date date;

				do
				{
					mID = cur.getString(index_id);
					Address = cur.getString(index_Address);
					Body = cur.getString(index_Body);
					Date = cur.getLong(index_Date);
					date = new Date(Date);
					Type = cur.getInt(index_Type);
					if (Address != null)
					{
						// remove non numbers and 1 in front of area code
						Address = Address.replaceAll("[^0-9]", "");
						if ((Address.length() == 11) && Address.startsWith("1"))
						{
							Address = Address.substring(1);
						}
					}

					if (Type == 1)
					{// received
						List<String> destiList = new ArrayList<String>();
						destiList.add(user);
						MessageU msgR = new MessageU(mID, Address, destiList,
								Body, date);
						messageList.add(msgR);
					}
					else if (Type == 2)
					{// sent
						List<String> destiList = new ArrayList<String>();
						destiList.add(Address);
						MessageU msgS = new MessageU(mID, user, destiList,
								Body, date);
						messageList.add(msgS);
					}
				} while (cur.moveToNext());

				if (cur != null)
				{
					cur.close();
					cur = null;
				}
			}

		}
		catch (SQLiteException ex)
		{
			Log.d("SQLiteException in getSmsInPhone", ex.getMessage());
		}

		return messageList;
	}

}
