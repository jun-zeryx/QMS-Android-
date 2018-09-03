package com.example.zeryx.qms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

public class UserChangePwdActivity extends AppCompatActivity {

    private EditText oldPasswordView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private Button saveButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_change_pwd);
        setTitle("Change Password");

        oldPasswordView = findViewById(R.id.user_change_password_oldpass);
        passwordView = findViewById(R.id.user_change_password);
        confirmPasswordView = findViewById(R.id.user_confirm_change_password);
        saveButton = findViewById(R.id.user_change_password_save);
        backButton = findViewById(R.id.user_change_password_back);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdate();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserChangePwdActivity.this.finish();
            }
        });
    }

    private void attemptUpdate() {
        // Reset errors.
        oldPasswordView.setError(null);
        passwordView.setError(null);
        confirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String oldPassword = oldPasswordView.getText().toString();
        String password = passwordView.getText().toString();
        String confirmPassword = confirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check old password
        if (!oldPassword.equals(SharedPrefs.getInstance().getDefaults("pass"))) {
            oldPasswordView.setError("Wrong password");
            focusView = oldPasswordView;
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

        if (cancel) {
            focusView.requestFocus();
        } else {
            updatePassword(password);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }

    private void updatePassword(String password) {
        final ProgressDialog updateDialog = new ProgressDialog(UserChangePwdActivity.this);
        updateDialog.setIndeterminate(true);
        updateDialog.setMessage("Saving...");
        updateDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$supdateuserpassword?uid=%2$s&pass=%3$s", QMS.serverAddress, QMS.uid, password);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {

                        //Update Login Info
                        Toast.makeText(UserChangePwdActivity.this, "Successfully changed passwords" , Toast.LENGTH_SHORT).show();
                        SharedPrefs.getInstance().clearAllDefaults();
                        Intent intent = new Intent(UserChangePwdActivity.this,LoginActivity.class);
                        finishAffinity();
                        UserChangePwdActivity.this.startActivity(intent);
                    } else {
                        Toast.makeText(UserChangePwdActivity.this, "Save failed" , Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(UserChangePwdActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
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
