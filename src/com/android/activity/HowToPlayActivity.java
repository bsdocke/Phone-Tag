package com.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HowToPlayActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.how_to_play);
	}
	
	public void onBackPressed(){
		Intent loadFirstScreen = new Intent(this, StartJoinActivity.class);
		startActivity(loadFirstScreen);
	}
	
	public void goToHowToSetUpActivity(View view) {
		Intent loadHowToSetUp = new Intent(this, HowToSetUpActivity.class);
		startActivity(loadHowToSetUp);
	}
	
	public void goToHowToPlayGameActivity(View view) {
		Intent loadHowToPlayGame = new Intent(this, HowToGameActivity.class);
		startActivity(loadHowToPlayGame);
	}
	
	public void goToHowToGainPointsActivity(View view) {
		Intent loadHowToGainPoints = new Intent(this, HowToGainPointsActivity.class);
		startActivity(loadHowToGainPoints);
	}

}
