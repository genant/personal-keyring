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
import it.personal.keyring.model.PersonalKey;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Displays a word and its definition.
 */  
public class DisplayKeyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_key);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Uri uri = getIntent().getData();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor == null) {
            finish();
        } else {
            cursor.moveToFirst();
            PersonalKey key = KeyringDatabase.getKeyFromCurrentRow(cursor);

            TextView name = (TextView) findViewById(R.id.display_name);
            TextView user = (TextView) findViewById(R.id.display_userName);
            TextView pwd = (TextView) findViewById(R.id.display_password);
            TextView definition = (TextView) findViewById(R.id.display_notes);
        	Button copyPwd = (Button) findViewById(R.id.display_copyButton); 
        	copyPwd.setOnClickListener(
    				new View.OnClickListener() {
    					@Override
    					public void onClick(View view) {
    						ClipboardManager clipboard = (ClipboardManager)
    						        getSystemService(Context.CLIPBOARD_SERVICE);
    						String keyName = ((TextView) findViewById(R.id.display_name)).getText().toString();
    						String password = ((TextView) findViewById(R.id.display_password)).getText().toString();
    						ClipData clip = ClipData.newPlainText(keyName, password);
    						clipboard.setPrimaryClip(clip);
    					}
    				});
    		
            name.setText(key.Name);
            user.setText(key.Username);
            try {
            	pwd.setText(SimpleCrypto.decrypt(
            			((KeyringApp)getApplicationContext())
            			.getCurrentUser()
            			.getPasswordSalt(), 
            			key.Password));
            } catch (Exception ex) {
				pwd.setText("**********");
				copyPwd.setEnabled(false); // disable if decrypt fails!
				Resources res = getResources();
				Toast.makeText(DisplayKeyActivity.this,
						res.getString(R.string.invalid_master_key),
						Toast.LENGTH_SHORT).show();

				Log.v("Decripting Pwd", ex.getMessage());
            }
            definition.setText(key.Notes);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.display_menu, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            case android.R.id.home:
            	onGoBack();
                return true;
            case R.id.action_edit_word:
            	onEditWord();
                return true;
            case R.id.action_delete_word:
            	deleteKey();
            	return true;
            default:
                return false;
        }
    }

	private void onEditWord() {
		Intent intent = new Intent(this, EditKeyActivity.class);
		intent.setData(getIntent().getData());
		
		startActivity(intent);
	}

    private void onGoBack() {
        Intent intent = new Intent(this, ListKeyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
	void deleteKey() {
        //Ask the user if they want to quit
        new AlertDialog.Builder(this)
        .setTitle(R.string.deleting_title)
        .setMessage(R.string.deleting_message)
        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Stop the activity
                //YourClass.this.finish();  
        		KeyringDatabase db = new KeyringDatabase(DisplayKeyActivity.this);
        		Uri uri = DisplayKeyActivity.this.getIntent().getData();
        		db.delete(uri.getLastPathSegment());
        		
        		// returns to the list
    			onGoBack();
        		
            }

        })
        .setNegativeButton(R.string.action_cancel, null)
        .show();
	}
}


