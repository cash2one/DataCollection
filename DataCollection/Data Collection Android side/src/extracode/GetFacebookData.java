package extracode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import android.os.AsyncTask;


public class GetFacebookData extends AsyncTask<String, Void, ArrayList<UserActivity>>
{
	/**
	 * Empty constructor
	 */
	public GetFacebookData()
	{

	}

	@Override
	/**
	 * @param arg0 The email of the user
	 * @param arg1 The password of the user
	 * @return The list of user activities
	 */
	protected ArrayList<UserActivity> doInBackground(String... arg0)
	{
		DefaultHttpClient httpclient = loginToFacebook(arg0[0], arg0[1]);
		
		ArrayList<UserActivity> userActivities = new ArrayList<UserActivity>();
		
		//Get user posts
		Elements userPosts = getUserPosts(httpclient);
		
		for (int i = 0; i < userPosts.size(); i++)
		{
			//Oddly enough, the presence of _k3z indicates it is a tbody html
			//that contains a user activity (its the timestamp link).
			if (userPosts.get(i).toString().contains("_k3z"))
				userActivities.add(new UserPost(userPosts.get(i)));
		}
		
		
		//Get user comments
		Elements userComments = getUserComments(httpclient);
		
		
		for (int i = 0; i < userComments.size(); i++)
			if (userComments.get(i).toString().contains("_k3z"))
				userActivities.add(new UserComment(userComments.get(i)));
		
		//Shutdown the Facebook connection
		httpclient.getConnectionManager().shutdown();
		return userActivities;
	}
	
	private Elements getUserComments(DefaultHttpClient httpclient)
	{
		//The html for "your comments"
		HttpGet httpget = new HttpGet("http://www.facebook.com/me/allactivity?privacy_source=activity_log&log_filter=cluster_116");
		String html = null;
		try
		{
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity);
			
			if (entity != null) {
			    entity.consumeContent();
			}
		}
		catch (ClientProtocolException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getStories(html);
	}
	
	private Elements getUserPosts(DefaultHttpClient httpclient)
	{
		//The html for "your posts"
		HttpGet httpget = new HttpGet("http://www.facebook.com/me/allactivity?privacy_source=activity_log&log_filter=cluster_11");
		String html = null;
		try
		{
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity);
			
			if (entity != null) {
			    entity.consumeContent();
			}
		}
		catch (ClientProtocolException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getStories(html);
	}

	public Elements getStories(String html)
	{
		Document d = Jsoup.parse(html);
		
		final StringBuffer commentData = new StringBuffer();
		d.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                if (node instanceof Comment) {
                    Comment comment = (Comment) node;
                    commentData.append(comment.getData());
                }
            }
            public void tail(Node node, int depth) {
            }
        });
		
		String data = commentData.toString();
		data = data.replace("&quot;", "\"");
		data = data.replace("&amp;", "&");
		data = data.replace("&lt;", "<");
		data = data.replace("&gt;", ">");
		
		Document logHTML = Jsoup.parse(data);		
		
		return logHTML.select("tbody");
	}
	
	/**
	 * This method sets up an HTTP connection to Facebook and logs in using
	 * the given username and password.
	 * @param email The email address of the user
	 * @param password The password of the user
	 * @return The HttpClient that is logged into Facebook.
	 */
	public DefaultHttpClient loginToFacebook(String email, String password)
	{
		try
		{		
			DefaultHttpClient httpclient = new DefaultHttpClient();

			HttpGet httpget = new HttpGet("http://www.facebook.com/login.php");

			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
			    entity.consumeContent();
			}

			HttpPost httpost = new HttpPost("http://www.facebook.com/login.php");

			List <NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("email", email));
			nvps.add(new BasicNameValuePair("pass", password));

			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			response = httpclient.execute(httpost);
			entity = response.getEntity();

			if (entity != null) {
			    entity.consumeContent();
			}
			
			return httpclient;
		}
		catch (MalformedURLException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		return null;
	}

	/**
	 * Currently unused, but method that is executed before the main body
	 */
	protected void onPreExecute()
	{
		
	}
}
