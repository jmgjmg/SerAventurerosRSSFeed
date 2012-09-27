package com.tumaku.async;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

public class ConfigActivity extends Activity {
	  public static final int DIR_CALL = 2;
	  private String currentDir;
	  private TextView dirTextView;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.config);
	    Intent intent = getIntent();

	    boolean flag= intent.getBooleanExtra(this.getString(R.string.wifi), false);
	    ((CheckBox)findViewById(R.id.wifi)).setChecked(flag);
	    flag= intent.getBooleanExtra(this.getString(R.string.download), false);
	    ((RadioButton)findViewById(R.id.download)).setChecked(flag);
	    ((RadioButton)findViewById(R.id.play)).setChecked(!flag);
	    dirTextView= (TextView)findViewById(R.id.currentDir);
	    currentDir = intent.getStringExtra(this.getString(R.string.downloaddir));
	    if (currentDir==null) currentDir=Environment.getExternalStorageDirectory().getPath();
	    dirTextView.setText(currentDir);
	  } 
	  
	public void onRadioButtonClicked(View view) {
		//Do nothing
	}
	
	
	public void onButtonClicked(View view) {
		switch (view.getId()) {
			case R.id.dirButton:
	        	 Intent intentDir = new Intent(this, com.tumaku.async.DirectoryList.class);
	        	 intentDir.putExtra(this.getString(R.string.downloaddir), currentDir); 
	        	 startActivityForResult(intentDir, DIR_CALL);				
	        	 return;
			case R.id.cancel:
				finish();
				return;
			case R.id.ok:
		        Intent resultIntent = new Intent();
		        resultIntent.putExtra(this.getString(R.string.wifi), ((CheckBox)findViewById(R.id.wifi)).isChecked()); 
		        resultIntent.putExtra(this.getString(R.string.download), ((RadioButton)findViewById(R.id.download)).isChecked()); 
		        resultIntent.putExtra(this.getString(R.string.downloaddir), currentDir); 
		        this.setResult(RESULT_OK, resultIntent);
		        finish();
			    return;
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    super.onActivityResult(requestCode, resultCode, data); 
	    // See which child activity is calling us back.
	    switch (requestCode) {
	        case DIR_CALL:
	            if (resultCode == RESULT_CANCELED){
	            	//DO NOTHING
 	            } 
	            else {
	            	currentDir = data.getStringExtra(this.getString(R.string.downloaddir));  
	            	dirTextView.setText(currentDir);
	            }
	        	return;
	        default:
	            break;
	    }
	}
  
	

}