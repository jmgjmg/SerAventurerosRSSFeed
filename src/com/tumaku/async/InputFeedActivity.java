package com.tumaku.async;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class InputFeedActivity extends Activity {
	  private String currentFeed;
	  private EditText rssEditText;

	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.inputfeed);
	    Intent intent = getIntent();


	    rssEditText= (EditText)findViewById(R.id.currentFeed);
	    currentFeed = intent.getStringExtra(this.getString(R.string.currentFeed));
	    if (currentFeed==null) currentFeed=this.getString(R.string.rss_feed);
	    rssEditText.setText(currentFeed);
	  } 
	  
	public void onButtonClicked(View view) {
		switch (view.getId()) {
			case R.id.cancel:
				finish();
				return;
			case R.id.ok:
		        Intent resultIntent = new Intent();
		        resultIntent.putExtra(this.getString(R.string.currentFeed), rssEditText.getText().toString()); 
		        this.setResult(RESULT_OK, resultIntent);
		        finish();
			    return;
		}
	}
	
}