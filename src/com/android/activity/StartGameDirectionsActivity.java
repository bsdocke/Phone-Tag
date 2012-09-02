package com.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartGameDirectionsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_game_directions);	
	}
	
	@Override
	public void onStop(){
		super.onStop();	
	}
	
	public void onOkHandler(View view){
		Intent loadLobby = new Intent(this, LobbyServerActivity.class);		
		startActivity(loadLobby);
	}
	
	
}
