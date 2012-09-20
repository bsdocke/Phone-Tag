package com.android.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.data.GlobalState;
import com.android.data.Player;

public class FinalScoreActivity extends BluetoothActivity {

	ArrayAdapter<ScoreObject> scoreAdapter;

	ArrayList<String> playerNames;

	Timer discoverTimer;
	Timer cancelTimer;

	ScoreTask scoreTask;
	CancelDiscoveryTask cancelTask;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_score);

		RelativeLayout relLay = (RelativeLayout) findViewById(R.id.scoreLayout);
		Drawable bg = relLay.getBackground();
		bg.setAlpha(100);
	}

	public void onStart() {
		super.onStart();
		init();
	}

	private void init() {
		setAdapter();
		initializeArrayList();
		initializeArrayAdapter();
		addMyScore();

		initializeTimers();
		collectScores();
	}

	private void initializeArrayList() {
		playerNames = new ArrayList<String>();
	}

	private void initializeArrayAdapter() {
		scoreAdapter = new ArrayAdapter<ScoreObject>(this,
				R.layout.device_entry);
		ListView list = (ListView) findViewById(R.id.scoreList);
		list.setAdapter(scoreAdapter);
	}

	private void initializeTimers() {
		discoverTimer = new Timer();
		cancelTimer = new Timer();
		scoreTask = new ScoreTask();
		cancelTask = new CancelDiscoveryTask();

		double interval = Math.random();
		interval = 5 + (interval * 6);
		discoverTimer.scheduleAtFixedRate(scoreTask, 0, 10000);
		cancelTimer.scheduleAtFixedRate(cancelTask,
				(long) Math.floor(interval * 1000), 10000);

	}

	private void addMyScore() {
		String score = Integer.toString(getIntent().getIntExtra("FINAL_SCORE",
				Short.MIN_VALUE));
		String name = adapter.getName().split("_\\$_")[0];
		scoreAdapter.add(new ScoreObject(name, Integer.parseInt(score)));
	}

	private void collectScores() {
		makeDiscoverable(120);
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
		adapter.disable();//
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
		unregisterReceiver(discoverReceiver);
		scoreAdapter.clear();
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
						String[] nameTokens = name.split("__");
						if (nameTokens.length > 2) {
							boolean isPlayer = false;
							for (String play : GlobalState.itOrder) {
								if (nameTokens[0].equals(play)) {
									isPlayer = true;
								}
							}

							if (isPlayer) {
								playerNames.add(nameTokens[0]);
								ScoreObject scoreItem = new ScoreObject(
										nameTokens[0].split("_\\$_")[0],
										Integer.parseInt(nameTokens[1]));
								scoreAdapter.add(scoreItem);
								orderScores();
								if (scoreAdapter.getCount() == GlobalState.itOrder.length) {
									adapter.cancelDiscovery();
									cancelTimers();
								}
							}
						}
					}
				}
			}
		};
	}

	private void orderScores() {
		scoreAdapter.sort(new Comparator<ScoreObject>() {
			public int compare(ScoreObject object1, ScoreObject object2) {
				return object1.compareTo(object2);
			}
		});
	}

	private boolean hasBeenFound(String name) {
		for (String player : playerNames) {
			if (player.equals(name)) {
				return true;
			}
		}

		return false;
	}

	private class ScoreTask extends TimerTask {
		public void run() {
			adapter.startDiscovery();
		}
	}

	private class CancelDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}

	private class ScoreObject implements Comparable<ScoreObject> {

		private String playerName;
		private int score;

		public ScoreObject(String name, int score) {
			playerName = name;
			this.score = score;
		}

		public int getScore() {
			return this.score;
		}

		public int compareTo(ScoreObject another) {
			if (this.score < another.getScore())
				return 1;
			else if (this.score > another.getScore())
				return -1;
			else if (this.score == another.getScore())
				return 0;

			return 0;
		}

		public String toString() {
			return this.playerName + " - " + this.score;
		}

	}
}
