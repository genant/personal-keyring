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

import it.personal.keyring.common.SimpleCrypto;
import it.personal.keyring.model.UserProfile;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class PasswordRecover extends Activity {

	EditText mAnswer1;
	EditText mAnswer2;
	EditText mAnswer3;
	UserProfile mUser;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_recover);
		
		KeyringDatabaseOpenHelper db = new KeyringDatabaseOpenHelper(this);
		UserProfile user = db.getUserProfile();
		
		// Create an ArrayAdapter using the string array and a default
		// spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(getBaseContext(),
						R.array.security_questions_array,
						android.R.layout.simple_spinner_item);

		TextView question;
		question = (TextView) findViewById(R.id.password_recover_question_1);
		question.setText(adapter.getItem(user.getSecurityQuestion1()));
		
		question = (TextView) findViewById(R.id.password_recover_question_2);
		question.setText(adapter.getItem(user.getSecurityQuestion2()));
		
		question = (TextView) findViewById(R.id.password_recover_question_3);
		question.setText(adapter.getItem(user.getSecurityQuestion3()));
		
		mAnswer1 = (EditText) findViewById(R.id.password_recover_answer_1);
		mAnswer2 = (EditText) findViewById(R.id.password_recover_answer_2);
		mAnswer3 = (EditText) findViewById(R.id.answer_3);
		
		mUser = user;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_menu, menu);
		return true;
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
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case R.id.edit_action_save:
			if (attemptRecover()) {
				displayPassword();
				return true;
			}
			return false;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void displayPassword() {
		try {
			final String plainTextPassword = SimpleCrypto.decrypt(mUser.getPasswordSalt(), mUser.getPassword());
			AlertDialog alert = new AlertDialog.Builder(this)
			.setMessage(getString(R.string.display_password_message,
					new Object[] { plainTextPassword }))
			.setTitle(R.string.title_activity_password_recover)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	dialog.dismiss();
		        	
		        	// set the password in the activity results
		        	getIntent().putExtra("password", plainTextPassword);
					setResult(Activity.RESULT_OK, getIntent());
					finish();
		        }
		     })
		    .create();
		alert.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean attemptRecover() {
		String answer;
		int validCount = 0;

		// reset error states
		mAnswer1.setError(null);
		mAnswer2.setError(null);
		mAnswer3.setError(null);
		
		answer = mAnswer1.getText().toString();
		if(!TextUtils.isEmpty(answer)) {
			if(TextUtils.equals(mUser.getSecurityAnswer1(), answer)) {
				validCount++;
			} else {
				mAnswer1.setError(getString(R.string.answer_not_match));
			}
		}
		answer = mAnswer2.getText().toString();
		if(!TextUtils.isEmpty(answer)) {
			if(TextUtils.equals(mUser.getSecurityAnswer2(), answer)) {
				validCount++;
			} else {
				mAnswer2.setError(getString(R.string.answer_not_match));
			}
		}
		answer = mAnswer3.getText().toString();
		if(!TextUtils.isEmpty(answer)) {
			if(TextUtils.equals(mUser.getSecurityAnswer3(), answer)) {
				validCount++;
			} else {
				mAnswer3.setError(getString(R.string.answer_not_match));
			}
		}
		
		return validCount > 1;
	}

}
