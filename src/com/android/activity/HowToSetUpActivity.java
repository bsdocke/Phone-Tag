package com.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class HowToSetUpActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.how_to_setup);
	}
	
	public void onBackPressed(){
		Intent loadPreviousScreen = new Intent(this, HowToPlayActivity.class);
		startActivity(loadPreviousScreen);
	}

}
