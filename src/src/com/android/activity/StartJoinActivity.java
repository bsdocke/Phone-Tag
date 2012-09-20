package com.android.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class StartJoinActivity extends BluetoothActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startjoin);
		setAdapter();
	}

	public void onStart() {
		super.onStart();

		if (!adapter.isEnabled())
			adapter.enable();
		registerListener(BluetoothAdapter.ACTION_STATE_CHANGED);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.startgame, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.startGameOption:
			startGame();
			return true;
		case R.id.howToOption:
			gotoHowToPlayActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						Short.MIN_VALUE);

				if (state == BluetoothAdapter.STATE_OFF)
					adapter.enable();

			}
		};
	}

	public void gotoSetJoinNameActivity(View view) {
		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
			alertBox.setMessage("Please wait one moment for the Bluetooth adapter to turn on.");

			alertBox.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface,
								int arg1) {
							dialogInterface.cancel();
						}
					});

			alertBox.show();

			return;
		}

		Intent loadJoinGame = new Intent(this, SetJoinNameActivity.class);
		startActivity(loadJoinGame);
	}

	public void onBackPressed() {
		Intent loadFirstScreen = new Intent(this, InputIDActivity.class);
		startActivity(loadFirstScreen);
	}

	private void startGame() {
		if (!adapter.isEnabled()) {
			AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
			alertBox.setMessage("Please wait one moment for the Bluetooth adapter to turn on.");

			alertBox.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface,
								int arg1) {
							dialogInterface.cancel();
						}
					});

			alertBox.show();

			return;
		}

		Intent loadTeamSetup = new Intent(this,
				CreateChoosePlayerNameActivity.class);
		startActivity(loadTeamSetup);
	}

	public void gotoHowToPlayActivity() {
		Intent loadHowTo = new Intent(this, HowToPlayActivity.class);
		startActivity(loadHowTo);
	}

}
