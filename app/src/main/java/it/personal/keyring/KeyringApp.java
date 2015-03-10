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
import it.personal.keyring.model.UserProfile;
import android.app.Application;
import android.content.SharedPreferences;

public class KeyringApp extends Application {
	public static final int REQUEST_SAVE = 100;
	public static final int REQUEST_LOAD = 101;
    public static final int REQUEST_MAIN_PWD = 102;
    public static final int REQUEST_SETUP_PWD = 103;
    public static final int REQUEST_RECOVER_PWD = 104;
    public static final int REQUEST_ENCRYPT_PWD = 105;
    public static final int REQUEST_CONFIRM_CREDENTIAL = 106;
    public static final int REQUEST_CHANGE_PWD = 107;
    
    public static final int AUTH_MODE_PASSWORD = 0;
    public static final int AUTH_MODE_PIN = 1;
	private static final String APP_CONFIG = "appConfig";

	private boolean firstRunSetted;
	private boolean firstRun;
	private boolean authModeSetted;
	private int authMode;

	private UserProfile currentUser;
	private long keepAliveTime;
	public KeyringApp() {
		firstRunSetted = false;
		authModeSetted = false;
	}
	public boolean isFirstRun() {
		if(firstRunSetted) {
			return firstRun;
		}
		SharedPreferences settings = getSharedPreferences(APP_CONFIG, 0);
		firstRun = settings.getBoolean("firstRun", true);
		firstRunSetted = true;
		return firstRun;
	}
	public void setFirstRun(boolean firstRun) {
		
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(APP_CONFIG, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("firstRun", firstRun);

		editor.commit();
		this.firstRun = firstRun;
	}
	
	public int getAuthenticationMode() {
		if(!authModeSetted) {
			SharedPreferences settings = getSharedPreferences(APP_CONFIG, 0);
			authMode = settings.getInt("authMode", AUTH_MODE_PASSWORD);
			authModeSetted = true;
		}
		return authMode;
	}
	public void setAuthenticationMode(int mode) {
		SharedPreferences settings = getSharedPreferences(APP_CONFIG, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("authMode", mode);

		editor.commit();
		this.authMode = mode;
	}
	
	public UserProfile getCurrentUser() {
		return currentUser;
	}

	
	public void setCurrentUser(UserProfile user) {
		currentUser = user;
		if(user == null) {
			keepAliveTime = 0;
		} else {
			keepAliveTime = System.currentTimeMillis();
		}
	}
	
	public boolean isAuthenticated() {
        if(currentUser == null) {
        	return false;
        }
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - keepAliveTime) < 300000;
	}
}
