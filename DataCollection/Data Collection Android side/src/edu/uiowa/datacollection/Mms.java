package edu.uiowa.datacollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * MMS returns all mms messages in the inbox and outbox
 * 
 */
public class Mms
{
	/*
	 * A lot like sms just mms are stored in multiple parts in multiple tables.
	 * Frist the inbox and outbox are searched to get all messages id's. These
	 * Id's are then used in the address and body tables to get all needed
	 * information.
	 */
	public List<MessageU> getMms(Context context, User theUser)
	{
		String user = theUser.getUser();
		String lastMessage = null;
		List<MessageU> messageList = new ArrayList<MessageU>();
		Uri mmsInboxUri = Uri.parse("content://mms");
		String[] projection = new String[] { "_id", "msg_box", "ct_t", "date" };
		Cursor mmsInboxCursor = context.getContentResolver().query(mmsInboxUri,
				projection, "msg_box=1 or msg_box=2", null, null);
		if (mmsInboxCursor != null)
		{
			try
			{
				if (mmsInboxCursor.moveToFirst())
				{
					int id;
					long Date;
					Date date;
					String ID;
					String partId;
					String type;
					String body = null;
					do
					{
						MessageU msgS = new MessageU();
						id = mmsInboxCursor.getInt(0);
						Date = (mmsInboxCursor.getLong(3) * 1000);
						date = new Date(Date);
						ID = Integer.toString(id);
						msgS.setCreateTime(date);
						msgS.setmID(ID);
						String selectionPart = "mid=" + ID;
						Uri uri = Uri.parse("content://mms/part");
						Cursor cursor = context.getContentResolver().query(uri,
								null, selectionPart, null, null);
						if (cursor != null)
						{
							if (cursor.moveToFirst())
							{
								do
								{
									partId = cursor.getString(cursor
											.getColumnIndex("_id"));
									type = cursor.getString(cursor
											.getColumnIndex("ct"));
									if ("text/plain".equals(type))
									{
										String data = cursor.getString(cursor
												.getColumnIndex("_data"));
										if (data != null)
										{
											body = getMmsText(context, partId);
										}
										else
										{
											body = cursor.getString(cursor
													.getColumnIndex("text"));
										}
									}
								} while (cursor.moveToNext());
								msgS.setText(body);
							}
						}
						if (cursor != null)
						{
							cursor.close();
							cursor = null;
						}
						getMMSAddress(context, ID, msgS, user);
						if (!(msgS.getDestiPIDList().get(0).equals("Draft"))
								&& !(msgS.getText().equals(lastMessage)))
						{
							lastMessage = msgS.getText();
							messageList.add(msgS);
						}
					} while (mmsInboxCursor.moveToNext());

				}
			}
			catch (Exception e)
			{

				System.out
						.println("MMSMonitor :: startMMSMonitoring Exception== "
								+ e.getMessage());
			}
			if (mmsInboxCursor != null)
			{
				mmsInboxCursor.close();
				mmsInboxCursor = null;
			}
		}
		return messageList;
	}

	public void getMMSAddress(Context context, String id, MessageU msgS,
			String user)
	{
		String addrSelection = "msg_id=" + id;
		String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
		Uri uriAddress = Uri.parse(uriStr);
		String[] columns = { "address" };
		Cursor cursor = context.getContentResolver().query(uriAddress, columns,
				addrSelection, null, null);
		List<String> address = new ArrayList<String>();
		String val;
		if (cursor.moveToFirst())
		{
			do
			{
				val = cursor.getString(cursor.getColumnIndex("address"));
				if (val != null)
				{
					if (!(val.equals("insert-address-token")))
					{
						val = val.replaceAll("[^0-9]", "");
					}
					if ((val.length() == 11) && val.startsWith("1"))
					{
						val = val.substring(1);
					}
					address.add(val);
				}
			} while (cursor.moveToNext());
		}
		if (cursor != null)
		{
			cursor.close();
		}
		if (address.contains("insert-address-token"))
		{// insert-address-token
			// is used as an address
			// for messages you sent
			msgS.setSourcePID(user);
			address.remove("insert-address-token");
			msgS.setDestiPIDList(address);
		}
		else
		{
			msgS.setSourcePID(address.get(0));
			address.remove(0);
			if (address.size() == 0)
			{// if there is only one address then the
				// message way never sent
				address.add("Draft");
			}
			msgS.setDestiPIDList(address);
		}
	}

	private String getMmsText(Context context, String id)
	{
		Uri partURI = Uri.parse("content://mms/part/" + id);
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		try
		{
			is = context.getContentResolver().openInputStream(partURI);
			if (is != null)
			{
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader reader = new BufferedReader(isr);
				String temp = reader.readLine();
				while (temp != null)
				{
					sb.append(temp);
					temp = reader.readLine();
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("MMSMonitor :: startMMSMonitoring Exception== "
					+ e.getMessage());
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					Log.i("debug", "cursor closing error");
				}
			}
		}
		return sb.toString();
	}

}
