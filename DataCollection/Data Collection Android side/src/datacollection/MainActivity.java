package datacollection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.example.messagesiphon.R;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.SessionState;

/**
 * The purpose of this class is to handle the main operations of the app as well
 * as the user interface and authenticating with Facebook. It handles user input
 * and collects data with a {@link datacollection.DataManager}.
 * @author Tom
 */
public class MainActivity extends Activity
{
	/**
	 * Button to start gathering data
	 */
	private Button goButton;

	/**
	 * Checkbox to toggle collecting Facebook messages
	 */
	private CheckBox collectMessagesCheckbox;

	/**
	 * Checkbox to toggle collecting stream data
	 */
	private CheckBox collectStreamCheckbox;

	/**
	 * Checkbox to toggle loading previously collected data
	 */
	private CheckBox loadOldDataCheckbox;

	/**
	 * Checkbox to toggle whether Facebook messages older than a month should
	 * be collected
	 */
	private CheckBox limitToMonthCheckbox;

	/**
	 * Button to save collected data
	 */
	private Button saveButton;

	/**
	 * Button to delete data file
	 */
	private Button deleteButton;

	/**
	 * Button to load the Facebook ID to name pairings
	 */
	private Button showParticipantsButton;
	
	/**
	 * Button to show the message display on the right side of the screen
	 */
	private Button showMessageDisplayButton;

	/**
	 * Button to show the stream display on the right side of the screen
	 */
	private Button showStreamDisplayButton;
	
	/**
	 * Button to reset the app to how it was on launch
	 */
	private Button resetAppButton;

