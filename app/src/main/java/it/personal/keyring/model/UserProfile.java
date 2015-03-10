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

package it.personal.keyring.model;

import android.provider.BaseColumns;

public class UserProfile {
	public static final String TABLE = "users";
	public static final String COLUMN_ID = BaseColumns._ID;
	public static final String COLUMN_USER_NAME = "user_name";
	public static final String COLUMN_PASSWORD = "password";
	public static final String COLUMN_PASSWORD_SALT = "salt";
	public static final String COLUMN_QUESTION_ANSWER_1 = "qa1";
	public static final String COLUMN_QUESTION_ANSWER_2 = "qa2";
	public static final String COLUMN_QUESTION_ANSWER_3 = "qa3";
	
	int id;
	String userName;
	String password;
	String passwordSalt;
	int securityQuestion1;
	String securityAnswer1;
	int securityQuestion2;
	String securityAnswer2;
	int securityQuestion3;
	String securityAnswer3;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPasswordSalt() {
		return passwordSalt;
	}
	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}
	public int getSecurityQuestion1() {
		return securityQuestion1;
	}
	public void setSecurityQuestion1(int securityQuestion1) {
		this.securityQuestion1 = securityQuestion1;
	}
	public String getSecurityAnswer1() {
		return securityAnswer1;
	}
	public void setSecurityAnswer1(String securityAnswer1) {
		this.securityAnswer1 = securityAnswer1;
	}
	public int getSecurityQuestion2() {
		return securityQuestion2;
	}
	public void setSecurityQuestion2(int securityQuestion2) {
		this.securityQuestion2 = securityQuestion2;
	}
	public String getSecurityAnswer2() {
		return securityAnswer2;
	}
	public void setSecurityAnswer2(String securityAnswer2) {
		this.securityAnswer2 = securityAnswer2;
	}
	public int getSecurityQuestion3() {
		return securityQuestion3;
	}
	public void setSecurityQuestion3(int securityQuestion3) {
		this.securityQuestion3 = securityQuestion3;
	}
	public String getSecurityAnswer3() {
		return securityAnswer3;
	}
	public void setSecurityAnswer3(String securityAnswer3) {
		this.securityAnswer3 = securityAnswer3;
	}
	public String getQA1() {
		return this.securityQuestion1 + ";" + this.securityAnswer1;
	}
	public void setQA1(String qa) {
		if(qa == null) {
			this.securityQuestion1 = -1;
			this.securityAnswer1 = null;
		} else {
			this.securityQuestion1 = Integer.parseInt(qa.substring(0, 1));
			this.securityAnswer1 = qa.substring(2);
		}
	}

	public void setQA2(String qa) {
		if(qa == null) {
			this.securityQuestion2 = -1;
			this.securityAnswer2 = null;
		} else {
			this.securityQuestion2 = Integer.parseInt(qa.substring(0, 1));
			this.securityAnswer2 = qa.substring(2);
		}
	}
	public void setQA3(String qa) {
		if(qa == null) {
			this.securityQuestion3 = -1;
			this.securityAnswer3 = null;
		} else {
			this.securityQuestion3 = Integer.parseInt(qa.substring(0, 1));
			this.securityAnswer3 = qa.substring(2);
		}
	}

	public static SecurityQuestion parseQA(String qa) {
		if(qa == null || qa.indexOf(';') < 0) 
			return null;
		
		return new SecurityQuestion(Integer.parseInt(qa.substring(0, 1)),
				qa.substring(2));
	}
	public String getQA2() {
		return this.securityQuestion2 + ";" + this.securityAnswer2;
	}
	public String getQA3() {
		return this.securityQuestion3 + ";" + this.securityAnswer3;
	}
	
	
}
