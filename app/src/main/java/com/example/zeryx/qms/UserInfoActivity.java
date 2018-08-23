package com.example.zeryx.qms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class UserInfoActivity extends AppCompatActivity {

    private EditText userFirstName;
    private EditText userLastName;
    private EditText userNric;
    private Button saveButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        userFirstName = findViewById(R.id.user_edit_info_fname);
        userLastName = findViewById(R.id.user_edit_info_lname);
        userNric = findViewById(R.id.user_edit_info_nric);
        saveButton = findViewById(R.id.user_edit_info_save);
        backButton = findViewById(R.id.user_edit_info_back);

        userFirstName.setText(QMS.firstName);
        userLastName.setText(QMS.lastName);
        userNric.setText(QMS.nric);
        saveButton.setEnabled(false);

        userFirstName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {}

        });

        userLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        userNric.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveButton.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdate();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfoActivity.this.finish();
            }
        });
    }

    private void attemptUpdate() {
        // Reset errors.
        userFirstName.setError(null);
        userLastName.setError(null);
        userNric.setError(null);

        // Store values at the time of the login attempt.
        String firstname = userFirstName.getText().toString();
        String lastname = userLastName.getText().toString();
        String nric = userNric.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check for valid last name
        if (TextUtils.isEmpty(lastname)) {
            userLastName.setError("Last name cannot be empty");
            focusView = userLastName;
            cancel = true;
        } else if (!isUserValid(lastname)) {
            userLastName.setError("This name is invalid");
            focusView = userLastName;
            cancel = true;
        }

        //Check for valid first name
        if (TextUtils.isEmpty(firstname)) {
            userFirstName.setError("First name cannot be empty");
            focusView = userFirstName;
            cancel = true;
        } else if (!isUserValid(firstname)) {
            userFirstName.setError("This name is invalid");
            focusView = userFirstName;
            cancel = true;
        }

        //Check for valid NRIC
        if (TextUtils.isEmpty(nric)) {
            userNric.setError("NRIC cannot be empty");
            focusView = userNric;
            cancel = true;
        } else if (!isNricValid(nric)) {
            userNric.setError("This NRIC is invalid");
            focusView = userNric;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            updateUser();
        }
    }

    private boolean isUserValid(String username) {
        return username.matches("[a-zA-Z0-9.? ]*");
    }

    private boolean isNricValid(String nric) {
        return nric.length() == 12 && nric.matches("[0-9.? ]*");
    }

    private void updateUser() {
        final ProgressDialog updateDialog = new ProgressDialog(UserInfoActivity.this);
        updateDialog.setIndeterminate(true);
        updateDialog.setMessage("Saving...");
        updateDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$supdateuser?uid=%2$s&fname=%3$s&lname=%4$s&nric=%5$s", QMS.serverAddress, QMS.uid, userFirstName.getText().toString(), userLastName.getText().toString(), userNric.getText().toString());

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {

                        //Update Login Info
                        QMS.firstName = userFirstName.getText().toString();
                        QMS.lastName = userLastName.getText().toString();
                        QMS.nric = userNric.getText().toString();
                        UserInfoActivity.this.finish();
                        Toast.makeText(UserInfoActivity.this, "Save success" , Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserInfoActivity.this, "Save failed" , Toast.LENGTH_SHORT).show();
                    }

                } catch (Throwable t) {
                    Log.e("QMS", "Invalid JSON");
                }
                updateDialog.dismiss();
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(UserInfoActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                        updateDialog.dismiss();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String credentials = String.format("%1$s:%2$s", QMS.serverID, QMS.serverPwd);
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(postRequest);
    }
}
