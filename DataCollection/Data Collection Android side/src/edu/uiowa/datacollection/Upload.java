package edu.uiowa.datacollection;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Uploads json to server
 * 
 */
public class Upload
{
	private byte[] data;

	public Upload(JSONObject json)
	{
		this.data = json.toString().getBytes();
	}

	public String post()
	{
		HttpPost post = new HttpPost(
				"http://128.255.45.52:7777/server/postandroid/");
		post.setEntity(new ByteArrayEntity(data));
		HttpResponse resp = null;
		HttpClient httpclient = new DefaultHttpClient();
		try
		{
			resp = httpclient.execute(post);
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return resp.toString();
	}
}
