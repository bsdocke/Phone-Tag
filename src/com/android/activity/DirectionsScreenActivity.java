package com.android.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class DirectionsScreenActivity extends Activity {
	
	private String originalDeviceName;
	private String playerName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directions);

		extractExtraNameData();
	}
	
	public void startSyncActivity(View view){
		setAdapterName();
		
		Intent loadGameSelection = createAndPopulateIntent();
		startActivity(loadGameSelection);
	}
	
	private Intent createAndPopulateIntent(){
		Intent loadGameSelection = new Intent(this, UserGameSelectActivity.class);
		loadGameSelection.putExtra("ORIGINAL_DEVICE_NAME", originalDeviceName);
		
		return loadGameSelection;
	}
	
	private void setAdapterName(){
		BluetoothAdapter.getDefaultAdapter().setName(playerName);
	}
	
	private void extractExtraNameData(){
		originalDeviceName = getIntent().getStringExtra("ORIGINAL_DEVICE_NAME");
		playerName = getIntent().getStringExtra("USER_ID");
	}
}
