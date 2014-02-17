package com.fuyo.jumplogger;

import java.util.Random;

import com.fuyo.jumplogger.CreateAccountAsyncTask.OnCompleteListener;


import android.app.Activity;
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
import android.widget.Toast;


public class CreateAccountActivity extends Activity {
	
	Button buttonRegister;
	EditText editEmail;
	EditText editEmail2;
	EditText editPassword;
	EditText editPassword2;
	final String ngColor = "#FFCCCC";
	final String okColor = "#FFFFFF";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        editEmail = (EditText)findViewById(R.id.editTextEmail2);
        editEmail2 = (EditText)findViewById(R.id.editTextEmail3);
        editPassword = (EditText)findViewById(R.id.editTextPassword2);
        editPassword2 = (EditText)findViewById(R.id.editTextPassword3);
        
        buttonRegister = (Button)findViewById(R.id.buttonRegister);


        
        buttonRegister.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String email = editEmail.getText().toString();
				final String email2 = editEmail2.getText().toString();
				final String password = editPassword.getText().toString();
				final String password2 = editPassword2.getText().toString();
				boolean okFlag = true;
				if (email.length() == 0 || email2.length() == 0 || !email.equals(email2)) {
					editEmail.setBackgroundColor(Color.parseColor(ngColor));
					editEmail2.setBackgroundColor(Color.parseColor(ngColor));
					okFlag = false;
				} else {
					editEmail.setBackgroundColor(Color.parseColor(okColor));
					editEmail2.setBackgroundColor(Color.parseColor(okColor));
				}
				if (password.length() == 0 || password2.length() == 0 || !password.equals(password2)) {
					editPassword.setBackgroundColor(Color.parseColor(ngColor));
					editPassword2.setBackgroundColor(Color.parseColor(ngColor));
					okFlag = false;
				} else {
					editPassword.setBackgroundColor(Color.parseColor(okColor));
					editPassword2.setBackgroundColor(Color.parseColor(okColor));
				}
				
				if (okFlag) {
					//login here
					CreateAccountAsyncTask lat = new CreateAccountAsyncTask(CreateAccountActivity.this, email, password,
							new OnCompleteListener() {
								@Override
								void onSuccess() {
									Log.d("login", "passcheck OK");
									Toast.makeText(CreateAccountActivity.this, "create account OK", Toast.LENGTH_SHORT).show();
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
									Toast.makeText(CreateAccountActivity.this, "create account failed", Toast.LENGTH_SHORT).show();
									editEmail.setBackgroundColor(Color.parseColor(ngColor));
									editEmail2.setBackgroundColor(Color.parseColor(ngColor));
									editPassword.setBackgroundColor(Color.parseColor(ngColor));
									editPassword2.setBackgroundColor(Color.parseColor(ngColor));
								}
					});
					lat.execute(new String[]{});
				}
			}
		});

        
        
    }

    
}
