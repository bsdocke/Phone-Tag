package com.android.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.data.GlobalState;
import com.fitnessapps.spacerayders.R;

public class SettingsActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_settings);
	}
	
	public void onStart(){
		super.onStart();
	}
	
	public void onSaveClick(View view){
		CheckBox silver = (CheckBox)findViewById(R.id.checkSilver);
		CheckBox blue = (CheckBox)findViewById(R.id.checkBlue);
		CheckBox red = (CheckBox)findViewById(R.id.checkRed);
		CheckBox black = (CheckBox)findViewById(R.id.checkBlack);
		CheckBox pink = (CheckBox)findViewById(R.id.checkPink);
		CheckBox green = (CheckBox)findViewById(R.id.checkGreen);
		
		GlobalState.currentPlayers = new ArrayList<String>();
		Spinner adaptName = (Spinner) findViewById(R.id.spinner1);
		String name = (String)adaptName.getSelectedItem();
		GlobalState.playerName = name;
		BluetoothAdapter.getDefaultAdapter().setName(name);
		
		if(silver.isChecked()){
			GlobalState.currentPlayers.add(silver.getText().toString());
		}
		if(blue.isChecked()){
			GlobalState.currentPlayers.add(blue.getText().toString());
		}
		if(red.isChecked()){
			GlobalState.currentPlayers.add(red.getText().toString());
		}
		if(black.isChecked()){
			GlobalState.currentPlayers.add(black.getText().toString());
		}
		if(pink.isChecked()){
			GlobalState.currentPlayers.add(pink.getText().toString());
		}
		if(green.isChecked()){
			GlobalState.currentPlayers.add(green.getText().toString());
		}
		
		Toast.makeText(this, BluetoothAdapter.getDefaultAdapter().getName() + " is you", Toast.LENGTH_LONG).show();
	}
}