/*
 * Copyright 2014 Antonello Genuario
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
import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class ConfirmLoginActivity extends Activity {
	public static final String REQUEST_CODE = "req-code";

	// Values for email and password at the time of the login attempt.
	private String mPassword;

	// UI references.
	private EditText mPasswordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirm_login);

		// Set up the login form.
		mPasswordView = (EditText) findViewById(R.id.activity_confirm_password);
		int authMode = ((KeyringApp)getApplicationContext()).getAuthenticationMode();
		if(authMode == KeyringApp.AUTH_MODE_PIN) {
			// change the input mode
			mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
			mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
		} else {
			// default!
			mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		}

		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) 
			{
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false; 
			}
		});
		findViewById(R.id.activity_confirm_sign_in_button).setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

	}

	protected void attemptLogin() {
		boolean success = false;
		mPasswordView.setError(null);
		mPassword = mPasswordView.getText().toString();
		KeyringApp app = (KeyringApp)getApplicationContext();
		UserProfile user = app.getCurrentUser();
		String pwd;
		try {
			pwd = SimpleCrypto.encrypt(user.getPasswordSalt(), mPassword);
			if (TextUtils.equals(pwd, user.getPassword())) {
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (success) {
			setResult(Activity.RESULT_OK);
			finish();
		} else {
			mPasswordView
					.setError(getString(R.string.error_incorrect_password));
			mPasswordView.requestFocus();
		}
	}

	
}
