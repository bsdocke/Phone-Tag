package com.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class CreateChoosePlayerNameActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_teamsetup);
	}
	
	public void onCancel(View view){
		finish();
	}
	
	public void onOkHandler(View view){
		EditText inputField = (EditText) findViewById(R.id.playerNameField);
    	String userID = inputField.getText().toString();
    	
    	if(userID.length() == 0){
    		AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
    		alertBox.setMessage("You must enter a valid user ID");
    		
    		alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener(){
    			public void onClick(DialogInterface dialogInterface, int arg1){
    				dialogInterface.cancel();
    			}
    		});
    		
    		alertBox.show();
    		
    		return;
    	}
    	
    	BluetoothAdapter.getDefaultAdapter().setName(userID);
    	
    	Intent loadGameNameActivity = new Intent(this, GameNameSetupActivity.class);
    	loadGameNameActivity.putExtra("PLAYER_NAME", userID);
    	startActivity(loadGameNameActivity);
	}

}
