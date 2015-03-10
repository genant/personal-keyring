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

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class PasswordRequestActivity extends Activity {
	public static final String PASSWORD = "enc-password";
	
	private EditText mPasswordView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_request);
		
		mPasswordView = (EditText)findViewById(R.id.password_request_password);
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
		case R.id.action_ok:
			setDialogResultOk();
			break;

		default:
			setDialogResultCanceled();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setDialogResultOk() {
		getIntent().putExtra(PASSWORD, mPasswordView.getText().toString());
		setResult(RESULT_OK, getIntent());
		finish();
	}

	private void setDialogResultCanceled() {
		setResult(RESULT_CANCELED, getIntent());
		finish();
	}

}
