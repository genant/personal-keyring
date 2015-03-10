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
import it.personal.keyring.common.PasswordProtectedException;
import it.personal.keyring.common.SimpleCrypto;
import it.personal.keyring.model.PersonalKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;


/**
 * Contains logic to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 */
public class KeyringDatabase {

    //The columns we'll include in the dictionary table
    public static final String KEY_ID = BaseColumns._ID;
    public static final String KEY_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_NOTES = SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String SCORE = "Score";

    private final KeyringDatabaseOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    public KeyringDatabase(Context context) {
        mDatabaseOpenHelper = new KeyringDatabaseOpenHelper(context);
    }

    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_NAME, KEY_NAME);
        map.put(KEY_NOTES, KEY_NOTES);
        map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        map.put(USERNAME, USERNAME);
        map.put(PASSWORD, PASSWORD);
        map.put(SCORE, SCORE);
        return map;
    }

    public static PersonalKey getKeyFromCurrentRow(Cursor cursor) {
    	PersonalKey key = new PersonalKey();
    	
        key.Name = cursor.getString(cursor.getColumnIndexOrThrow(KeyringDatabase.KEY_NAME));
        key.Username = cursor.getString(cursor.getColumnIndexOrThrow(KeyringDatabase.USERNAME));
        key.Password = cursor.getString(cursor.getColumnIndexOrThrow(KeyringDatabase.PASSWORD));
        key.Notes = cursor.getString(cursor.getColumnIndexOrThrow(KeyringDatabase.KEY_NOTES));
        if(cursor.getColumnIndex(KeyringDatabase.KEY_ID) >= 0) {
        	key.Id = cursor.getString(cursor.getColumnIndexOrThrow(KeyringDatabase.KEY_ID));
        } else if(cursor.getColumnIndex("rowid") >= 0) {
        	key.Id = cursor.getString(cursor.getColumnIndexOrThrow("rowid"));
        }
    	return key;
    }
    
    /**
     * Returns a Cursor positioned at the word specified by rowId
     *
     * @param rowId id of word to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching word, or null if not found.
     */
    public Cursor getKeyById(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }

    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getKeyMatches(String query, String[] columns) {
        String selection = KEY_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <KEY_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the DictionaryProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }

    public Cursor browseAll(String[] columns) {

        return query(columns, KeyringDatabase.KEY_NAME, null);
    }

    
    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(KeyringDatabaseOpenHelper.KEYS_FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private Cursor query(String[] columns, String sortOrder, String limit) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(KeyringDatabaseOpenHelper.KEYS_FTS_VIRTUAL_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, null, null, null, null, sortOrder, limit);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public long insert(PersonalKey key) {
    	
    	return mDatabaseOpenHelper.insertKey(key);
    }

    public int update(PersonalKey key) {
    	
    	return mDatabaseOpenHelper.updateKey(key);
    }

    
    public void delete(String rowid) {
		// TODO Auto-generated method stub
    	mDatabaseOpenHelper.deleteKey (rowid);
	}


	public void importXml(ImportArgs request) throws Exception  {
		KeyXmlParser parser = new KeyXmlParser();
		parser.setEncryptPassword(request.getPassword());
		try {
			List<PersonalKey> keys = parser.parse(request.getFileName());
			for(PersonalKey key: keys) {
				key.Password = SimpleCrypto.encrypt(request.getMainPassword(), key.Password);
			}
			if(request.isCleanBeforeStart()) {
				mDatabaseOpenHelper.deleteAll();
			}
			mDatabaseOpenHelper.importKeys(keys, request.getDuplicateAction());
		} catch (PasswordProtectedException e) {
			
		} catch (Exception e) {
            Log.w(KeyringDatabaseOpenHelper.TAG, "Error while loading XML source file: " + e.getMessage());
            throw e;
		}
	}

	public List<PersonalKey> exportXml(String mainPassword) throws Exception {

		Cursor cursor = query(new String[] { 	    
				KEY_ID,
			    KEY_NAME,
			    KEY_NOTES,
			    USERNAME,
			    PASSWORD }, null, null);
		
		List<PersonalKey> items = new ArrayList<PersonalKey>();
		cursor.moveToFirst();
		do {
        	PersonalKey key = getKeyFromCurrentRow(cursor);
        	items.add(key);
        	
			String pwd;
			// decrypt the key with di main password
			pwd = SimpleCrypto.decrypt(mainPassword, key.Password);
			key.Password = pwd;
		}
        while(cursor.moveToNext());
		
		return items;
	}


}
