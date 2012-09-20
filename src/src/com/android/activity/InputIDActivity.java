package com.android.activity;


import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.data.GlobalState;

public class InputIDActivity extends Activity {
	public final static String ID_NUMBER_STRING = "com.activities.EnterIDActivity.IDNUMBER";
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_id);
        
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }
    
    public void enterID(View view){
    	Intent loadOptions = new Intent(this, StartJoinActivity.class);
    	EditText inputField = (EditText) findViewById(R.id.editview);
    	String idNumber = inputField.getText().toString();
    	
    	loadOptions.putExtra(ID_NUMBER_STRING, idNumber);
    	GlobalState.playerIDNumber = idNumber;
    	startActivity(loadOptions);
    }
    
    public void exitApp(View view){
    	finish();
    }
    
    public void onBackPressed(){
    	moveTaskToBack(true);
    }
}