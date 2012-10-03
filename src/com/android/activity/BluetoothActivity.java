package com.android.activity;

import java.lang.reflect.Method;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.fitnessapps.spacerayders.R;

public class BluetoothActivity extends Activity {

	protected BluetoothAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected boolean selectedYes(int resultCode) {
		return resultCode > 0;
	}

	protected void setAdapter() {
		adapter = BluetoothAdapter.getDefaultAdapter();
	}

	protected void setupBluetoothDetection() {
		registerListeners();
	}

	protected BluetoothDevice getRemoteDevice(Intent intent) {
		return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	}

	protected void makeDiscoverable(int timeDiscoverable) {
		int fullSessions = timeDiscoverable / 120;
		int remainingSession = timeDiscoverable % 120;

		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeDiscoverable);
		startActivityForResult(discoverableIntent, 0);
	}

	protected void registerListeners() {
		registerListener(BluetoothDevice.ACTION_FOUND);
		registerListener(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

	}

	protected void registerListener(String action) {
		IntentFilter filter = new IntentFilter(action);
		registerReceiver(discoverReceiver, filter);
	}

	protected void ensureBluetoothDiscoverability() {

		try {

			IBluetooth mBtService = getIBluetooth();

			Log.d("TESTE", "Ensuring bluetoot is discoverable");
			if (mBtService.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
				Log.e("TESTE", "Device was not in discoverable mode");
				try {
					mBtService.setDiscoverableTimeout(60);
					mBtService
							.setScanMode(
									BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,
									60);
				} catch (Exception e) {
					Log.e("TESTE", "Error setting bt discoverable", e);
				}
				Log.i("TESTE", "Device must be discoverable");
			} else {
				Log.e("TESTE", "Device already discoverable");
			}
		} catch (Exception e) {
			Log.e("TESTE", "Error ensuring BT discoverability", e);
		}

	}

	private IBluetooth getIBluetooth() {

		IBluetooth ibt = null;

		try {

			Class c2 = Class.forName("android.os.ServiceManager");

			Method m2 = c2.getDeclaredMethod("getService", String.class);
			IBinder b = (IBinder) m2.invoke(null, "bluetooth");

			Class c3 = Class.forName("android.bluetooth.IBluetooth");

			Class[] s2 = c3.getDeclaredClasses();

			Class c = s2[0];
			Method m = c.getDeclaredMethod("asInterface", IBinder.class);
			m.setAccessible(true);
			ibt = (IBluetooth) m.invoke(null, b);

		} catch (Exception e) {
			Log.e("BluetoothError", "Erroraco!!! " + e.getMessage());
		}

		return ibt;
	}

	protected boolean isPhone(BluetoothDevice device) {
		return device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE;
	}

	protected boolean isActionFound(String action) {
		return BluetoothDevice.ACTION_FOUND.equals(action);
	}

	protected boolean isDiscoveryFinished(String action) {
		return action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	}

	protected boolean isDiscoverableChange(String action) {
		return BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action);
	}

	protected boolean isDiscoverableState(int state) {
		return state == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
	}

	protected int getDiscoverState(Intent intent) {
		return intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,
				Short.MIN_VALUE);
	}

	protected int getBluetoothState(Intent intent) {
		return intent
				.getIntExtra(BluetoothAdapter.EXTRA_STATE, Short.MIN_VALUE);
	}

	protected BroadcastReceiver initReceiver() {
		return new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
			}
		};
	}

	protected boolean isBluetoothStateOff(int state) {
		return state == BluetoothAdapter.STATE_OFF;
	}

	protected final BroadcastReceiver discoverReceiver = initReceiver();
}
