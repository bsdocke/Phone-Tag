package com.android.activity;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.android.data.GlobalState;

public class SplashActivity extends BluetoothActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startjoin);
		
		setAdapter();
		getRoot();

		initItOrders();
	}

	public void onStart() {
		super.onStart();
		initBluetooth();
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.startGameOption:
	            Intent loadSettings = new Intent(this, SettingsActivity.class);
	            startActivity(loadSettings);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void getRoot(){
		Process p;
		try {
			p = Runtime.getRuntime().exec("su");
		} catch (Exception e) {
			Log.e("ROOT","Getting root priveleges failed for some reason");
		}
	}
	
	private void initBluetooth(){
		enableBluetoothAdapter();
		registerListener(BluetoothAdapter.ACTION_STATE_CHANGED);
	}
	
	private void enableBluetoothAdapter(){
		if (!adapter.isEnabled())
			adapter.enable();
	}

	private void initItOrders() {
		GlobalState.itLists = new ArrayList<String[]>();
		GlobalState.currentPlayers = new ArrayList<String>();

		String[] list1 = { "Red", "Blue", "Green", "Pink", "Silver", "Black" };
		String[] list2 = { "Red", "Green", "Black", "Silver", "Pink", "Blue" };
		String[] list3 = { "Blue", "Green", "Silver", "Red", "Pink", "Black" };
		String[] list4 = { "Blue", "Black", "Pink", "Silver", "Green", "Red" };
		String[] list5 = { "Green", "Silver", "Black", "Red", "Blue", "Gold" };
		String[] list6 = { "Green", "Gold", "Red", "Blue", "Black", "Silver" };
		String[] list7 = { "Pink", "Red", "Green", "Black", "Silver", "Blue" };
		String[] list8 = { "Pink", "Blue", "Silver", "Black", "Red", "Green" };
		String[] list9 = { "Silver", "Blue", "Black", "Red", "Pink", "Green" };
		String[] list10 = { "Silver", "Red", "Green", "Blue", "Pink", "Black" };
		String[] list11 = { "Black", "Blue", "Green", "Silver", "Red", "Pink" };
		String[] list12 = { "Black", "Green", "Pink", "Silver", "Blue", "Red" };

		GlobalState.itLists.add(list1);
		GlobalState.itLists.add(list2);
		GlobalState.itLists.add(list3);
		GlobalState.itLists.add(list4);
		GlobalState.itLists.add(list5);
		GlobalState.itLists.add(list6);
		GlobalState.itLists.add(list7);
		GlobalState.itLists.add(list8);
		GlobalState.itLists.add(list9);
		GlobalState.itLists.add(list10);
		GlobalState.itLists.add(list11);
		GlobalState.itLists.add(list12);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.startgame, menu);
		return true;
	}

	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				int state = getBluetoothState(intent);
				bluetoothOffHandler(state);
			}
		};
	}

	private void bluetoothOffHandler(int state) {
		if (isBluetoothStateOff(state))
			adapter.enable();
	}

	public void gotoSetJoinNameActivity(View view) {
		Intent loadGame = new Intent(this, DevicesTrackerActivity.class);
		startActivity(loadGame);
	}

}
