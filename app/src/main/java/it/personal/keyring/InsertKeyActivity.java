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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class InsertKeyActivity extends Activity {
	private EditText editName;
	private EditText editNotes;
	private EditText editPassword;
	private EditText editUsername;

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
        
	}

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
			Toast.makeText(InsertKeyActivity.this,
					res.getString(R.string.invalid_master_key),
					Toast.LENGTH_SHORT).show();

			Log.v("Encripting Pwd", e.toString());
		}

		KeyringDatabase db = new KeyringDatabase(this);
		return db.insert(key) > 0;
	}

	private void navigateUp() {
		Intent intent = new Intent(this, ListKeyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
}
