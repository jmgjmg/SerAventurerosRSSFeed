package com.tumaku.async;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SerAventurerosRSSFeed extends ListActivity {
  public static final String EXTRA_URL = "EXTRA_URL";
  public static final int CONFIG_CALL = 1;
  public static final int DIR_CALL = 2;

  private TextView textView;
  private boolean isDownloadSelected = true;
  private boolean isWifiSelected= true;
  private List<RSSParser.Entry> entryList = new ArrayList<RSSParser.Entry>();
  private ArrayAdapter<RSSParser.Entry> adapter;
  private String currentDir;
  private String currentFeed;
  
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    this.textView = (TextView) findViewById(R.id.TextView01);
    adapter = new ArrayAdapter<RSSParser.Entry>(this,
    		  android.R.layout.simple_list_item_1, android.R.id.text1, entryList);  
    setListAdapter(adapter);
    SharedPreferences settings = getSharedPreferences(this.getString(R.string.prefs_file), 0);
    isDownloadSelected = settings.getBoolean(this.getString(R.string.download), false);
    isWifiSelected = settings.getBoolean(this.getString(R.string.wifi), true);
    currentDir = settings.getString(this.getString(R.string.downloaddir), Environment.getExternalStorageDirectory().getPath());
    currentFeed = settings.getString(this.getString(R.string.currentFeed), this.getString(R.string.rss_feed));
    readWebpage();
  }

  @Override
  public void onListItemClick(ListView lv, View v, int index, long id){
	    // String item = (String) getListAdapter().getItem(index).toString();
	    String url = ((RSSParser.Entry) getListAdapter().getItem(index)).getLink();
	    String mimeType= ((RSSParser.Entry) getListAdapter().getItem(index)).getMimeType();
	    
	    ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
	  	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	  	boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
	  	boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
	  	
	  	if (isWifiSelected && !isWiFi) {
		    Toast.makeText(this, "'WiFi only' selected in main menu but current network is not WiFi.\nYou can change settings in Configuration menu.", Toast.LENGTH_LONG).show();	  		
	  	} else {
	  		if (isMobile) alertDialogMethod(url,mimeType);
	  		else downloadOrPlayFile(url,mimeType);
	  	}
	  /*  
	    Intent intent = new Intent(this, BackgroundAudioActivity.class);
	    intent.putExtra(EXTRA_URL, url);
	    startActivity(intent);
	   */ 
	  }
  
  
  private void downloadOrPlayFile(String url, String mimeType) {
		Uri uri= Uri.parse(url);
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(uri, mimeType);
		if (!isDownloadSelected) {
			startActivity(intent);
		} else {
		    Toast.makeText(this, "Downloading file " + uri.getLastPathSegment() + "\nCheck download folder " + currentDir +
		    		" in your device", Toast.LENGTH_LONG).show();
			DownloadFileTask task = new DownloadFileTask();
		    task.execute(new String[] {url });	    
		}	  
  }
  
  
  private class DownloadFileTask extends AsyncTask<String, Void, Void > {

	    
	    @Override
	    protected Void doInBackground(String... urls) {
	      for (String url : urls) {
	  	    try {
	  	    	Uri uri=Uri.parse(url);
	  	        //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
	  	    	File path = new File(currentDir);
				new DefaultHttpClient().execute(new HttpGet(url))
		        .getEntity().writeTo(new FileOutputStream(new File(path,uri.getLastPathSegment())));
		    } catch (Exception e) {
		    	Log.e("ERROR", "Error trying to download remote file");
		    	Log.e("ERROR",e.getMessage());
		    	Log.e("ERROR",e.toString());
		    }
	      }
	      return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {	
		    toastDownload();	  	  
	    }
	  }

  private void toastDownload(){
	  // Toast.makeText(this, "File download ended", Toast.LENGTH_LONG).show();
  }
  
  private class DownloadWebPageTask extends AsyncTask<String, Void, List<RSSParser.Entry> > {

    RSSParser myParser = new RSSParser();
    
    @Override
    protected List<RSSParser.Entry> doInBackground(String... urls) {
      List<RSSParser.Entry> entryList = new ArrayList<RSSParser.Entry>();
      for (String url : urls) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
          HttpResponse execute = client.execute(httpGet);
          InputStream content = execute.getEntity().getContent();

         /* BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
          String s = "";
          while ((s = buffer.readLine()) != null) {
            response += s;
          }
          */
          entryList = myParser.parse(content);
          
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return entryList;
    }

    @Override
    protected void onPostExecute(List<RSSParser.Entry> result) {	
      textView.setText("PARSED");
      entryList= result;
      adapter.clear();
      // adapter.addAll(result); not supported in Android 10
	  Iterator <RSSParser.Entry> iterator = result.iterator();
  	  while (iterator.hasNext()) {
  		  adapter.add(iterator.next());
  	  }

 
	  Iterator <RSSParser.Entry> iterator2 = result.iterator();
  	  while (iterator2.hasNext()) {
  		RSSParser.Entry myEntry = iterator2.next();
  		textView.setText(textView.getText() + myEntry.getTitle() +"\n" + myEntry.getSummary() + "\n" + myEntry.getLink() + 
  				"\n++++++++++++++++++++++++++++++++++++++++++++\n");
  	  }

	  if (result.isEmpty()) textView.setText("Problem reading RSS feed");
	  else textView.setText("RSS feed successfully parsed");

    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main_menu, menu);
      return true;
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {

          case R.id.update:
        	  readWebpage();  
        	  return true;
        	  
          case R.id.config:   
        	  Intent intent = new Intent(this, com.tumaku.async.ConfigActivity.class);
        	  intent.putExtra(this.getString(R.string.wifi), isWifiSelected); 
        	  intent.putExtra(this.getString(R.string.download), isDownloadSelected); 
        	  intent.putExtra(this.getString(R.string.downloaddir), currentDir); 
        	  intent.putExtra(this.getString(R.string.currentFeed), currentFeed); 
        	  startActivityForResult(intent, CONFIG_CALL);
        	  return true;
        
          case R.id.about:
      		AlertDialog.Builder aboutDialogBuilder = new AlertDialog.Builder(this);
    		// set title
      		aboutDialogBuilder.setTitle("About");
      		aboutDialogBuilder
    			.setMessage("This andorid application uses the RSS feed created by Radio Asturias (Cadena Ser) "
    					+ "in iVoox at http://www.ivoox.com/" +
    	    			"\n - Download/Play:  download the selected podcast file for later play (saved to 'download' folder in Android device) or play immediately"+ 
    	    			"\n - Only Wifi: prevents download or play of podcast files when the Android device is not connected to a WiFi network." +
    	    		    "\nCreated by @tumaku_")
    			.setCancelable(true).setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog,int id) {
    					//Do  nothing
    				}
    			  });
		
   			// create alert dialog
   			AlertDialog aboutDialog = aboutDialogBuilder.create();
   			aboutDialog.show();
   			return true;
              		 
          default:
              return super.onOptionsItemSelected(item);
      }
  }
  
  protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    super.onActivityResult(requestCode, resultCode, data); 
	    // See which child activity is calling us back.
	    switch (requestCode) {
	        case CONFIG_CALL:
	            // This is the standard resultCode that is sent back if the
	            // activity crashed or didn't doesn't supply an explicit result.
	            if (resultCode == RESULT_CANCELED){
	            	//DO NOTHING
 	            } 
	            else {
	            	isDownloadSelected = data.getBooleanExtra(this.getString(R.string.download),isDownloadSelected);
	            	isWifiSelected= data.getBooleanExtra(this.getString(R.string.wifi),isWifiSelected);
	            	currentDir = data.getStringExtra(this.getString(R.string.downloaddir));
	            	String currentFeed2 = data.getStringExtra(this.getString(R.string.currentFeed));
	            	if (! currentFeed2.equalsIgnoreCase(currentFeed)) readWebpage();
	                SharedPreferences settings = getSharedPreferences(this.getString(R.string.prefs_file), 0);
	                SharedPreferences.Editor editor = settings.edit();
	                editor.putBoolean(this.getString(R.string.download),isDownloadSelected);
	                editor.putBoolean(this.getString(R.string.wifi),isWifiSelected);
	                editor.putString(this.getString(R.string.downloaddir),currentDir);
	                editor.putString(this.getString(R.string.currentFeed),currentFeed);
	                editor.commit();	            	
	            }
	        	
	        default:
	            break;
	    }
	}
  

  
  	private void alertDialogMethod(String url, String mimeType) {
  		final String dialogUrl =url;
  		final String dialogMimeType = mimeType;
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Warning");
		
		// set dialog message
		alertDialogBuilder
			.setMessage("You are connecting through a mobile network. Remember that depending on your data plan this may result in " +
	    			"an expensive data bill.\nPress '"+ this.getString(R.string.no)+ "' to cancel this action.")
			.setCancelable(true).setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					downloadOrPlayFile(dialogUrl,dialogMimeType);
				}
			  })
			.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					// if this button is clicked, just close
					// the dialog box and do nothing
				}
			});

		
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
	}
  
  public void readWebpage() {
	adapter.clear();
    DownloadWebPageTask task = new DownloadWebPageTask();
    task.execute(new String[] { currentFeed });
  }
} 