	/**
	 * The DataManager handles all of the loading and storing of the data,
	 * and gives the MainActivity handles to access it
	 */
	private DataManager dataManager;
	
	
	/**
	 * This method initializes all of the pieces of the app - the dataManager,
	 * the Facebook session, and the user interface.
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Get our session initialized
		openFacebookSession();
		
		//Create out dataManager, giving it our initialized session
		dataManager = new DataManager(Session.getActiveSession());
		
		//Create the user interface
		setupUI();
	}
	
	/**
	 * All of the app's UI initialization goes here, it also resets all of the
	 * UI elements
	 */
	private void setupUI()
	{		
		collectMessagesCheckbox = (CheckBox) findViewById(R.id.loadMessageCheckbox);
		collectMessagesCheckbox.setChecked(false);
		
		collectStreamCheckbox = (CheckBox) findViewById(R.id.loadActivityCheckBox);
		collectStreamCheckbox.setChecked(false);
		
		loadOldDataCheckbox = (CheckBox) findViewById(R.id.loadDataCheckBox);
		loadOldDataCheckbox.setChecked(false);
		
		limitToMonthCheckbox = (CheckBox) findViewById(R.id.restrictMonthCheckbox);
		limitToMonthCheckbox.setChecked(false);
		
		goButton = (Button) findViewById(R.id.loadDataButton);
        goButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dataManager.collectData(collectMessagesCheckbox.isChecked(),
							limitToMonthCheckbox.isChecked(),
							collectStreamCheckbox.isChecked(),
							loadOldDataCheckbox.isChecked());
				
				if (collectMessagesCheckbox.isChecked() || 
					loadOldDataCheckbox.isChecked())
				{
					setupConversationDisplay();
				}
				else
				{
					System.out.println("setting up stream display");
					setupStreamDisplay();
				}
			}
		});
        
        saveButton = (Button) findViewById(R.id.saveDataButton);
        saveButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dataManager.saveJSONData();
			}
		});
        
        deleteButton = (Button) findViewById(R.id.deleteDataButton);
        deleteButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				dataManager.deleteOldData();
			}
		});
        
        showParticipantsButton = (Button) findViewById(R.id.showParticipantsButton);
        showParticipantsButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				dataManager.collectParticipantInformation();
				setupConversationDisplay();
			}
		});
        
        showMessageDisplayButton = (Button) findViewById(R.id.showMessagesButton);
        showMessageDisplayButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				setupConversationDisplay();
			}
		});
        
        showStreamDisplayButton = (Button) findViewById(R.id.showActivityButton);
        showStreamDisplayButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				setupStreamDisplay();
			}
		});
        
        resetAppButton = (Button) findViewById(R.id.resetButton);
        resetAppButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				//Create out dataManager, giving it our initialized session
				dataManager = new DataManager(Session.getActiveSession());
				
				//Create the user interface
				setupUI();
				
				final ListView postList = (ListView) findViewById(R.id.itemSelect);
				postList.setAdapter(null);
				final ListView postDetails = (ListView) findViewById(R.id.itemView);
				postDetails.setAdapter(null);
			}
		});
	}

	/**
	 * This method sets up our facebook connection.
	 */
	private void openFacebookSession()
	{
		//Call this method if there is an authentication problem, it was only
		//needed the first time getting the app authenticated with facebook,
		//and remains for debugging purposes.
//		getKeyIfKeyWrong();
		
		// start Facebook Login
		Session.openActiveSession(this, true, new Session.StatusCallback()
		{
			// callback when session changes state
			@Override
			public void call(Session session, SessionState state,
					Exception exception)
			{
				if (session.isOpened())
				{
					ArrayList<String> permissions = new ArrayList<String>();
					permissions.add("read_mailbox");
					permissions.add("read_stream");
					
					// Remove callback from old session to prevent infinite loop
					session.removeCallback(this); 
					
					//send our permissions request
					Session.getActiveSession().requestNewReadPermissions(new NewPermissionsRequest(MainActivity.this, permissions));
				}
			}
		});
		
	}

	/**
	 * This method fixes some app authentication errors when run for the first
	 * time before the app is published. 
	 */
	@SuppressWarnings("unused")
	private void getKeyIfKeyWrong()
	{
		PackageInfo info = null;
		try
		{
			info = getPackageManager().getPackageInfo("com.example.messagesiphon",  PackageManager.GET_SIGNATURES);
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
	        Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	    }
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	/**
	 * This method is called to initialize the itemSelect and itemView UI
	 * pieces, the streamList is put in the itemSelect, and the itemView
	 * is configured so that when a stream object is clicked on the appropriate
	 * data is displayed.
	 */
	private void setupStreamDisplay()
	{
		final ListView postList = (ListView) findViewById(R.id.itemSelect);
		final ListView postDetails = (ListView) findViewById(R.id.itemView);
		final ArrayList<StreamObject> streamObjects = dataManager.getStreamObjects();
		
	    final ArrayList<String> list = new ArrayList<String>();
	    for (int i = 0; i < streamObjects.size(); ++i)
	    	list.add(streamObjects.get(i).getPostID());

	    final StableArrayAdapter adapter = new StableArrayAdapter(this,
	        android.R.layout.simple_list_item_1, list);
	    
	    postList.setAdapter(adapter);
	    
	    postList.setOnItemClickListener(new AdapterView.OnItemClickListener() 
	    {
	      @Override
	      public void onItemClick(AdapterView<?> parent, final View view,
	          int position, long id) 
	      {
	    	  final ArrayList<String> lines = new ArrayList<String>();
	    	  for (String line : streamObjects.get(position).toString().split("\n"))
	    		  lines.add(line);
	    	  
	    	  final StableArrayAdapter adapter2 = new StableArrayAdapter(MainActivity.this,
	  		        android.R.layout.simple_list_item_1, lines);
	    	  postDetails.setAdapter(adapter2);
	      }
	    });
	    
	    postList.setSelection(0);
	}
	
	/**
	 * This method is called to initialize the itemSelect and itemView UI
	 * pieces, the conversation list is put in the itemSelect, and the itemView
	 * is configured so that when a conversation is clicked on the appropriate
	 * messages are displayed.
	 */
	private void setupConversationDisplay()
	{
		final ListView convoList = (ListView) findViewById(R.id.itemSelect);
		final ListView messageList = (ListView) findViewById(R.id.itemView);
		final ArrayList<Conversation> conversations = dataManager.getConversations();
		
	    final ArrayList<String> list = new ArrayList<String>();
	    for (int i = 0; i < conversations.size(); ++i)
	    	list.add(conversations.get(i).getParticipantString());

	    final StableArrayAdapter adapter = new StableArrayAdapter(this,
	        android.R.layout.simple_list_item_1, list);
	    
	    convoList.setAdapter(adapter);
	    
	    convoList.setOnItemClickListener(new AdapterView.OnItemClickListener() 
	    {
	      @Override
	      public void onItemClick(AdapterView<?> parent, final View view,
	          int position, long id) 
	      {
	    	  final ArrayList<String> mess = new ArrayList<String>();
	    	  ArrayList<Message> messages = conversations.get(position).getMessages();
	    	  for (int i = 0; i < messages.size(); i++)
	    		  mess.add(messages.get(i).text());
	    	  
	    	  final StableArrayAdapter adapter2 = new StableArrayAdapter(MainActivity.this,
	  		        android.R.layout.simple_list_item_1, mess);
	    	  messageList.setAdapter(adapter2);
	    	  
	    	  messageList.setSelection(messageList.getCount() - 1);
	      }
	    });
	    convoList.setSelection(0);
	}
	
	/**
	 * Private class used for the list display
	 */
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }

}