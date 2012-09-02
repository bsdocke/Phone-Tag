package com.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SetJoinNameActivity extends Activity {
	public final Activity thisActivity = this;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_teamsetup);
	}

	public void onOkHandler(View view) {
		
		String userID = getNameFieldText();
    	
    	if(!nameIsEntered()){
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
    	
    	Intent loadDirections = createIntentWithDeviceName(userID);
    	startActivity(loadDirections);
	}
	
	private String getNameFieldText(){
		EditText inputField = (EditText) findViewById(R.id.playerNameField);
		return inputField.getText().toString();
	}
	
	private boolean nameIsEntered(){
		return getNameFieldText().length() > 0;
	}
	
	private Intent createIntentWithDeviceName(String userID){
		Intent loadDirections = new Intent(this, DirectionsScreenActivity.class);
		loadDirections.putExtra("USER_ID", userID);
    	loadDirections.putExtra("ORIGINAL_DEVICE_NAME", BluetoothAdapter.getDefaultAdapter().getName());
    	
    	return loadDirections;	
	}
	
	public void onCancel(View view){
		finish();
	}
}
