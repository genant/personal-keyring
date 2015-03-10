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

import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class PasswordEncryptActivity extends Activity {
	public static final String ENCRYPT_PASSWORD = "enc-password";
	private EditText mPasswordView;
	private EditText mConfirmPasswordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_encrypt);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		mPasswordView = (EditText)findViewById(R.id.password_encrypt_password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) 
			{
				if (id == R.id.password_encrypt_password || id == EditorInfo.IME_NULL) {
					return validatePasswords();
				}
				return false; 
			}
		});

		mConfirmPasswordView = (EditText)findViewById(R.id.password_encrypt_confirm_password);
		mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) 
			{
				if (id == R.id.password_encrypt_confirm_password || id == EditorInfo.IME_NULL) {
					return validatePasswords();
				}
				return false; 
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.password_request, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_cancel:
			setDialogResultCanceled();

			break;
		case R.id.action_ok:
			if(validatePasswords()) {
				setDialogResultOk();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean validatePasswords() {
		mPasswordView.setError(null);
		mConfirmPasswordView.setError(null);
		
		String password = mPasswordView.getText().toString();
		
		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			mPasswordView.requestFocus();
			return false;
		} else if (password.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			mPasswordView.requestFocus();
			return false;
		} else {
			String confirmPassword = mConfirmPasswordView.getText().toString();
			if(!TextUtils.equals(password,confirmPassword)) {
				mConfirmPasswordView.setError(getString(R.string.passwords_not_match));
				mConfirmPasswordView.requestFocus();
				return false;
			}
		}
		return true;
	}

	private void setDialogResultOk() {
		getIntent().putExtra(ENCRYPT_PASSWORD, mPasswordView.getText().toString());
		setResult(RESULT_OK, getIntent());
		finish();
	}

	private void setDialogResultCanceled() {
		setResult(RESULT_CANCELED, getIntent());
		finish();
	}

}
