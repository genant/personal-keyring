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

import it.personal.keyring.common.ImportArgs;
import it.personal.keyring.common.KeyXmlParser;
import it.personal.keyring.model.PersonalKey;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The main activity for the dictionary. Displays search results triggered by
 * the search dialog and handles actions from search suggestions.
 */
public class ListKeyActivity extends Activity {
	private ListView mListView;
	private TextView mEmptyView;
	//private int tileSize;
	//private LetterTileProvider tileProvider;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_key);
		mListView = (ListView) findViewById(R.id.list);
		mEmptyView = (TextView) findViewById(R.id.empty_list_item);
		mListView.setEmptyView(mEmptyView);
		handleIntent(getIntent());
		
		PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
	    
//		final Resources res = getResources();
//	    tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);
//
//	    tileProvider = new LetterTileProvider(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Because this activity has set launchMode="singleTop", the system
		// calls this method
		// to deliver the intent if this activity is currently the foreground
		// activity when
		// invoked again (when the user executes a search from this activity, we
		// don't create
		// a new instance of this activity, so the system delivers the search
		// intent here)
		handleIntent(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case KeyringApp.REQUEST_SETUP_PWD:
			if (resultCode == RESULT_OK) {
				onSetupCompleted();
				handleIntent(getIntent());
			} else {
				finish(); // the user cancels wizard prompts
			}
			break;
		case KeyringApp.REQUEST_MAIN_PWD:
			if (resultCode == RESULT_OK) {
				handleIntent(getIntent());
			} else {
				finish(); // the user cancels password prompts
			}
			break;
		case KeyringApp.REQUEST_SAVE:
			if (resultCode == RESULT_OK) {
				exportXml(data);
			}
			break;
		case KeyringApp.REQUEST_LOAD:
			if (resultCode == RESULT_OK) {
				importXml(data);
				queryAll(); // refresh key list
			}
			break;
		case KeyringApp.REQUEST_CONFIRM_CREDENTIAL:
			if(resultCode == RESULT_OK) {
				Intent setupIntent = new Intent(this, SetupActivity.class);
				startActivityForResult(setupIntent, KeyringApp.REQUEST_CHANGE_PWD);
			}
			break;
		case KeyringApp.REQUEST_CHANGE_PWD:
			if(resultCode == RESULT_OK) {
				MessageBox.show(this, R.string.change_password, R.string.change_password_succeed);
			}
			break;
		}
	}

	private void onSetupCompleted() {
		KeyringApp app = (KeyringApp) getApplicationContext();
		app.setFirstRun(false);
	}

	private void handleIntent(Intent intent) {
		KeyringApp app = (KeyringApp) getApplicationContext();
		if (app.isFirstRun()) {
			Intent setupIntent = new Intent(this, SetupActivity.class);
			startActivityForResult(setupIntent, KeyringApp.REQUEST_SETUP_PWD);
		} else {
			if (app.isAuthenticated()) {
				if (Intent.ACTION_VIEW.equals(intent.getAction())) {
					// handles a click on a search suggestion; launches activity
					// to show word
					Intent wordIntent = new Intent(this,
							DisplayKeyActivity.class);
					wordIntent.setData(intent.getData());
					startActivity(wordIntent);
				} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
					// handles a search query
					String query = intent.getStringExtra(SearchManager.QUERY);
					filter(query);
				} else if (Intent.ACTION_MAIN.equals(intent.getAction())
						|| intent.getAction() == null) {
					queryAll();
				}
			} else {
				Intent loginIntent = new Intent(this, LoginActivity.class);
				startActivityForResult(loginIntent, KeyringApp.REQUEST_MAIN_PWD);
			}
		}
	}

	/**
	 * Select all keys in the database
	 */
	private void queryAll() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);

		mEmptyView.setText(getString(R.string.wellcome_instructions));

		CursorLoader loader = new CursorLoader(this,
				KeyringProvider.CONTENT_URI, 
				null, null, null, "order by " + KeyringDatabase.KEY_NAME);

		populateListView(loader);
	}

	/**
	 * Searches the dictionary and displays results for the given query.
	 * 
	 * @param query
	 *            The search query
	 */
	private void filter(String query) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mEmptyView.setText(getString(R.string.no_results,
				new Object[] { query }));

		CursorLoader loader = new CursorLoader(this,
				KeyringProvider.CONTENT_URI, null, null,
				new String[] { query }, null);

		populateListView(loader);
	}

	private void populateListView(CursorLoader loader) {
		Cursor cursor = loader.loadInBackground();
		
		// Define the on-click listener for the list items
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Build the Intent used to open WordActivity with a
				// specific word Uri
				Intent wordIntent = new Intent(getApplicationContext(),
						DisplayKeyActivity.class);
				Uri data = Uri.withAppendedPath(
						KeyringProvider.CONTENT_URI, String.valueOf(id));
				wordIntent.setData(data);
				startActivity(wordIntent);
			}
		});

		// Specify the columns we want to display in the result
		String[] from = new String[] { 
//				KeyringDatabase.USERNAME,
				KeyringDatabase.KEY_NAME,
				KeyringDatabase.USERNAME,
				KeyringDatabase.KEY_NOTES };

		// Specify the corresponding layout elements where we want the
		// columns to go
		int[] to = new int[] { 
//				R.id.key_row_icon, 
				R.id.name, 
				R.id.key_row_username, 
				R.id.notes };

		// Create a simple cursor adapter for the definitions and apply them
		// to the ListView
		SimpleCursorAdapter words = 
				new SimpleCursorAdapter(this, R.layout.search_result, cursor, from, to);

