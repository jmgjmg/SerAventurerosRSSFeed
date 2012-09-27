package com.tumaku.async;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DirectoryList extends ListActivity {

  
  private TextView textView;
  private List<File> dirList = new ArrayList<File>();
  private ArrayAdapter<File> adapter;
  private String currentDir;
  File m_dir;
/** Called when the activity is first created. */

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.directory);
    textView = (TextView) findViewById(R.id.currentDir);
    adapter = new ArrayAdapter<File>(this,
    		  android.R.layout.simple_list_item_1, android.R.id.text1, dirList);  
    setListAdapter(adapter);
    currentDir= getIntent().getStringExtra(this.getString(R.string.downloaddir));

    if ( currentDir != null )
        m_dir = new File( currentDir );
    else
    	m_dir = Environment.getExternalStorageDirectory();

    listDirs();
  }
  
  private void listDirs()
  {
	  adapter.clear();
	  textView.setText(m_dir.getPath());

      // Get files
      File[] files = m_dir.listFiles();

      // Add the ".." entry
      if ( m_dir.getParent() != null )
    	  dirList.add( new File("..") );

      if ( files != null )
      {
          for ( File file : files )
          {
              if ( !file.isDirectory() )
                  continue;

              dirList.add( file );
          }
      }

      Collections.sort( dirList, new Comparator<File>() {
              public int compare(File f1, File f2)
              {
                  return f1.getName().toLowerCase().compareTo( f2.getName().toLowerCase() );
              }
      } );
  }

  public void onListItemClick(ListView lv, View v, int index, long id) {
      if ( index < 0 || index >= dirList.size() )
          return;

      if ( dirList.get( index).getName().equals( ".." ) )
          m_dir = m_dir.getParentFile();
      else
          m_dir = dirList.get( index );
      listDirs();
  }
  
	public void onButtonClicked(View view) {
		switch (view.getId()) {
			case R.id.cancel:
				finish();
				return;
			case R.id.ok:
		        Intent resultIntent = new Intent();
		        resultIntent.putExtra(this.getString(R.string.downloaddir), m_dir.getPath()); 
		        this.setResult(RESULT_OK, resultIntent);
		        finish();
			    return;
		}
	}

} 