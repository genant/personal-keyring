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

import it.personal.keyring.common.*;
import it.personal.keyring.model.PersonalKey;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditKeyActivity extends Activity {
	private EditText editName;
	private EditText editNotes;
	private EditText editPassword;
	private EditText editUsername;
	private String _id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_key);

		// Show the Up button in the action bar.
		setupActionBar();

		editName = (EditText) findViewById(R.id.editName);
		editNotes = (EditText) findViewById(R.id.editNotes);
		editPassword = (EditText) findViewById(R.id.editPassword);
		editUsername = (EditText) findViewById(R.id.editUserName);

		Uri uri = getIntent().getData();
		Cursor cursor = getContentResolver().query(uri, null, null, null,
				null);

		if (cursor == null) {
			finish();
		} else {
			cursor.moveToFirst();
			PersonalKey key = KeyringDatabase.getKeyFromCurrentRow(cursor);

			_id = uri.getLastPathSegment().toString();
			editName.setText(key.Name);
			editUsername.setText(key.Username);
			editNotes.setText(key.Notes);

			String salt = ((KeyringApp) getApplicationContext())
					.getCurrentUser()
					.getPasswordSalt();
			try {
				editPassword.setText(SimpleCrypto.decrypt(salt, key.Password));
			} catch (Exception e) {
				editPassword.setText("**********");
				Resources res = getResources();
				Toast.makeText(EditKeyActivity.this,
						res.getString(R.string.invalid_master_key),
						Toast.LENGTH_SHORT).show();

				Log.v("Decripting Pwd", e.toString());
			}
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.edit_action_cancel:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// NavUtils.navigateUpFromSameTask(this);
			navigateUp();
			return true;
		case R.id.edit_action_save:
			if (!save()) {
				return false;
			}
			navigateUp();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean save() {
		PersonalKey key = new PersonalKey();
		key.Id = _id;
		key.Name = editName.getText().toString();
		key.Notes = editNotes.getText().toString();

		key.Username = editUsername.getText().toString();

		String salt = ((KeyringApp) getApplicationContext())
				.getCurrentUser()
				.getPasswordSalt();
		try {
			key.Password = SimpleCrypto.encrypt(salt, editPassword
					.getText().toString());
		} catch (Exception e) {
			key.Password = null;
			Resources res = getResources();
			Toast.makeText(EditKeyActivity.this,
					res.getString(R.string.invalid_master_key),
					Toast.LENGTH_SHORT).show();

			Log.v("Encripting Pwd", e.toString());
		}

		KeyringDatabase db = new KeyringDatabase(this);
		return db.update(key) > 0;
	}

	private void navigateUp() {
		Intent intent = new Intent(this, DisplayKeyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setData(getIntent().getData());
		startActivity(intent);
	}

}
