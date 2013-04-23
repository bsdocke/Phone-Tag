package fitnessapps.spacerayders.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DirectionsActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directions);
	}
	
	@Override
	public void onBackPressed() {
		Intent restart = new Intent(this, SplashActivity.class);
		startActivity(restart);
	}

}
