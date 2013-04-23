package fitnessapps.spacerayders.activity;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import fitnessapps.spacerayders.data.GlobalState;

public class DevicesTrackerActivity extends RemoteServiceClient {

	private Intent loadScore;
	private Intent startIntent;

	private Timer cancelScheduler;
	private Timer startScheduler;
	private Timer itScheduler;

	private CancelDiscoveryTask cancelTask;
	private ChangeItTask itTask;

	private TextView scoreBoard;
	private int score;
	private String[] playerList;
	private String it;
	private int index = 0;
	private int foundCount = 0;

	private SoundPool pool;
	private int soundID;

	private static final int START_SCORE = 0;
	private static final int SCORE_OFFSET = 100;
	private static final int GAME_DURATION = 300;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices);
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public void onStart() {
		super.onStart();

		index = 0;
		init();
		
	}

	private void init() {
		initSound();
		resetScore();
		setPlayerList();
		setIntents();

		gameStart(); // binds to accelerometer service if it exists
		
		initTimerItems();
		initScoreItems(START_SCORE);
		
		setAdapter(); // must be called before discovarableAccepeted();
		setUpBluetooth();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == BluetoothActivity.REQUEST_ENABLE_BT) {
	        if (resultCode == RESULT_CANCELED) {
	        	Toast.makeText(this, "You must enable Bluetooth to play this game", Toast.LENGTH_LONG).show();
	        	startActivity(startIntent);
	        } 
	    }
	    if (requestCode == BluetoothActivity.REQUEST_DISCOV_BT) {
	    	if (resultCode == GAME_DURATION) {
	    		setItOrder();
	    	}
	    	if (resultCode == RESULT_CANCELED) {
	    		Toast.makeText(this, "You must be discoverable to play this game", Toast.LENGTH_LONG).show();
	    		startActivity(startIntent);
	    	}
	    }
	}
	
	
	private void resetScore(){
		GlobalState.myScore = START_SCORE;
	}

	private void setPlayerList() {
		Calendar c = Calendar.getInstance();
		int itOrderIndex = c.get(Calendar.MINUTE) / 5;
		playerList = GlobalState.itLists.get(itOrderIndex);
		GlobalState.itOrder = playerList;
	}

	private void setIntents() {
		loadScore = new Intent(this, FinalScoreActivity.class);
		startIntent = new Intent(this, SplashActivity.class);
	}

	private void setItOrder() {
		//int length = playerList.length;
		int interval = 62000; //(GAME_DURATION / length) * 1000;

		updateIt();
		setItLabel();
		setItScheduling(interval);
		playItAlert();
	}

	private void updateIt() {
		if (index < playerList.length) {
			String player = playerList[index];
			if (isPlaying(player)) {
				it = player;
			} else {
				setNextIt();
			}
		}
		else{
			setNextIt();
		}
	}

	private void setItScheduling(int interval) {
		itScheduler = new Timer();
		itTask = new ChangeItTask();
		itScheduler.schedule(itTask, interval, interval);
	}

	private void setItLabel() {
		TextView itLabel = (TextView) findViewById(R.id.itLabel);
		itLabel.setText(it + " is IT");
	}

	private void initTimerItems() {
		startScheduler = new Timer();
		cancelTask = new CancelDiscoveryTask();
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
		score = startScore;
	}

	private void setScoreText(String newScore) {
		scoreBoard.setText(newScore);
	}

	protected void setUpBluetooth() {
		super.setupBluetoothDetection();
		super.enableBluetooth();
		super.makeDiscoverable(GAME_DURATION);
		//Log.e("TIME EXTENDED", "1 minute");
		//ensureBluetoothDiscoverability();
	}

	@Override
	protected void registerListeners() {
		super.registerListeners();
		registerListener(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
	}

	@Override
	public void onStop() {
		stopAdapter();
		cancelScheduledTasks();

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	onStop();

		    		releaseService();
		    		it = "nobody";
		    		startActivity(startIntent);
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            dialog.cancel();
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to quit?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
	}

	/*
	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	} */

	private void stopAdapter() {
		adapter.cancelDiscovery();
	}

	private String intToString(int value) {
		return Integer.toString(value);
	}

	@Override
	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			@Override
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
		for (String player : playerList) {
			if (player.equals(device.getName())) {
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
			addFoundPoints(device, strength);
	}

	private boolean thisPlayerIsIt() {
		return it.equals(GlobalState.playerName);
	}

	private void addFoundPoints(BluetoothDevice device, short strength) {
		if (isPlayer(device)) {
			score += (SCORE_OFFSET - strength);
			updateScoreLabel();
		}
	}

	private void updateScoreLabel() {
		setScoreText("Your score: " + intToString(score));
	}

	private boolean devicesFound() {
		return foundCount > 0;
	}

	private void discoveryFinishedHandler() {
		setItLabel();

		resetDiscoveryIfIt();
		nobodyDiscoveredHandler();

		clearDiscoveredDevices();
	}

	private void resetDiscoveryIfIt() {
		if (thisPlayerIsIt())
			adapter.startDiscovery();
	}

	private void nobodyDiscoveredHandler() {
		if (isItAndFoundNobody()) {
			if (scoreWillBeLessThanZero()) {
				score = 0;
			} else {
				score -= 10;
			}
			updateScoreLabel();
		}
	}

	private boolean isItAndFoundNobody() {
		return thisPlayerIsIt() && !devicesFound();
	}

	private boolean scoreWillBeLessThanZero() {
		return (score - 10) < 0;
	}

	private void clearDiscoveredDevices() {
		foundCount = 0;
	}

	private void cancelScheduledTasks() {
		cancelDiscoveryTimers();
		cancelItTimers();
		cancelStartTimers();
	}

	private void cancelDiscoveryTimers() {
		if (cancelScheduler != null)
			cancelScheduler.cancel();
	}

	private void cancelItTimers() {
		if (itScheduler != null)
			itScheduler.cancel();
		if (itTask != null)
			itTask.cancel();
	}

	private void cancelStartTimers() {
		if (startScheduler != null)
			startScheduler.cancel();
		if (cancelTask != null)
			cancelTask.cancel();
	}

	private void initSound() {
		pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundID = pool.load(this, R.raw.youreit, 1);
	}

	private void playItAlert() {

		try {
			if (thisPlayerIsIt()) {
				adapter.startDiscovery();
				startScheduler.scheduleAtFixedRate(cancelTask, 4000, 4000);
				pool.play(soundID, 1, 1, 1, 0, 1);
			} else if (startScheduler != null) {
				cancelTask.cancel();
				cancelTask = new CancelDiscoveryTask();
			}
		} catch (IllegalStateException e) {
			//Do nothing, the timer has already been cancelled;
		}
		makeVibrate(500);
	}

	private void makeVibrate(int length) {
		Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibe.vibrate(length);
	}

	private boolean isPlaying(String playerID) {
		for (int i = 0; i < playerList.length; i++) {
			if (i < GlobalState.currentPlayers.size()
					&& playerID.equals(GlobalState.currentPlayers.get(i))) {
				return true;
			}
		}

		return false;
	}

	public class CancelDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}

	public class StartDiscoveryTask extends TimerTask {
		@Override
		public void run() {
			adapter.cancelDiscovery();
		}
	}
	
	private void setNextIt() {
		index++;
		adapter.cancelDiscovery();
		if (index < playerList.length) {
			//Log.d("Tag", "setNextIt, index is " + index);
			if (isPlaying(playerList[index])) {
				//Log.d("Tag", playerList[index] + "is playing");
				//ensureBluetoothDiscoverability();
				it = playerList[index];
			} else {
				//Log.d("Tag", "Recursively calling setNextIt");
				setNextIt();
			}
		} else {
			endGame();
		}
	}
	
	private void endGame(){
		//Log.d("Tag", "The game is over because we are out of people to be it");
		cancelScheduledTasks();
		adapter.cancelDiscovery();

		GlobalState.myScore = score;
		releaseService();
		it = "nobody";
		startActivity(loadScore);
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
	}

	
}
