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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

public class SaveFileActivity extends FileListActivity {

	public static final String RESULT_PATH = "RESULT_PATH";
	private String selectedFileName;
	EditText editFileName;
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_file);
		setFilter("xml");

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(getString(R.string.export_xml));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

		ListView listView = (ListView)findViewById(R.id.save_file_list);
		listView.setEmptyView(findViewById(R.id.save_file_empty_list_item));
		setListView(listView);
		editFileName = (EditText)findViewById(R.id.save_file_edit_filename);
		editFileName.setText("key_" + new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime()) + ".xml");
	}

	@Override
	protected boolean OnFileSelectionChanging(File file) {
		if(!file.isDirectory()) {
			selectedFileName = file.getPath(); 
			setDialogResultOk();
		}
		return true;
	}

	private void setDialogResultOk() {
		CheckBox optEncrypt = (CheckBox)findViewById(R.id.save_file_encrypt_option);
		if(optEncrypt.isChecked()) {
			Intent intent = new Intent(SaveFileActivity.this, PasswordEncryptActivity.class);
			intent.putExtra(RESULT_PATH, selectedFileName);
			startActivityForResult(intent, KeyringApp.REQUEST_ENCRYPT_PWD);
		} else {
			getIntent().putExtra(RESULT_PATH, selectedFileName);
			setResult(RESULT_OK, getIntent());
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case KeyringApp.REQUEST_ENCRYPT_PWD:
			if(resultCode == RESULT_OK) {
				setResult(RESULT_OK, data);
				finish();
			}
			break;

		default:
			break;
		}
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	private void setDialogResultCanceled() {
		setResult(RESULT_CANCELED, getIntent());
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save_file, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save_file_action_save:
			if(validateFilename()) {
				selectedFileName = getCurrentFolder().getPath() + "/" + editFileName.getText().toString();
				setDialogResultOk();
			}
			break;
        case android.R.id.home:
        	goBack();
        	break;
		default:
			setDialogResultCanceled();
			break;
		}
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	private boolean validateFilename() {
		String fileName = editFileName.getText().toString();
		if(TextUtils.isEmpty(fileName)) 
			return false;
		
		if(!fileName.endsWith(".xml")) {
			// aggiunge l'estensione del file se non disponibile
			editFileName.setText(fileName + ".xml");
		}
		return true;
	}
}
