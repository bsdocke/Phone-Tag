package com.android.activity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.data.GameListItem;

public class UserGameSelectActivity extends BluetoothActivity {

	private ArrayAdapter<GameListItem> devicesAdapter;
	private Date date;
	private boolean isDateChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.join_game);

		date = new Date();
		isDateChanged = false;
		setAdapter();

		setUpScanAndDisplay();
	}
	
	private void setUpScanAndDisplay() {
		initializeDeviceListDisplay();
		setupBluetoothDetection();
	}

	private void initializeDeviceListDisplay() {
		devicesAdapter = new ArrayAdapter<GameListItem>(this,
				R.layout.device_entry);
		ListView list = (ListView) findViewById(R.id.deviceList);
		list.setAdapter(devicesAdapter);

		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapt /* adapter */,
					View view, int position, long arg) {
				adapter.cancelDiscovery();
				ListView devList = (ListView) findViewById(R.id.deviceList);
				GameListItem listItem = (GameListItem) devList
						.getItemAtPosition(position);
				setGameServerTimestamp(listItem.getDate());
				goToWaitingRoom(view, listItem);
			}
		});
	}

	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (isActionFound(action)) {
					BluetoothDevice dev = getRemoteDevice(intent);
					String deviceString = dev.getName();
					if (isPhone(dev) && deviceString != null) {
						String[] remDeviceInfo = deviceString.split("_\\$_");
						if (remDeviceInfo.length == 3) {
							String gameName = remDeviceInfo[1];
							String gameTimeStamp = remDeviceInfo[2];
							DateFormat df = new SimpleDateFormat("HHmmss");
							try {
								Date newDate = df.parse(gameTimeStamp);
								Date gameDate = new Date();

								gameDate.setHours(newDate.getHours());
								gameDate.setMinutes(newDate.getMinutes());
								gameDate.setSeconds(newDate.getSeconds());

								if (validateGameServerTimestamp(gameDate)) {
									GameListItem newListItem = new GameListItem(
											gameName, gameDate);
									int count = devicesAdapter.getCount();
									if (count == 0) {
										devicesAdapter.add(newListItem);
									} else {
										for (int i = 0; i < count; i++) {
											if (!devicesAdapter.getItem(i)
													.equals(newListItem)) {
												if (i == --count) {
													devicesAdapter
															.add(newListItem);
												}
											}
										}
									}

								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					}
				} else if (isDiscoveryFinished(action)) {
					adapter.startDiscovery();
				}
			}
		};
	}

	@Override
	public void onStop() {
		unregisterReceiver(discoverReceiver);
		adapter.cancelDiscovery();
		
		super.onStop();
	}

	public void setGameServerTimestamp(Date d) {
		date = d;
		isDateChanged = true;
	}

	public Date getGameServerTimestamp() {
		return date;
	}

	public boolean validateGameServerTimestamp(Date d) {
		Date currTimestamp = new Date();
		if (currTimestamp.before(d)) {
			return true;
		}
		return false;
	}

	// When game button is selected then go to waiting room
	public void goToWaitingRoom(View view, final GameListItem listItem) {
		Date serverDate = getGameServerTimestamp();
		if (validateGameServerTimestamp(serverDate)) {
			adapter.cancelDiscovery();
			Intent loadDevices = buildLobbyIntent(serverDate);
			startActivity(loadDevices);
		} else {
			gameAlreadyStartedHandler(listItem);
		}

	}

	private Intent buildLobbyIntent(Date serverDate) {
		Intent loadDevices = new Intent(this, LobbyServerActivity.class);
		String dateString = serverDate.toString();
		loadDevices.putExtra("joinTimestamp", dateString);
		loadDevices.putExtra("dateChangedBool", isDateChanged);

		return loadDevices;

	}

	private void gameAlreadyStartedHandler(final GameListItem listItem) {
		AlertDialog.Builder alertBox = buildAlert(listItem);
		alertBox.show();
	}

	private AlertDialog.Builder buildAlert(final GameListItem listItem) {
		AlertDialog.Builder alertBox = new AlertDialog.Builder(this);
		alertBox.setMessage("Sorry! This game has already started.");
		alertBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int arg1) {
				devicesAdapter.remove(listItem);
				dialogInterface.cancel();
			}
		});
		return alertBox;
	}

}
