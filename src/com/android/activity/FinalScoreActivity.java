package com.android.activity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.data.GlobalState;

public class FinalScoreActivity extends BluetoothActivity {

	ArrayList<String> playerNames;

	Timer discoverTimer;
	Timer cancelTimer;
	int itIndex = 0;

	ScoreTask scoreTask;
	CancelDiscoveryTask cancelTask;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_score);

		setBackground();
	}

	public void onStart() {
		super.onStart();
		init();
	}

	private void init() {
		setAdapter();
		initializeArrayList();
		initializeTimers();
		discoverTimer.schedule(scoreTask, getScanDelay());

		collectScores();
	}

	private int getScanDelay() {
		String name = adapter.getName().split("_")[0];
		int delay = 0;
		for (int i = 0; i < GlobalState.itOrder.length; i++) {
			if (GlobalState.itOrder[i].equals(name)) {
				return delay;
			} else {
				itIndex++;
				delay += 10000;
			}
		}

		return delay;
	}

	private void setBackground() {
		RelativeLayout relLay = (RelativeLayout) findViewById(R.id.scoreLayout);
		Drawable bg = relLay.getBackground();
		bg.setAlpha(100);
	}

	private void initializeArrayList() {
		playerNames = new ArrayList<String>();
	}

	private void initializeTimers() {
		discoverTimer = new Timer();
		cancelTimer = new Timer();
		scoreTask = new ScoreTask();
		cancelTask = new CancelDiscoveryTask();
	}

	private void collectScores() {
		listenForScores();
		this.ensureBluetoothDiscoverability();
	}

	public void makeDiscoverable(int duration) {
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
		startActivityForResult(discoverableIntent, 0);
		listenForScores();
	}

	public void onStop() {
		// unregisterReceiver(discoverReceiver);
		cancelTimers();
		// adapter.disable();//
		super.onStop();
	}

	private void cancelTimers() {
		if (timerItemDefined(discoverTimer))
			discoverTimer.cancel();
		if (timerItemDefined(scoreTask))
			scoreTask.cancel();
		if (timerItemDefined(cancelTimer))
			cancelTimer.cancel();
		if (timerItemDefined(cancelTask))
			cancelTask.cancel();
	}

	private boolean timerItemDefined(Timer time) {
		return time != null;
	}

	private boolean timerItemDefined(TimerTask time) {
		return time != null;
	}

	private void listenForScores() {
		registerListeners();
	}

	public void onBackPressed() {
		gotoSplashActivity();
	}

	private void gotoSplashActivity() {
		cancelTimers();
		// unregisterReceiver(discoverReceiver);
		adapter.cancelDiscovery();
		Intent restart = new Intent(this, SplashActivity.class);
		startActivity(restart);
	}

	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (isActionFound(action)) {
					BluetoothDevice device = getRemoteDevice(intent);
					if (isPhone(device) && !hasBeenFound(device.getName())) {
						String name = device.getName();
						String[] nameTokens = name.split("_");
						name = nameTokens[0];

						boolean isPlayer = false;
						for (String play : GlobalState.itOrder) {
							if (name.equals(play)) {
								isPlayer = true;
							}
						}

						if (isPlayer) {
							GlobalState.myScore -= Integer
									.parseInt(nameTokens[itIndex + 1]);
							playerNames.add(name);
							// TextView scoreView =
							// (TextView)findViewById(R.id.scoreList);
						}

					}
				} else if (action
						.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
					TextView scoreView = (TextView) findViewById(R.id.scoreList);
					scoreView.setText(Integer.toString(GlobalState.myScore));
				}
			}
		};
	}

	private boolean hasBeenFound(String name) {
		if (name.split("_").length > 1) {
			String nameToken = name.split("_")[0];
			for (String player : playerNames) {
				if (player.equals(nameToken)) {
					return true;
				}
			}

			return false;
		} else {
			return true;
		}
	}

	private class ScoreTask extends TimerTask {
		public void run() {
			Log.d("BLUETOOTH", "Discovery started");
			adapter.startDiscovery();
		}
	}

	private class CancelDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}
}
