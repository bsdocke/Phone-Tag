package fitnessapps.spacerayders.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fitnessapps.spacerayders.data.GlobalState;

public class FinalScoreActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_score);
		setBackground();
	}

	@Override
	public void onStart() {
		super.onStart();
		init();
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
	}

	private void init() {
		TextView scoreView = (TextView) findViewById(R.id.scoreList);
		scoreView.setText(Integer.toString(GlobalState.myScore));
	}

	private void setBackground() {
		RelativeLayout relLay = (RelativeLayout) findViewById(R.id.scoreLayout);
		Drawable bg = relLay.getBackground();
		bg.setAlpha(100);
	}

	@Override
	public void onBackPressed() {
		gotoSplashActivity();
	}

	private void gotoSplashActivity() {
		Intent restart = new Intent(this, SplashActivity.class);
		startActivity(restart);
	}
}