//		words.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//			
//			@Override
//			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//		       if(view.getId() == R.id.key_row_icon){
//		           //...
//		    	   String keyName = cursor.getString(1); // KEY_NAME is the 2nd field in the cursor
//		    	   String userName = cursor.getString(columnIndex);
//		    	   if(TextUtils.isEmpty(userName)) {
//		    		   userName = "username";
//		    	   }
//		    	   ((LetterTileView)view).setTileSize(tileSize, tileSize);
//		    	   ((LetterTileView)view).setTileText(userName, keyName);
//
//		           return true; //true because the data was bound to the view*/
//		       }
//				return false;
//			}
//		});
		mListView.setAdapter(words);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_menu, menu);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchView searchView = (SearchView) menu.findItem(R.id.search)
					.getActionView();
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			onSearchRequested();
			return true;
		case R.id.action_new:
			onAddNew();
			return true;
		case R.id.action_export_xml:
			onExportXml();
			return true;
		case R.id.action_import_xml:
			onImportXml();
			return true;
		case R.id.change_password:
			changePassword();
			return true;
//		case R.id.action_share:
//			shareIt();
//			return true;
		case R.id.action_quit:
			((KeyringApp) getApplicationContext()).setCurrentUser(null);
			finish();
			return true;
        case android.R.id.home:
        	queryAll();
        	return true;
		default:
			return false;
		}
	}

//	private void shareIt() {
//		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//		sharingIntent.setType("text/plain");
//		String shareBody = "Here is the share content body";
//		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
//		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//		startActivity(Intent.createChooser(sharingIntent, "Share via"));
//	}

	private void changePassword() {
		Intent loginIntent = new Intent(this, ConfirmLoginActivity.class);
		loginIntent.putExtra(ConfirmLoginActivity.REQUEST_CODE, KeyringApp.REQUEST_CONFIRM_CREDENTIAL);
		startActivityForResult(loginIntent, KeyringApp.REQUEST_CONFIRM_CREDENTIAL);
	}

	private void onImportXml() {
		if(isExternalStorageReadable()) {
			Intent intent = new Intent(ListKeyActivity.this, OpenFileActivity.class);
			startActivityForResult(intent, KeyringApp.REQUEST_LOAD);
		} else {
			Toast.makeText(ListKeyActivity.this,
					getString(R.string.external_storage_not_found),
					Toast.LENGTH_LONG).show();

			Log.w(KeyringDatabaseOpenHelper.TAG, "external store not mounted!");
		}
	}

	private void importXml(Intent data) {
		ImportArgs request = new ImportArgs();
		request.setFileName(data.getStringExtra(OpenFileActivity.RESULT_PATH));
		if(data.hasExtra(PasswordRequestActivity.PASSWORD)) {
			request.setPassword(data.getStringExtra(PasswordRequestActivity.PASSWORD));
		}
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(sharedPref.contains(ImportSettingsActivity.PREF_DUPLICATE)) {
			request.setDuplicateAction(Integer.parseInt(sharedPref.getString(ImportSettingsActivity.PREF_DUPLICATE, "-1")));
		}

		if(sharedPref.contains(ImportSettingsActivity.PREF_DELETE)) {
			request.setCleanBeforeStart(sharedPref.getBoolean(ImportSettingsActivity.PREF_DELETE, false));
		}

		request.setMainPassword(((KeyringApp) getApplicationContext()).getCurrentUser().getPasswordSalt());
		
		KeyringDatabase dba = new KeyringDatabase(this);
		try {
			dba.importXml(request);
		} catch (Exception e) {
			Resources res = getResources();
			MessageBox.show(ListKeyActivity.this, 
					res.getString(R.string.invalid_password), 
					res.getString(R.string.invalid_decrypt_pwd));

			Log.v("Decripting Pwd", e.toString());

		}
	}


	private void onExportXml() {
		if(isExternalStorageWritable()) {
			Intent intent = new Intent(ListKeyActivity.this, SaveFileActivity.class);
			startActivityForResult(intent, KeyringApp.REQUEST_SAVE);
		} else {
			Toast.makeText(ListKeyActivity.this,
					getString(R.string.external_storage_read_only),
					Toast.LENGTH_LONG).show();

			Log.w(KeyringDatabaseOpenHelper.TAG, "external storeg read-only!");
		}

	}

	private void exportXml(Intent data) {
		String filePath = data.getStringExtra(SaveFileActivity.RESULT_PATH);
		String encryptPassword = null;
		if(data.hasExtra(PasswordEncryptActivity.ENCRYPT_PASSWORD)) {
			// recupera la password indicata opzionalemente dall'utente
			encryptPassword = data.getStringExtra(PasswordEncryptActivity.ENCRYPT_PASSWORD);
		}
		KeyringDatabase dba = new KeyringDatabase(this);
		List<PersonalKey> items;
		try {
			items = dba
					.exportXml(((KeyringApp) getApplicationContext())
							.getCurrentUser()
							.getPasswordSalt());

			KeyXmlParser xml = new KeyXmlParser();
			xml.setEncryptPassword(encryptPassword);
			xml.export(items, filePath);
		} catch (Exception e) {
			Resources res = getResources();
			Toast.makeText(ListKeyActivity.this,
					res.getString(R.string.invalid_master_key),
					Toast.LENGTH_SHORT).show();

			Log.v("Decripting Pwd", e.toString());
		}
	}


	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}


	private void onAddNew() {
		Intent editIntent = new Intent(ListKeyActivity.this,
				InsertKeyActivity.class);
		startActivity(editIntent);
	}
}
