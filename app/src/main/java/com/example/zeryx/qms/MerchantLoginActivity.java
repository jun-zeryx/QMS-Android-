package com.example.zeryx.qms;




import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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


public class MerchantLoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_login);
        // Set up the login form.
        mUserView = findViewById(R.id.merchant_username);

        mPasswordView = findViewById(R.id.merchant_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserLoginButton = findViewById(R.id.merchant_login_button);
        mUserLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.merchant_login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_login:
                Intent intent = new Intent(MerchantLoginActivity.this, LoginActivity.class);
                MerchantLoginActivity.this.startActivity(intent);
                MerchantLoginActivity.this.finish();
                return true;
            default:

                super.onOptionsItemSelected(item);

        }
        return true;
    }

    private void attemptLogin() {
        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isUserValid(username)) {
            mUserView.setError(getString(R.string.error_invalid_email));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            doLogin(username, password);
        }
    }

    private boolean isUserValid(String username) {
        return username.contains("\"/[\\u2190-\\u21FF]|[\\u2600-\\u26FF]|[\\u2700-\\u27BF]|[\\u3000-\\u303F]|[\\u1F300-\\u1F64F]|[\\u1F680-\\u1F6FF]/g\"");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 2;
    }

    private void doLogin(final String username,final String password) {

        final ProgressDialog authDialog = new ProgressDialog(MerchantLoginActivity.this);
        authDialog.setIndeterminate(true);
        authDialog.setMessage("Authenticating...");
        authDialog.show();

        RequestQueue queue = Volley.newRequestQueue(this);

        //String url = "http://zeryx.ddns.net/qms/login";

        String url = String.format("http://%1$smerchantlogin?user=%2$s&pass=%3$s", QMS.serverAddress ,username, password);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {

                        //Save Login Info
                        JSONObject merchantInfo = obj.getJSONObject("merchantInfo");
                        QMS.mid = merchantInfo.getInt("m_id");
                        QMS.merchantUsername = merchantInfo.getString("m_username");
                        QMS.merchantName = merchantInfo.getString("m_name");
                        SharedPrefs.getInstance().setDefaults("user",username);
                        SharedPrefs.getInstance().setDefaults("pass",password);
                        SharedPrefs.getInstance().setDefaults("type","merchant");
                        Intent intent = new Intent(MerchantLoginActivity.this, MerchantMenuActivity.class);
                        MerchantLoginActivity.this.startActivity(intent);
                        MerchantLoginActivity.this.finish();
                    } else {
                        Toast.makeText(MerchantLoginActivity.this, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                    }

                } catch (Throwable t) {
                    Log.e("QMS", "Invalid JSON");
                }
                authDialog.dismiss();
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(MerchantLoginActivity.this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                        authDialog.dismiss();
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

