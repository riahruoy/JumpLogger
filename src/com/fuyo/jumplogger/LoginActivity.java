package com.fuyo.jumplogger;

import java.util.Random;

import com.fuyo.jumplogger.LoginAsyncTask.OnCompleteListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class LoginActivity extends Activity {
	
	Button buttonLogin;
	Button buttonRegister;
	EditText editEmail;
	EditText editPassword;
	SharedPreferences sharedPref;
	final String ngColor = "#FFCCCC";
	final String okColor = "#FFFFFF";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        editEmail = (EditText)findViewById(R.id.editTextEmail);
        editPassword = (EditText)findViewById(R.id.editTextPassword);
        buttonRegister = (Button)findViewById(R.id.buttonCreateAccount);
        
        if (sharedPref.contains("email") && sharedPref.contains("password")) {
        	editEmail.setText(sharedPref.getString("email", ""));
        	editPassword.setText(sharedPref.getString("password", ""));
        	buttonRegister.setVisibility(Button.INVISIBLE);
        }
 
        
        buttonLogin = (Button)findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = editEmail.getText().toString();
				if (email.length() == 0) {
					editEmail.setBackgroundColor(Color.parseColor(ngColor));
				} else {
					editEmail.setBackgroundColor(Color.parseColor(okColor));
				}
				final String password = editPassword.getText().toString();
				if (password.length() == 0) {
					editPassword.setBackgroundColor(Color.parseColor(ngColor));
				} else {
					editPassword.setBackgroundColor(Color.parseColor(okColor));
				}
				
				if (email.length() > 0 && password.length() > 0) {
					//login here
					LoginAsyncTask lat = new LoginAsyncTask(LoginActivity.this, email, password,
							new OnCompleteListener() {
								@Override
								void onSuccess() {
									Log.d("login", "passcheck OK");
							        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						        	Editor e = sharedPref.edit();
						        	e.putString("email", email);
						        	e.putString("password", password);
						        	e.commit();
						        	finish();
								}
								@Override
								void onFailure() {
									Log.d("login", "passcheck NG");
									editEmail.setBackgroundColor(Color.parseColor(ngColor));
									editPassword.setBackgroundColor(Color.parseColor(ngColor));
								}
					});
					lat.execute(new String[]{});
				}
			}
		});
        buttonRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				startActivity(intent);
				finish();
			}
		});
        
        
    }

    
}
