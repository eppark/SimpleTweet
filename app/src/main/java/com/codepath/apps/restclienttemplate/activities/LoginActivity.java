package com.codepath.apps.restclienttemplate.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityLoginBinding;
import com.codepath.apps.restclienttemplate.models.SampleModel;
import com.codepath.apps.restclienttemplate.models.SampleModelDao;
import com.codepath.oauth.OAuthLoginActionBarActivity;

import java.util.Calendar;
import java.util.Date;

public class LoginActivity extends OAuthLoginActionBarActivity<TwitterClient> {

    private static final String TAG = LoginActivity.class.getSimpleName();
    SampleModelDao sampleModelDao;
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int LOCKOUT_TIME_MIN = 1;
    int loginAttempts;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    boolean firstTry = true;
    boolean loginSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set ViewBinding
        final ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());

        // layout of activity is stored in a special property called root
        View view = binding.getRoot();
        setContentView(view);
        firstTry = true;
        loginSuccess = false;

        // Read the number of login attempts from file
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        loginAttempts = sharedPref.getInt(getString(R.string.login_attempts_key), 0); // 0 by default if the key isn't there

        final SampleModel sampleModel = new SampleModel();
        sampleModel.setName("CodePath");

        sampleModelDao = ((TwitterApp) getApplicationContext()).getMyDatabase().sampleModelDao();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                sampleModelDao.insertModel(sampleModel);
            }
        });
    }


    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    // OAuth authenticated successfully, launch primary authenticated activity
    // i.e Display application "homepage"
    @Override
    public void onLoginSuccess() {
    	loginSuccess = true;
        Log.i("LoginActivity", "login success");
        // Remove the login attempts value
        if (loginAttempts > 0) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(getString(R.string.login_attempts_key));
            editor.apply();
        }
        Intent i = new Intent(this, TimelineActivity.class);
        startActivity(i);
    }

	@Override
	protected void onResume() {
		super.onResume();
		// Increase login attempt counter and let the user know how many tries are left
		if (!firstTry) {
			if (!loginSuccess) {
				handleFailedLogin();
			}
		} else {
			firstTry = false;
		}
	}

	// Handle how many login attempts we have left
	private void handleFailedLogin() {
		// Increase login attempt counter and let the user know how many tries are left
		loginAttempts++;
		int triesLeft = MAX_LOGIN_ATTEMPTS - loginAttempts;
		if (triesLeft > 0) {
			Toast.makeText(this, String.format("Invalid login! %d tries left.", (MAX_LOGIN_ATTEMPTS - loginAttempts)), Toast.LENGTH_SHORT).show();
			// Ensures that sneaky users that quit the app will still face a lock-out after 3 tries
			editor.putInt(getString(R.string.login_attempts_key), loginAttempts);
			editor.apply();
		} else {
			// Prevent the user from logging in for 40 minutes
			Toast.makeText(this, String.format("No more login tries left. Try again in %d minutes.", LOCKOUT_TIME_MIN), Toast.LENGTH_SHORT).show();
			// Save the time the user was locked out
			editor.putString(getString(R.string.lockout_key), Calendar.getInstance().getTime().toString());
			editor.commit();
		}
	}

	// OAuth authentication flow failed, handle the error
    @Override
    public void onLoginFailure(Exception e) {
        Log.e(TAG, "onLoginFailure!", e);

        // Check that the login error was due to invalid credentials
        if (e.getMessage().equals(getString(R.string.twitter_invalid_credentials_error))) {
           handleFailedLogin();
        } else {
            // If not an invalid credentials error, the error has to do with Twitter's side
            Toast.makeText(this, "Couldn't login through Twitter. Try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    // Click handler method for the button used to start OAuth flow
    // Checks to see that the user isn't locked out
    // Uses the client to initiate OAuth authorization
    // This should be tied to a button used to login
    public void loginToRest(View view) {
        // Check whether the user's lockout has been cleared
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        // Default is null if the key isn't there
        String lockoutTime = sharedPref.getString(getString(R.string.lockout_key), null);

		if (lockoutTime != null) {
            long minLeft = ((Calendar.getInstance().getTime().getTime() - new Date(lockoutTime).getTime()) / 1000 / 60);
            // If we still have time left in lockout, return
            if (minLeft < LOCKOUT_TIME_MIN) {
                Toast.makeText(this, String.format("Locked out. Try again in %d minutes.", (LOCKOUT_TIME_MIN - minLeft)), Toast.LENGTH_SHORT).show();
                return;
            } else {
                // Delete the original lock-out key and login attempts
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.remove(getString(R.string.lockout_key));
                editor.remove(getString(R.string.login_attempts_key));
                editor.apply();
                loginAttempts = 0;
            }
        }
        // Connect to Twitter
        getClient().connect();
    }

}
