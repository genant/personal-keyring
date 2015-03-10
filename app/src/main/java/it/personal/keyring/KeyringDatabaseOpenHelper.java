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

import it.personal.keyring.common.SessionIdentifierGenerator;
import it.personal.keyring.common.SimpleCrypto;
import it.personal.keyring.model.PersonalKey;
import it.personal.keyring.model.UserProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * This creates/opens the database.
 */
public class KeyringDatabaseOpenHelper extends SQLiteOpenHelper {
    public static final String TAG = "KeysDatabase";
    public static final String DATABASE_NAME = "keys";
    public static final String KEYS_FTS_VIRTUAL_TABLE = "FTSKeys";
    public static final int DATABASE_VERSION = 2;


    private SQLiteDatabase mDatabase;

    /* Note that FTS3 does not support column constraints and thus, you cannot
     * declare a primary key. However, "rowid" is automatically used as a unique
     * identifier, so when making requests, we will use "_id" as an alias for "rowid"
     */
    private static final String KEYS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + KEYS_FTS_VIRTUAL_TABLE +
                " USING fts3 (" +
                KeyringDatabase.KEY_NAME + ", " +
                KeyringDatabase.KEY_NOTES + ", " +
                KeyringDatabase.PASSWORD + ", " +
                KeyringDatabase.USERNAME + ", " +
                KeyringDatabase.SCORE + " INTEGER);";
    private static final String USER_TABLE_CREATE = 
    		"CREATE TABLE " + UserProfile.TABLE + "(" +
    		UserProfile.COLUMN_ID + ", " +
			UserProfile.COLUMN_USER_NAME + ", " +
			UserProfile.COLUMN_PASSWORD + ", " +
			UserProfile.COLUMN_PASSWORD_SALT + ", " +
			UserProfile.COLUMN_QUESTION_ANSWER_1 + ", " +
			UserProfile.COLUMN_QUESTION_ANSWER_2 + ", " +
    		UserProfile.COLUMN_QUESTION_ANSWER_3 + ")";

    KeyringDatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        mDatabase.execSQL(KEYS_TABLE_CREATE);
        mDatabase.execSQL(USER_TABLE_CREATE);
    }

    
    /**
     * Starts a thread to load the database table with words
     */
    public void importKeys(List<PersonalKey> keys, int duplicateAction) throws IOException {
        Log.d(TAG, "Loading keys...");
        long id;
    	for (PersonalKey key : keys) {
    		if(duplicateAction == 1 || duplicateAction == 0) {
    			PersonalKey existingKey = getKeyByName(key.Name);
    			if(duplicateAction == 1) {
        			if(existingKey != null) {
        				deleteKey(key.Id);
        			}
                    id = insertKey(key);
                    if (id < 0) {
                        Log.e(TAG, "unable to add key: " + key.Name);
                    }
    			}
    			if(duplicateAction == 0) {
        			if(existingKey == null) {
                        id = insertKey(key);
                        if (id < 0) {
                            Log.e(TAG, "unable to add key: " + key.Name);
                        }
        			} else {
    					if(TextUtils.isEmpty(existingKey.Notes)) {
    						existingKey.Notes = key.Notes;
    					}
    					if(TextUtils.isEmpty(existingKey.Password)) {
    						existingKey.Password = key.Password;
    					}
    					if(TextUtils.isEmpty(existingKey.Username)) {
    						existingKey.Username = key.Username;
    					}
    					updateKey(existingKey);
        			}
    				
    			}
    		} else {
                id = insertKey(key);
                if (id < 0) {
                    Log.e(TAG, "unable to add key: " + key.Name);
                }
    		}
		}
        Log.d(TAG, "DONE loading keys.");
    }

    private PersonalKey getKeyByName(String keyName) {
    	PersonalKey key = null;
    	try {
	    	mDatabase = getReadableDatabase();
	    	String sql = "SELECT rowid as " + BaseColumns._ID +", * FROM " 
	    			+ KEYS_FTS_VIRTUAL_TABLE + " WHERE " + KeyringDatabase.KEY_NAME + "=?";
	    	Cursor cursor = mDatabase.rawQuery(sql, new String[] { keyName });
	    	
	    	if(cursor.moveToFirst()) {
	    		key = KeyringDatabase.getKeyFromCurrentRow(cursor);
	    	}
    	} finally {
    		mDatabase.close();
    	}
    	return key;
    }
    /**
     * Add a word to the dictionary.
     * @return rowId or -1 if failed
     */
    public long insertKey(PersonalKey key) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KeyringDatabase.KEY_NAME, key.Name);
        initialValues.put(KeyringDatabase.KEY_NOTES, key.Notes);
        initialValues.put(KeyringDatabase.USERNAME, key.Username);
        initialValues.put(KeyringDatabase.PASSWORD, key.Password);
    	
        long ret;
        try {
            mDatabase = getWritableDatabase();
            ret = mDatabase.insert(KEYS_FTS_VIRTUAL_TABLE, null, initialValues);
        } catch (Exception ex) {
        	Log.e("insertKey", ex.getMessage());
        	ret = -1;
        } finally {
        	mDatabase.close();
        }

        return ret;
    }

    public int updateKey(PersonalKey key) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KeyringDatabase.KEY_NAME, key.Name);
        initialValues.put(KeyringDatabase.KEY_NOTES, key.Notes);
        initialValues.put(KeyringDatabase.USERNAME, key.Username);
        initialValues.put(KeyringDatabase.PASSWORD, key.Password);

        try {
            mDatabase = getWritableDatabase();
            return mDatabase.update(KEYS_FTS_VIRTUAL_TABLE, initialValues, "rowid=?",
            		new String[] { key.Id });
        } catch (Exception ex) {
        	Log.e("update", ex.getMessage());
        	return -1;
        } finally {
        	mDatabase.close();
        }
    }
    
    public int deleteKey(String id) {
        try {
            mDatabase = getWritableDatabase();
            return mDatabase.delete(KEYS_FTS_VIRTUAL_TABLE, "rowid=?", new String[] { id });
        } catch (Exception ex) {
        	Log.e("deleteWord", ex.getMessage());
        	return -1;
        } finally {
        	mDatabase.close();
        }
    }

	public void deleteAll() {
        try {
            mDatabase = getWritableDatabase();
            mDatabase.delete(KEYS_FTS_VIRTUAL_TABLE, null, null);
        } catch (Exception ex) {
        	Log.e("deleteWord", ex.getMessage());
        } finally {
        	mDatabase.close();
        }
	}

    public List<PersonalKey> takeKeys(int skip, int count) {
        SQLiteDatabase db = this.getReadableDatabase();
        String qry = "SELECT rowid as " + BaseColumns._ID +", * FROM " + KEYS_FTS_VIRTUAL_TABLE;
        
        Log.e(KeyringDatabaseOpenHelper.TAG, qry);
        
        Cursor c = db.rawQuery(qry, null);
     
        if (c != null)
            c.moveToFirst();

        List<PersonalKey> result = new ArrayList<PersonalKey>();
        c.move(skip);
        do {
        	PersonalKey key = KeyringDatabase.getKeyFromCurrentRow(c);
        	result.add(key);
        } while(c.moveToNext() && result.size() < count);
        
        return result;
    }
    
    public long saveUserProfile(UserProfile profile, boolean create) throws Exception {
        SQLiteDatabase db = this.getWritableDatabase();

        if(create) {
	        // create a new random seed and set it to user profile
	        SessionIdentifierGenerator salt = new SessionIdentifierGenerator();
	        profile.setPasswordSalt(salt.nextSessionId());
        }
        ContentValues values = new ContentValues();
        values.put(UserProfile.COLUMN_USER_NAME, profile.getUserName());
        values.put(UserProfile.COLUMN_PASSWORD, SimpleCrypto.encrypt(profile.getPasswordSalt(), profile.getPassword()));
        values.put(UserProfile.COLUMN_PASSWORD_SALT, profile.getPasswordSalt());
        values.put(UserProfile.COLUMN_QUESTION_ANSWER_1, profile.getQA1());
        values.put(UserProfile.COLUMN_QUESTION_ANSWER_2, profile.getQA2());
        values.put(UserProfile.COLUMN_QUESTION_ANSWER_3, profile.getQA3());
     
        long user_id;
        if(create) {
	        // insert row
	        user_id = db.insert(UserProfile.TABLE, null, values);
        } else {
        	db.update(UserProfile.TABLE, values, null, null);
        	user_id = profile.getId();
        }
        return user_id;    
    }
    
    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        String qry = "SELECT * FROM " + UserProfile.TABLE;
        
        Log.e(KeyringDatabaseOpenHelper.TAG, qry);
        
        Cursor c = db.rawQuery(qry, null);
     
        if (c != null && c.moveToFirst()) {
	        UserProfile user = new UserProfile();
	        user.setId(c.getInt(c.getColumnIndex(UserProfile.COLUMN_ID)));
	        user.setUserName(c.getString(c.getColumnIndex(UserProfile.COLUMN_USER_NAME)));
	        user.setPassword(c.getString(c.getColumnIndex(UserProfile.COLUMN_PASSWORD)));
	        user.setPasswordSalt(c.getString(c.getColumnIndex(UserProfile.COLUMN_PASSWORD_SALT)));
	        user.setQA1(c.getString(c.getColumnIndex(UserProfile.COLUMN_QUESTION_ANSWER_1)));
	        user.setQA2(c.getString(c.getColumnIndex(UserProfile.COLUMN_QUESTION_ANSWER_2)));
	        user.setQA3(c.getString(c.getColumnIndex(UserProfile.COLUMN_QUESTION_ANSWER_3)));
	     
	        return user;
        } else {
        	return null;
        }
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        
        //db.execSQL("DROP TABLE IF EXISTS " + KEYS_FTS_VIRTUAL_TABLE);
        //onCreate(db);
        if(oldVersion == 1 && newVersion == 2) {
        	db.execSQL(USER_TABLE_CREATE);
        }
    }


}