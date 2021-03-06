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

package it.personal.keyring.common;

public class PasswordProtectedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4305827846350967238L;

	public PasswordProtectedException() {
		super("Required password non specified");
	}

	public PasswordProtectedException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public PasswordProtectedException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public PasswordProtectedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
