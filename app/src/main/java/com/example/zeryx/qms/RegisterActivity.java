package com.example.zeryx.qms;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private EditText usernameView;
    private EditText lastNameView;
    private EditText firstNameView;
    private EditText nricView;
    private EditText passwordView;
    private EditText confirmPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        usernameView = findViewById(R.id.username);
        lastNameView = findViewById(R.id.lastName);
        firstNameView = findViewById(R.id.firstName);
        nricView = findViewById(R.id.nric);
        passwordView = findViewById(R.id.password);

        confirmPasswordView = findViewById(R.id.confirmPassword);
        confirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button registerButton = findViewById(R.id.user_register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
    }

    private void attemptRegister() {
        // Reset errors.
        usernameView.setError(null);
        lastNameView.setError(null);
        firstNameView.setError(null);
        nricView.setError(null);
        passwordView.setError(null);
        confirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String lastname = lastNameView.getText().toString();
        String firstname = firstNameView.getText().toString();
        String nric = nricView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check for valid last name
        if (TextUtils.isEmpty(lastname)) {
            lastNameView.setError("Last name cannot be empty");
            focusView = lastNameView;
            cancel = true;
        } else if (!isUserValid(lastname)) {
            lastNameView.setError("This name is invalid");
            focusView = lastNameView;
            cancel = true;
        }

        //Check for valid first name
        if (TextUtils.isEmpty(firstname)) {
            firstNameView.setError("First name cannot be empty");
            focusView = firstNameView;
            cancel = true;
        } else if (!isUserValid(lastname)) {
            firstNameView.setError("This name is invalid");
            focusView = firstNameView;
            cancel = true;
        }

        //Check for valid NRIC
        if (TextUtils.isEmpty(nric)) {
            nricView.setError("NRIC cannot be empty");
            focusView = nricView;
            cancel = true;
        } else if (!isNricValid(nric)) {
            nricView.setError("This NRIC is invalid");
            focusView = nricView;
            cancel = true;
        }

        //Check password match & valid password.
        if (!password.equals(confirmPassword)) {
            passwordView.setError("Passwords do not match");
            focusView = passwordView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            confirmPasswordView.setError(getString(R.string.error_field_required));
            focusView = confirmPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            confirmPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = confirmPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isUserValid(username)) {
            usernameView.setError(getString(R.string.error_invalid_email));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            doRegister(username,password,lastname,firstname,nric);
        }
    }

    private boolean isUserValid(String username) {
        //TODO: Replace this with your own logic
        return username.matches("[a-zA-Z0-9.? ]*");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    private boolean isNricValid(String nric) {
        return nric.length() == 12 && nric.matches("[0-9.? ]*");
    }

    private void doRegister(String username, String password, String lastname,String firstname,String nric) {
        final ProgressDialog regDialog = new ProgressDialog(RegisterActivity.this);
        regDialog.setIndeterminate(true);
        regDialog.setMessage("Registering...");
        regDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$sregister?user=%2$s&pass=%3$s&lname=%4$s&fname=%5$s&nric=%6$s",QMS.serverAddress,username,password,lastname,firstname,nric);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {
                        regDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        RegisterActivity.this.startActivity(intent);
                        RegisterActivity.this.finish();
                    } else {
                        regDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Registration failed, please try again", Toast.LENGTH_SHORT).show();
                    }

                } catch (Throwable t) {
                    Log.e("QMS", "Invalid JSON");
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(RegisterActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String credentials = String.format("%1$s:%2$s", QMS.serverID, QMS.serverPwd);
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(postRequest);
    }
}

