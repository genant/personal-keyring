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

public class SecurityQuestion {
	private int mQuestionIndex;
	private String mAnswer;
	
	public int getQuestionIndex() {
		return mQuestionIndex;
	}

	public void setQuestionIndex(int questionIndex) {
		this.mQuestionIndex = questionIndex;
	}

	public String getAnswer() {
		return mAnswer;
	}

	public void setAnswer(String answer) {
		this.mAnswer = answer;
	}

	public SecurityQuestion(int questionIndex, String answer) {
		this.mQuestionIndex = questionIndex;
		this.mAnswer = answer;
	}

}
