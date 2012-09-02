package com.android.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.data.Player;

public class LobbyServerActivity extends BluetoothActivity {

	private ArrayAdapter<String> devicesAdapter;
	private static ArrayList<Player> playerList;
	private Date timestamp;
	private Date currentTimestamp;
	private long discovSeconds = 0;

	private Timer timer;
	private Timer cancelScheduler;
	private Timer startScheduler;
	private CancelDiscoveryTask cancelTask;
	private StartDiscoveryTask startTask;
	private StartGameTask startGameTask;
	private boolean receiverRegistered = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lobby_server);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void onStart() {
		super.onStart();
		init();
	}

	private void init() {
		initializeTimers();
		initializePlayerList();
		setAdapter();
		initializeDeviceListDisplay();
		setTimeDifference();

		Intent activityIntent = getIntent();
		boolean isServerTimestamp = activityIntent.getBooleanExtra(
				"dateChangedBool", false);
		Process p;
		try {
			p = Runtime.getRuntime().exec("su");

			if (!isServerTimestamp) {
				ensureBluetoothDiscoverability();
				startUp();
			} else {
				setTimeDifference();
				ensureBluetoothDiscoverability();
				startUp();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void initializeTimers() {
		timer = new Timer();
		startScheduler = new Timer();
		cancelScheduler = new Timer();
		startGameTask = new StartGameTask();
		startTask = new StartDiscoveryTask();
		cancelTask = new CancelDiscoveryTask();
	}

	private void initializePlayerList() {
		playerList = new ArrayList<Player>();
	}

	private void initializeAdapter() {
		setAdapter();
		registerListeners();
		registerListener(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		receiverRegistered = true;
	}

	private void setTimeDifference() {
		timestamp = new Date();
		Intent activityIntent = getIntent();
		String newTimestamp = activityIntent.getStringExtra("joinTimestamp");
		boolean isServerTimestamp = activityIntent.getBooleanExtra(
				"dateChangedBool", false);

		if (newTimestamp != null && isServerTimestamp) {
			String[] ts = newTimestamp.split(":");
			int newHours = Integer.parseInt(ts[0].substring(ts[0].length() - 2,
					ts[0].length()));
			int newMins = Integer.parseInt(ts[1].substring(0, 2));
			int newSecs = Integer.parseInt(ts[2].substring(0, 2));
			getTimestamp().setHours(newHours);
			getTimestamp().setMinutes(newMins);
			getTimestamp().setSeconds(newSecs);
		}

		currentTimestamp = new Date();
		discovSeconds = (getTimestamp().getTime() - currentTimestamp.getTime());
		discovSeconds = (discovSeconds - 5000) / 1000;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (selectedYes(resultCode)) {
			initializeDeviceListDisplay();
			if (discovSeconds != 0) {
				Date newTime = getTimestamp();
				Intent activityIntent = getIntent();
				String newTimestamp = activityIntent
						.getStringExtra("joinTimestamp");
				boolean isServerTimestamp = activityIntent.getBooleanExtra(
						"dateChangedBool", false);

				if (newTimestamp != null && isServerTimestamp) {
					String[] ts = newTimestamp.split(":");
					int newHours = Integer.parseInt(ts[0].substring(
							ts[0].length() - 2, ts[0].length()));
					int newMins = Integer.parseInt(ts[1].substring(0, 2));
					int newSecs = Integer.parseInt(ts[2].substring(0, 2));
					newTime.setHours(newHours);
					newTime.setMinutes(newMins);
					newTime.setSeconds(newSecs);
				} else {
					if (newTime.getMinutes() < 58) {
						newTime.setMinutes(newTime.getMinutes() + 2);
					} else {
						newTime.setHours(newTime.getHours() + 1);
						newTime.setMinutes(1);
					}
				}
				setTimestamp(newTime);
			}

			String hours;
			String minutes;
			String seconds;

			if (getTimestamp().getHours() < 10) {
				hours = "0" + Integer.toString(getTimestamp().getHours());
			} else {
				hours = Integer.toString(timestamp.getHours());
			}
			if (getTimestamp().getMinutes() < 10) {
				minutes = "0" + Integer.toString(getTimestamp().getMinutes());
			} else {
				minutes = Integer.toString(getTimestamp().getMinutes());
			}
			if (getTimestamp().getSeconds() < 10) {
				seconds = "0" + Integer.toString(getTimestamp().getSeconds());
			} else {
				seconds = Integer.toString(getTimestamp().getSeconds());
			}

			String dateString = hours + minutes + seconds;
			dateString = dateString.replaceAll("\\W", "");
			appendDate(dateString);
			updateTimestampLabel();
			initializeAdapter();

			double interval = Math.random();
			interval = 4 + (interval * 3);

			timer.schedule(startGameTask, timestamp);
			startScheduler.schedule(startTask, 0, 10000);
			cancelScheduler
					.schedule(cancelTask, (int) (interval * 1000), 10000);
			setupBluetoothDetection();
		} else {
			gotoStartJoinActivity();
		}
	}

	private void startUp() {
		initializeDeviceListDisplay();
		if (discovSeconds != 0) {
			Date newTime = getTimestamp();
			Intent activityIntent = getIntent();
			String newTimestamp = activityIntent
					.getStringExtra("joinTimestamp");
			boolean isServerTimestamp = activityIntent.getBooleanExtra(
					"dateChangedBool", false);

			if (newTimestamp != null && isServerTimestamp) {
				String[] ts = newTimestamp.split(":");
				int newHours = Integer.parseInt(ts[0].substring(
						ts[0].length() - 2, ts[0].length()));
				int newMins = Integer.parseInt(ts[1].substring(0, 2));
				int newSecs = Integer.parseInt(ts[2].substring(0, 2));
				newTime.setHours(newHours);
				newTime.setMinutes(newMins);
				newTime.setSeconds(newSecs);
			} else {
				if (newTime.getMinutes() < 58) {
					newTime.setMinutes(newTime.getMinutes() + 2);
				} else {
					newTime.setHours(newTime.getHours() + 1);
					newTime.setMinutes(1);
				}
			}
			setTimestamp(newTime);
		}

		String hours;
		String minutes;
		String seconds;

		if (getTimestamp().getHours() < 10) {
			hours = "0" + Integer.toString(getTimestamp().getHours());
		} else {
			hours = Integer.toString(timestamp.getHours());
		}
		if (getTimestamp().getMinutes() < 10) {
			minutes = "0" + Integer.toString(getTimestamp().getMinutes());
		} else {
			minutes = Integer.toString(getTimestamp().getMinutes());
		}
		if (getTimestamp().getSeconds() < 10) {
			seconds = "0" + Integer.toString(getTimestamp().getSeconds());
		} else {
			seconds = Integer.toString(getTimestamp().getSeconds());
		}

		String dateString = hours + minutes + seconds;
		dateString = dateString.replaceAll("\\W", "");
		appendDate(dateString);
		updateTimestampLabel();
		initializeAdapter();

		double interval = Math.random();
		interval = 4 + (interval * 3);

		timer.schedule(startGameTask, timestamp);
		startScheduler.schedule(startTask, 0, 10000);
		cancelScheduler.schedule(cancelTask, (int) (interval * 1000), 10000);
		setupBluetoothDetection();
	}

	private void appendDate(String dateString) {
		adapter.setName(adapter.getName() + "_$_" + dateString);
	}

	private void updateTimestampLabel() {
		TextView view = (TextView) findViewById(R.id.gameTimeLabel);
		String amPM = "A.M.";
		amPM = getTimestamp().getHours() > 12 ? "P.M" : amPM;
		int hours = getTimestamp().getHours() % 12;
		hours = hours == 0 ? 12 : hours;
		int minutes = getTimestamp().getMinutes();
		String mins = minutes < 10 ? "0" + minutes : Integer.toString(minutes);
		int seconds = getTimestamp().getSeconds();
		String secs = seconds < 10 ? "0" + seconds : Integer.toString(seconds);
		String gameBeginTime = "Start time: " + hours + ":" + mins + ":" + secs
				+ " " + amPM;
		view.setText(gameBeginTime);
	}

	private void setUpScanAndDisplay() {
		setupBluetoothDetection();
		initializeDeviceListDisplay();
	}

	private void initializeDeviceListDisplay() {
		devicesAdapter = new ArrayAdapter<String>(this, R.layout.device_entry);
		ListView list = (ListView) findViewById(R.id.deviceList);
		list.setAdapter(devicesAdapter);
	}

	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (isActionFound(action)) {
					onDiscoveredHandler(intent);
				}
				if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
					if (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
							Short.MIN_VALUE) == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
						startUp();
					}
				}
			}
		};
	}

	private void onDiscoveredHandler(Intent intent) {
		BluetoothDevice device = getRemoteDevice(intent);
		String devString = device.getName();
		if (isPhone(device) && devString != null) {
			String[] tokens = devString.split("_\\$_");
			String hours;
			String minutes;
			String seconds;

			if (getTimestamp().getHours() < 10) {
				hours = "0" + Integer.toString(getTimestamp().getHours());
			} else {
				hours = Integer.toString(getTimestamp().getHours());
			}
			if (getTimestamp().getMinutes() < 10) {
				minutes = "0" + Integer.toString(getTimestamp().getMinutes());
			} else {
				minutes = Integer.toString(getTimestamp().getMinutes());
			}
			if (getTimestamp().getSeconds() < 10) {
				seconds = "0" + Integer.toString(getTimestamp().getSeconds());
			} else {
				seconds = Integer.toString(getTimestamp().getSeconds());
			}

			String dateString = hours + minutes + seconds;
			dateString = dateString.replaceAll("\\W", "");

			if (tokens.length > 1) {

				if (tokens.length > 2) {
					if (tokens[2].equals(dateString)) {
						String name = tokens[0];
						String address = device.getAddress();
						Player player = new Player(name, address);
						player.setHash(getTimestamp());

						addPlayer(player);
					}
				} else if (tokens[1].equals(dateString)) {
					String name = tokens[0];
					String address = device.getAddress();
					Player player = new Player(name, address);
					player.setHash(getTimestamp());

					addPlayer(player);
				}
			}
		}
	}

	private void addPlayer(Player player) {
		if (player.getName() != null && !hasBeenFound(player)) {
			devicesAdapter.add(player.getName());
			playerList.add(player);
		}
	}

	@Override
	public void onStop() {
		adapter.cancelDiscovery();
		cancelTimers();
		if (receiverRegistered)
			unregisterReceiver(discoverReceiver);
		super.onStop();

	}

	@SuppressWarnings("unchecked")
	public void startGame() {
		Intent loadTracker = new Intent(this, DevicesTrackerActivity.class);

		adapter.cancelDiscovery();
		cancelTimers();

		addYourself();
		Collections.sort(playerList);

		loadTracker.putParcelableArrayListExtra("IT_ORDER", playerList);
		startActivity(loadTracker);
	}

	private boolean hasBeenFound(Player player) {
		for (Player p : playerList) {
			if (p.equals(player))
				return true;
		}

		return false;
	}

	private void addYourself() {
		Player you = new Player(adapter.getName(), adapter.getAddress());
		you.setHash(getTimestamp());

		playerList.add(you);
	}

	public void onBackPressed() {
		gotoStartJoinActivity();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date newTimestamp) {
		timestamp = newTimestamp;
	}

	private void gotoStartJoinActivity() {
		adapter.cancelDiscovery();
		cancelTimers();
		adapter.disable();

		Intent loadStartJoin = new Intent(this, StartJoinActivity.class);
		startActivity(loadStartJoin);
	}

	private void cancelTimers() {
		timer.cancel();
		cancelScheduler.cancel();
		startScheduler.cancel();
		cancelTask.cancel();
		startTask.cancel();
		startGameTask.cancel();
	}

	private class CancelDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}

	private class StartDiscoveryTask extends TimerTask {
		public void run() {
			adapter.startDiscovery();
		}
	}

	private class StartGameTask extends TimerTask {

		@Override
		public void run() {
			startGame();
		}

	}

}
