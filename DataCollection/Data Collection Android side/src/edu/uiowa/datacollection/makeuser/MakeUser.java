package edu.uiowa.datacollection.makeuser;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.SessionState;

import edu.uiowa.datacollection.R;

/**
 * The purpose of this class is to handle the main operations of the app as well
 * as the user interface and authenticating with Facebook. It handles user input
 * and collects data with a {@link datacollection.DataManager}.
 * 
 * @author Tom
 */
public class MakeUser extends Activity
{
	
	private static final String SERVER_URL = "http://128.255.45.52:7777/server/makeuser/";
	private Button loginToFacebook;
	private Button loginToTwitter;
	private String oauthText;
	private String oauthSecretText;
	private String screenNameText;
	private Button done;
	private SharedPreferences sharedPreferences;
	private EditText phoneField;
	private TextView phoneLabel;
	private String twitterID;

	/**
	 * This method initializes all of the pieces of the app - the dataManager,
	 * the Facebook session, and the user interface.
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("test","makeUser");
		// Create the user interface
		setupUI();

		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (!sharedPreferences.getBoolean(
				ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, false))
		{
			initControl();
		}

	}

	/**
	 * All of the app's UI initialization goes here, it also resets all of the
	 * UI elements
	 */
	private void setupUI()
	{
		loginToFacebook = (Button) findViewById(R.id.loginToFacebookButton);
		loginToFacebook.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				openFacebookSession();
			}
		});

		loginToTwitter = (Button) findViewById(R.id.loginToTwitterButton);
		loginToTwitter.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.remove(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN);
				editor.remove(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET);
				editor.remove(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN);
				editor.commit();
				openTwitterSession();
			}
		});

		done = (Button) findViewById(R.id.doneButton);
		done.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				savePhoneNumber();
				uploadData();
			}
		});

		phoneField = (EditText) findViewById(R.id.phoneInput);
		phoneLabel = (TextView) findViewById(R.id.phoneLabel);
	}

	protected void savePhoneNumber()
	{

		String filename = "phoneNumber";
		FileOutputStream outputStream;

		try {
		  outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
		  outputStream.write(phoneField.getText().toString().getBytes());
		  outputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		}
	}

	private void uploadData()
	{
		if (Session.getActiveSession() == null || oauthText.length() == 0
				|| oauthSecretText.length() == 0
				|| screenNameText.length() == 0
				|| phoneField.getText().toString().length() == 0)
		{
			Toast.makeText(this, "Complete logins please", Toast.LENGTH_LONG)
					.show();
			return;
		}

		JSONObject obj = new JSONObject();
		try
		{
			obj.put("phone_number", phoneField.getText().toString());
			obj.put("facebook_token", Session.getActiveSession()
					.getAccessToken());
			obj.put("facebook_appid", Session.getActiveSession()
					.getApplicationId());
			obj.put("twitter_token", this.oauthText);
			obj.put("twitter_secret", this.oauthSecretText);
			obj.put("twitter_screen_name", this.screenNameText);
			obj.put("twitter_id", twitterID);
			System.out.println(obj.toString(1));

			AsyncTask<JSONObject, Void, JSONObject> postData = new AsyncTask<JSONObject, Void, JSONObject>()
			{
				protected JSONObject doInBackground(JSONObject... params)
				{
					HttpPost post = new HttpPost(SERVER_URL);
					post.setEntity(new ByteArrayEntity(params[0].toString()
							.getBytes()));
					HttpResponse resp = null;
					HttpClient httpclient = new DefaultHttpClient();
					try
					{
						resp = httpclient.execute(post);
						return readJson(resp);
					}
					catch (ClientProtocolException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
					return null;
				}

			};
			JSONObject resp = postData.execute(obj).get();
			System.out.println(resp);
			Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}

	}

	private void openTwitterSession()
	{
		new TwitterAuthenticateTask().execute();
	}

	/**
	 * This method sets up our facebook connection.
	 */
	private void openFacebookSession()
	{
		// Call this method if there is an authentication problem, it was only
		// needed the first time getting the app authenticated with facebook,
		// and remains for debugging purposes.
		 getKeyIfKeyWrong();

		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback()
		{
			// callback when session changes state
			@Override
			public void call(Session session, SessionState state,
					Exception exception)
			{
				System.out.println(session.isOpened());
				if (session.isOpened())
				{
					ArrayList<String> permissions = new ArrayList<String>();
					permissions.add("read_mailbox");
					permissions.add("read_stream");

					// Remove callback from old session to prevent infinite loop
					session.removeCallback(this);

					// send our permissions request
					Session.getActiveSession().requestNewReadPermissions(
							new NewPermissionsRequest(MakeUser.this,
									permissions));
					loginToFacebook.setEnabled(false);
					loginToTwitter.setEnabled(true);
				}
			}
		});
	}

	/**
	 * This method fixes some app authentication errors when run for the first
	 * time before the app is published.
	 */
	private void getKeyIfKeyWrong()
	{
		PackageInfo info = null;
		try
		{
			info = getPackageManager().getPackageInfo(
					"com.example.messagesiphon", PackageManager.GET_SIGNATURES);
		}
		catch (NameNotFoundException e1)
		{
			Log.i("ERROR:", "Couldn't make info");
		}

		for (Signature signature : info.signatures)
		{
			MessageDigest md = null;
			try
			{
				md = MessageDigest.getInstance("SHA");
			}
			catch (NoSuchAlgorithmException e)
			{
				Log.i("ERROR:", "Couldn't make md");
			}
			md.update(signature.toByteArray());
			Log.i("KeyHash:",
					Base64.encodeToString(md.digest(), Base64.DEFAULT));
		}
	}

	private void initControl()
	{
		Uri uri = getIntent().getData();
		if (uri != null
				&& uri.toString().startsWith(
						ConstantValues.TWITTER_CALLBACK_URL))
		{
			String verifier = uri
					.getQueryParameter(ConstantValues.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
			TwitterGetAccessTokenTask t = new TwitterGetAccessTokenTask();
			t.execute(verifier);

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	class TwitterAuthenticateTask extends
			AsyncTask<String, String, RequestToken>
	{

		@Override
		protected void onPostExecute(RequestToken requestToken)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(requestToken.getAuthenticationURL()));
			startActivity(intent);
		}

		@Override
		protected RequestToken doInBackground(String... params)
		{
			return TwitterUtil.getInstance().getRequestToken();
		}
	}

	class TwitterGetAccessTokenTask extends
			AsyncTask<String, String, AccessToken>
	{

		@Override
		protected void onPostExecute(AccessToken accessToken)
		{
			if (accessToken == null)
			{
				oauthText = "";
				oauthSecretText = "";
				screenNameText = "";
				twitterID = "";

				System.out.println("Error with first attempt, trying again.");
				openTwitterSession();
			}
			else
			{
				oauthText = (accessToken.getToken());
				oauthSecretText = (accessToken.getTokenSecret());
				screenNameText = (accessToken.getScreenName());
				twitterID = "" + accessToken.getUserId();

				loginToFacebook.setEnabled(false);
				loginToTwitter.setEnabled(false);
				phoneField.setEnabled(true);
				phoneLabel.setEnabled(true);
			}
		}

		@Override
		protected AccessToken doInBackground(String... params)
		{
			AccessToken accessToken = TwitterUtil.getInstance().getAccessToken(
					params[0]);
			accessToken = TwitterUtil.getInstance().getAccessToken(params[0]);
			if (accessToken == null)
			{
				return null;
			}
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN,
					accessToken.getToken());
			editor.putString(
					ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET,
					accessToken.getTokenSecret());
			// editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,
			// true);
			editor.commit();
			return accessToken;
		}
	}

	private String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1)
		{
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private JSONObject readJson(HttpResponse resp) throws IOException,
			JSONException
	{
		InputStream is = resp.getEntity().getContent();
		try
		{
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			System.out.println(jsonText);
			JSONObject json = new JSONObject(jsonText);
			return json;
		}
		finally
		{
			is.close();
		}
	}
}