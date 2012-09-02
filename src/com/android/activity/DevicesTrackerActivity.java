package com.android.activity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.android.data.GlobalState;
import com.android.data.Player;

public class DevicesTrackerActivity extends BluetoothActivity implements
		SensorEventListener {

	private Intent loadScore;
	
	private Timer cancelScheduler;
	private Timer startScheduler;
	private Timer itScheduler;
	
	private StartDiscoveryTask startTask;
	private ChangeItTask itTask;
	
	private TextView scoreBoard;
	private int score;
	private ArrayList<AccelerometerData> accelerationSamples;
	private ArrayList<Player> playerList;
	private Player it;
	private int index = 0;
	private int foundCount = 0;
	
	private SoundPool pool;
	private int soundID;
	
	private SensorManager sManager;
	private Sensor accelerometer;
	
	private String startTime;

	private static final int START_SCORE = 0;
	private static final int SCORE_OFFSET = 110;
	private static final int GAME_DURATION = 120;
	private static final int SCORE_HANDICAPP = 200;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices);
		accelerationSamples = new ArrayList<AccelerometerData>();
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void onStart() {
		super.onStart();
		init();
	}

	private void init() {
		initSound();
		setPlayerListFromExtra();
		setFinalScoreIntent();

		setAdapter();
		initTimerItems();
		initScoreItems(START_SCORE);

		makeDiscoverable(GAME_DURATION);
	}
	
	private void startUp(){
		Date newTime = new Date();
	}

	private void setPlayerListFromExtra() {
		Bundle extras = getIntent().getExtras();
		playerList = extras.getParcelableArrayList("IT_ORDER");

	}
	

	private void setFinalScoreIntent() {
		loadScore = new Intent(this, FinalScoreActivity.class);
	}

	private void setItOrder() {
		int length = playerList.size();
		int interval = (GAME_DURATION / length) * 1000;

		updateIt();
		setItLabel();
		setItScheduling(interval);
		playItAlert();
	}

	private void updateIt() {
		it = playerList.get(index);
	}

	private void setItScheduling(int interval) {
		itScheduler = new Timer();
		itTask = new ChangeItTask();
		itScheduler.schedule(itTask, interval, interval);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode > 0) {
			discoverableAccepted();
			sManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
			startTime = Calendar.HOUR_OF_DAY + "_" + Calendar.MINUTE + "_"
					+ Calendar.SECOND;
		} else
			discoverableRejected();
	}

	private void discoverableAccepted() {
		setItOrder();
		setUpBluetoothDetection();
	}

	private void discoverableRejected() {
		stopAdapter();
		cancelScheduledTasks();
		gotoJoinStartActivity();
	}

	private void setItLabel() {
		TextView itLabel = (TextView) findViewById(R.id.itLabel);
		String str = parseNameFromIt();
		itLabel.setText(str + " is IT");
	}

	private String parseNameFromIt() {
		String name = it.getName();
		String[] tokens = name.split("_\\$_");
		return tokens[0];
	}

	private void initTimerItems() {
		startScheduler = new Timer();
		startTask = new StartDiscoveryTask();
	}

	private void initScoreItems(int startScore) {
		initScoring(startScore);
		setScoreText("Your score: " + intToString(score));
	}

	private void initScoring(int startScore) {
		scoreBoard = getScoreboard();
		initScore(startScore);
	}

	private TextView getScoreboard() {
		TextView scoreBoard = (TextView) findViewById(R.id.scoreLabel);
		return scoreBoard;
	}

	private void initScore(int startScore) {
		String starterAddress = playerList.get(0).getAddress();
		boolean isFirst = starterAddress.equals(adapter.getAddress());
		if (isFirst) {
			score = SCORE_HANDICAPP;
		} else {
			score = startScore;
		}
	}

	private void setScoreText(String newScore) {
		scoreBoard.setText(newScore);
	}

	protected void setUpBluetoothDetection() {
		super.setupBluetoothDetection();
	}

	protected void registerListeners() {
		super.registerListeners();
		registerListener(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
	}

	@Override
	public void onStop() {
		stopAdapter();
		cancelScheduledTasks();
		try {
			sManager.unregisterListener(this);
		} catch (NullPointerException e) {

		}
		try {
			File card = Environment.getExternalStorageDirectory();
			File directory = new File(card.getAbsolutePath()
					+ "/accelerometerdata");
			directory.mkdirs();

			String dayString = "";
			int day = Calendar.DAY_OF_MONTH;

			if (day < 10) {
				dayString = "0" + day;
			} else {
				dayString += day;
			}

			String monthString = "";
			int month = Calendar.MONTH + 1;

			if (month < 10) {
				monthString = "0" + month;
			} else {
				monthString += month;
			}
			String fileName = GlobalState.playerIDNumber + "_tag_"
					+ Calendar.YEAR + "-" + dayString + "-" + monthString
					+ "_0";

			File sampleFile = new File(directory, fileName);
			sampleFile.createNewFile();

			FileWriter fw = new FileWriter(sampleFile);

			for (AccelerometerData data : accelerationSamples) {
				fw.write(data.getX() + " " + data.getY() + " " + data.getZ()
						+ "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("WRITEACCELEROMETER", "File write failed");
		}

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		stopAdapter();
		unregisterReceiver(discoverReceiver);
		cancelScheduledTasks();
		adapter.disable();
		gotoJoinStartActivity();
	}

	private void stopAdapter() {
		adapter.cancelDiscovery();
	}

	/*
	 * private void stopScheduledItems() { if (startScheduler != null)
	 * startScheduler.cancel(); if (startTask != null) startTask.cancel(); }
	 */

	private void gotoJoinStartActivity() {
		// restores the name to original
		String tokens[] = adapter.getName().split("_\\$_");
		adapter.setName(tokens[0]);
		// end of restore name
		Intent restart = new Intent(this, StartJoinActivity.class);
		startActivity(restart);
	}

	private String intToString(int value) {
		return Integer.toString(value);
	}

	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				actionHandler(action, intent);
			}
		};
	}

	private void actionHandler(String action, Intent intent) {
		if (isActionFound(action))
			deviceDiscoveredHandler(intent);
		else if (isDiscoveryFinished(action))
			discoveryFinishedHandler();
		else if (isDiscoverableChange(action))
			endDiscoverableHandler(intent);
	}

	private void deviceDiscoveredHandler(Intent intent) {
		BluetoothDevice device = getRemoteDevice(intent);
		if (isPhone(device) && isPlayer(device)) {
			foundCount++;
			short strength = getAbsoluteSignalStrength(intent);
			setScoreFromSignalStrength(device, strength);
		}
	}

	private boolean isPlayer(BluetoothDevice device) {
		for (Player player : playerList) {
			if (player.getAddress().equals(device.getAddress())) {
				return true;
			}
		}

		return false;
	}

	private short getAbsoluteSignalStrength(Intent intent) {
		return (short) Math.abs(getSignalStrength(intent));
	}

	private short getSignalStrength(Intent intent) {
		return intent
				.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
	}

	private void setScoreFromSignalStrength(BluetoothDevice device,
			short strength) {
		if (thisPlayerIsIt())
			addFoundPoints(strength);
	}

	private boolean thisPlayerIsIt() {
		return it.getAddress().equals(adapter.getAddress());
	}

	/*
	 * private boolean foundPlayerIsIt(BluetoothDevice device) { return
	 * it.getAddress().equals(device.getAddress()); }
	 */

	private void addFoundPoints(short strength) {
		score += (SCORE_OFFSET - strength);
		updateScoreLabel();
	}

	/*
	 * private void loseFoundPoints(short strength) { score -= (SCORE_OFFSET -
	 * strength); updateScoreLabel(); }
	 */

	private void updateScoreLabel() {
		setScoreText("Your score: " + intToString(score));
	}

	private boolean devicesFound() {
		return foundCount > 0;
	}

	private void discoveryFinishedHandler() {
		setItLabel();

		if (thisPlayerIsIt())
			adapter.startDiscovery();
		if (thisPlayerIsIt() && !devicesFound()) {
			if (score - 10 < 0) {
				score = 0;
				setScoreText("Your Score: " + intToString(score));
			} else
				setScoreText("Your Score: " + intToString(score -= 10));
		}

		clearDiscoveredDevices();
	}

	private void clearDiscoveredDevices() {
		foundCount = 0;
	}

	private void cancelScheduledTasks() {
		if (cancelScheduler != null)
			cancelScheduler.cancel();
		if (itScheduler != null)
			itScheduler.cancel();
		if (startScheduler != null)
			startScheduler.cancel();
		if (startTask != null)
			startTask.cancel();
		if (itTask != null)
			itTask.cancel();
	}

	private void endDiscoverableHandler(Intent intent) {
		int discoverState = getDiscoverState(intent);
		if (!isDiscoverableState(discoverState)) {
			unregisterReceiver(discoverReceiver);
			cancelScheduledTasks();
			adapter.setName(adapter.getName() + "__" + score);
			loadScore.putExtra("FINAL_SCORE", score);
			loadScore.putParcelableArrayListExtra("PLAYERS", playerList);
			startActivity(loadScore);
		}
	}

	private void initSound() {
		pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundID = pool.load(this, R.raw.youreit, 1);
	}

	private void playItAlert() {

		if (thisPlayerIsIt()) {
			adapter.startDiscovery();
			startScheduler.scheduleAtFixedRate(startTask, 4000, 4000);
			pool.play(soundID, 1, 1, 1, 0, 1);
		} else if (startScheduler != null) {
			startTask.cancel();
			startTask = new StartDiscoveryTask();
		}
		makeVibrate(500);
	}

	private void makeVibrate(int length) {
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(length);
	}

	public class CancelDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}

	public class StartDiscoveryTask extends TimerTask {
		public void run() {
			adapter.cancelDiscovery();
		}
	}

	private class ChangeItTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				public void run() {
					setNextIt();
					setItLabel();
					playItAlert();
				}
			});

		}

		private void setNextIt() {
			index++;
			it = playerList.get(index);
		}

	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Not implemented

	}

	public void onSensorChanged(SensorEvent event) {
		accelerationSamples.add(new AccelerometerData(event.values[0],
				event.values[1], event.values[2]));

	}

	private class AccelerometerData {
		private float x;
		private float y;
		private float z;

		public AccelerometerData(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public float getX() {
			return x;
		}

		public void setX(float x) {
			this.x = x;
		}

		public float getY() {
			return y;
		}

		public void setY(float y) {
			this.y = y;
		}

		public float getZ() {
			return z;
		}

		public void setZ(float z) {
			this.z = z;
		}

	}
}
