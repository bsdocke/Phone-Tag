package fitnessapps.spacerayders.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import fitnessapps.spacerayders.data.GlobalState;

public class SettingsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.admin_settings);
	}
	
	@Override
	public void onStart(){
		super.onStart();
	}
	
	public void onSaveClick(View view){
		CheckBox silver = (CheckBox)findViewById(R.id.checkSilver);
		CheckBox blue = (CheckBox)findViewById(R.id.checkBlue);
		CheckBox red = (CheckBox)findViewById(R.id.checkRed);
		CheckBox black = (CheckBox)findViewById(R.id.checkBlack);
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
		if(green.isChecked()){
			GlobalState.currentPlayers.add(green.getText().toString());
		}
		
		Toast.makeText(this, "You are " + name, Toast.LENGTH_LONG).show();
	}
}