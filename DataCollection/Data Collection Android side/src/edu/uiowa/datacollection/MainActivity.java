package edu.uiowa.datacollection;

import java.io.File;

import edu.uiowa.datacollection.makeuser.MakeUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
/*
 * First time it gets tokens and saves phone number the next time it runs is message collection.
 * 
 * Working on//
 * It still needs alarm to turn it on and a way to turn it off.
 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		File fileName = this.getFileStreamPath("phoneNumber");
		if(fileName.exists()){
			Intent intent = new Intent(this, MessageService.class);
			startService(intent);
			finish();
		}
		else{
			Intent intent = new Intent(this, MakeUser.class);
			startActivity(intent);
		}
		
		
	}
}
