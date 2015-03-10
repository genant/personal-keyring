/*
 * Copyright (C) 2014 Antonello Genuario
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.personal.keyring;

import it.personal.keyring.common.KeyXmlParser;

import java.io.File;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class OpenFileActivity extends FileListActivity {
	
	public static final String START_PATH = "START_PATH";
	public static final String RESULT_PATH = "RESULT_PATH";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_file);
		setFilter("xml");
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(getString(R.string.import_xml));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
		ListView listView = (ListView)findViewById(R.id.open_file_list);
		listView.setEmptyView(findViewById(R.id.open_file_empty_list_item));

		setListView(listView);
		String startPath = getIntent().getStringExtra(START_PATH);
		if(startPath != null) {
			setCurrentFolder(new File(startPath));
		}
	}

	@Override
	boolean OnFileSelectionChanging(File file) {
		if(file.isDirectory() == false) {
			getIntent().putExtra(RESULT_PATH, file.getPath());

			// check if the file i password protected, if yes, require user input the password
			KeyXmlParser parser = new KeyXmlParser();
			if(parser.isProtected(file.getPath())) {
				Intent intent = new Intent(OpenFileActivity.this, PasswordRequestActivity.class);
				startActivityForResult(intent, KeyringApp.REQUEST_ENCRYPT_PWD);

				return false;
			}

			
			setResult(RESULT_OK, getIntent());
			finish();
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == KeyringApp.REQUEST_ENCRYPT_PWD && resultCode == RESULT_OK) {
			getIntent().putExtra(PasswordRequestActivity.PASSWORD, 
					data.getStringExtra(PasswordRequestActivity.PASSWORD));
			setResult(RESULT_OK, getIntent());
			finish();
		}
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open_file, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.open_file_action_cancel:
				setResult(RESULT_CANCELED, getIntent());
				finish();
                return true;
            case android.R.id.home:
            	goBack();
				return true;
            case R.id.open_file_action_settings:
            	showSettings();
            default:
                return false;
        }
    }

    private void showSettings() {
		Intent intent = new Intent(OpenFileActivity.this, ImportSettingsActivity.class);
		startActivityForResult(intent, 0);
		
	}
}
