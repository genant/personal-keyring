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

import it.personal.keyring.model.SecurityQuestion;
import it.personal.keyring.model.UserProfile;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class SetupActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	PasswordSectionFragment mPasswordPage;
	QuestionsSectionFragment mQuestionsPage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	public ViewPager getViewPager() {
		return mViewPager;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setup, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.setup_action_cancel:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// NavUtils.navigateUpFromSameTask(this);
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case R.id.setup_action_save:
			if (!save()) {
				return false;
			}
			setResult(Activity.RESULT_OK);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private boolean save() {
		if(mPasswordPage.validatePasswords()) {
			if(mQuestionsPage.validateAnswers()) {
				boolean create = false;
				UserProfile usr = ((KeyringApp)getApplicationContext()).getCurrentUser();
				if(usr == null) {
					create = true;
					usr = new UserProfile();
				}
				usr.setPassword(mPasswordPage.getPassword());
				usr.setSecurityQuestion1(mQuestionsPage.getQuestion(1));
				usr.setSecurityQuestion2(mQuestionsPage.getQuestion(2));
				usr.setSecurityQuestion3(mQuestionsPage.getQuestion(3));
				usr.setSecurityAnswer1(mQuestionsPage.getAnswer(1));
				usr.setSecurityAnswer2(mQuestionsPage.getAnswer(2));
				usr.setSecurityAnswer3(mQuestionsPage.getAnswer(3));
				
				
				KeyringDatabaseOpenHelper db = new KeyringDatabaseOpenHelper(this);
				try {
					db.saveUserProfile(usr, create);
					((KeyringApp)getApplicationContext()).setCurrentUser(usr);
					((KeyringApp)getApplicationContext()).setAuthenticationMode(mPasswordPage.getAuthenticationMode());
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				} finally {
					db.close();
				}
				return true;
			} else {
				mViewPager.setCurrentItem(1);
			}
		} else {
			mViewPager.setCurrentItem(0);
		}
		return false;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.

			Fragment fragment = null;
			Bundle args = null;
			switch (position) {
			case 0:
				mPasswordPage = new PasswordSectionFragment();
				args = new Bundle();
				args.putInt(PasswordSectionFragment.ARG_AUTH_MODE,
						((KeyringApp)getApplicationContext()).getAuthenticationMode());
				mPasswordPage.setArguments(args);
				fragment = mPasswordPage;
				break;
			case 1:
				mQuestionsPage = new QuestionsSectionFragment();
				args = new Bundle();
				args.putInt(QuestionsSectionFragment.ARG_SECTION_NUMBER,
						position + 1);
				UserProfile profile = ((KeyringApp)getApplicationContext()).getCurrentUser();
				if(profile != null) {
					args.putCharSequence(QuestionsSectionFragment.ARG_QUESTION_1, profile.getQA1());
					args.putCharSequence(QuestionsSectionFragment.ARG_QUESTION_2, profile.getQA2());
					args.putCharSequence(QuestionsSectionFragment.ARG_QUESTION_3, profile.getQA3());
				}
				mQuestionsPage.setArguments(args);
				fragment = mQuestionsPage;
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class PasswordSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_AUTH_MODE = "auth_mode";

		private EditText mPasswordView;
		private EditText mConfirmPasswordView;
		private RadioGroup authModeGroupView;
		private String password;
		private String confirmPassword;
		public PasswordSectionFragment() {
		}

		public int getAuthenticationMode() {
			if(authModeGroupView.getCheckedRadioButtonId()==R.id.page_password_auth_mode_pin) 
				return KeyringApp.AUTH_MODE_PIN;
			
			return KeyringApp.AUTH_MODE_PASSWORD;
		}
		public String getPassword() {
			return password;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_setup_password,
					container, false);

			authModeGroupView = (RadioGroup)rootView.findViewById(R.id.page_password_auth_mode);
			authModeGroupView.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if(checkedId == R.id.page_password_auth_mode_password) {
						mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						mConfirmPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					} else {
						mPasswordView.setText("");
						mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
						mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
						mConfirmPasswordView.setText("");
						mConfirmPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
						mConfirmPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
					}
					
				}
			});
			rootView.findViewById(R.id.page_password_next_button).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if(validatePasswords()) {
								((SetupActivity) getActivity()).getViewPager()
										.setCurrentItem(1);
							}
						}
					});

			mPasswordView = (EditText)rootView.findViewById(R.id.page_password_password);
			mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() 
			{
				@Override
				public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) 
				{
					if (id == R.id.page_password_password || id == EditorInfo.IME_NULL) {
						return validatePasswords();
					}
					return false; 
				}
			});

			mConfirmPasswordView = (EditText)rootView.findViewById(R.id.page_password_confirm_password);
			mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() 
			{
				@Override
				public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) 
				{
					if (id == R.id.page_password_confirm_password || id == EditorInfo.IME_NULL) {
						return validatePasswords();
					}
					return false; 
				}
			});

			// sets load arguments
			Bundle args = getArguments();
			if(args.containsKey(ARG_AUTH_MODE)) {
				if(args.getInt(ARG_AUTH_MODE) == KeyringApp.AUTH_MODE_PASSWORD) {
					authModeGroupView.check( R.id.page_password_auth_mode_password);
				} else {
					authModeGroupView.check( R.id.page_password_auth_mode_pin);
				}
			}

			return rootView;
		}
		
		public boolean validatePasswords() {
			mPasswordView.setError(null);
			mConfirmPasswordView.setError(null);
			
			password = mPasswordView.getText().toString();
			
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
				confirmPassword = mConfirmPasswordView.getText().toString();
				if(!TextUtils.equals(password,confirmPassword)) {
					mConfirmPasswordView.setError(getString(R.string.passwords_not_match));
					mConfirmPasswordView.requestFocus();
					return false;
				}
			}
			return true;
		}
	}

	public static class QuestionsSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public static final String ARG_QUESTION_1 = "question_1";
		public static final String ARG_QUESTION_2 = "question_2";
		public static final String ARG_QUESTION_3 = "question_3";

		private Spinner mQuestion1;
		private Spinner mQuestion2;
		private Spinner mQuestion3;

		private EditText mAnswer1;
		private EditText mAnswer2;
		private EditText mAnswer3;

		public QuestionsSectionFragment() {
		}

		public int getQuestion(int number) {
			switch(number) {
			case 1:
				return mQuestion1.getSelectedItemPosition();
			case 2:
				return mQuestion2.getSelectedItemPosition();
			case 3:
				return mQuestion3.getSelectedItemPosition();
			}
			
			return -1;
		}

		public String getAnswer(int number) {
			switch(number) {
			case 1:
				return mAnswer1.getText().toString();
			case 2:
				return mAnswer2.getText().toString();
			case 3:
				return mAnswer3.getText().toString();
			}
			
			
			return null;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_setup_questions,
					container, false);

			mQuestion1 = (Spinner) rootView.findViewById(R.id.page_question_question_1);

			mQuestion2 = (Spinner) rootView.findViewById(R.id.page_question_question_2);
			mQuestion3 = (Spinner) rootView.findViewById(R.id.page_question_question_3);

			// Create an ArrayAdapter using the string array and a default
			// spinner layout
			ArrayAdapter<CharSequence> adapter = ArrayAdapter
					.createFromResource(rootView.getContext(),
							R.array.security_questions_array,
							android.R.layout.simple_spinner_item);
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			// Apply the adapter to the spinner
			mQuestion1.setAdapter(adapter);
			mQuestion2.setAdapter(adapter);
			mQuestion3.setAdapter(adapter);

			mAnswer1 = (EditText)rootView.findViewById(R.id.page_question_answer_1);
			mAnswer2 = (EditText)rootView.findViewById(R.id.page_question_answer_2);
			mAnswer3 = (EditText)rootView.findViewById(R.id.page_question_answer_3);
			
			Bundle args = getArguments();
			SecurityQuestion question;
			if(args.containsKey(ARG_QUESTION_1)) {
				question = UserProfile.parseQA(args.getString(ARG_QUESTION_1));
				mQuestion1.setSelection(question.getQuestionIndex());
				mAnswer1.setText(question.getAnswer());
			}
			if(args.containsKey(ARG_QUESTION_2)) {
				question = UserProfile.parseQA(args.getString(ARG_QUESTION_2));
				mQuestion2.setSelection(question.getQuestionIndex());
				mAnswer2.setText(question.getAnswer());
			}
			if(args.containsKey(ARG_QUESTION_3)) {
				question = UserProfile.parseQA(args.getString(ARG_QUESTION_3));
				mQuestion3.setSelection(question.getQuestionIndex());
				mAnswer3.setText(question.getAnswer());
			}
			return rootView;
		}
		
		public boolean validateAnswers() {
			mAnswer1.setError(null);
			mAnswer2.setError(null);
			mAnswer3.setError(null);
			
			if(TextUtils.isEmpty(getAnswer(1))) {
				mAnswer1.setError(getString(R.string.error_field_required));
				mAnswer1.requestFocus();
				return false;
			}
			if(TextUtils.isEmpty(getAnswer(2))) {
				mAnswer2.setError(getString(R.string.error_field_required));
				mAnswer2.requestFocus();
				return false;
			}
			if(TextUtils.isEmpty(getAnswer(3))) {
				mAnswer3.setError(getString(R.string.error_field_required));
				mAnswer3.requestFocus();
				return false;
			}
			
			return true;
		}
	}

}
