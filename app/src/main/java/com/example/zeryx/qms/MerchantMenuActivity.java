package com.example.zeryx.qms;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MerchantMenuActivity extends AppCompatActivity {

    private QueueAdapter queueAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    final ArrayList<QueueDataModel> dataModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_menu);
        this.setTitle(String.format("Welcome, %1$s",QMS.merchantName));

        getQueueData();



        queueAdapter = new QueueAdapter(dataModels,getApplicationContext());

        ListView queueListView = findViewById(R.id.queue_list);
        queueListView.setAdapter(queueAdapter);
        queueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                QueueDataModel dataModel = dataModels.get(position);
                TicketInfoActivity.qid = dataModel.getQueueID();
                TicketInfoActivity.queueName = dataModel.getQueueName();
                Intent intent = new Intent(MerchantMenuActivity.this, TicketInfoActivity.class);
                MerchantMenuActivity.this.startActivity(intent);
            }
        });

        queueListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final QueueDataModel dataModel = dataModels.get(position);
                CharSequence options[] = new CharSequence[] {"Delete Queue"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MerchantMenuActivity.this);
                builder.setTitle(dataModel.getQueueName());
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                deleteQueue(dataModel.getQueueID());
                        }
                    }
                });
                builder.show();
                return true;
            }
        });

        mSwipeRefreshLayout = findViewById(R.id.queue_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getQueueData();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.merchant_main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_queue_menu:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Add Queue");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                input.setHint("Queue Name");

                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String queueName = input.getText().toString();
                        addQueue(queueName);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                alert.show();
                return true;
            case R.id.merchant_logout:
                SharedPrefs.getInstance().clearAllDefaults();
                Intent intent = new Intent(MerchantMenuActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                MerchantMenuActivity.this.startActivity(intent);
                finish();
                return true;
            default:

                super.onOptionsItemSelected(item);

        }
        return true;
    }

    private void getQueueData() {

        dataModels.clear();

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$sgetqueues?id=%2$s", QMS.serverAddress , QMS.mid);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (obj.getInt("code") == 0) {
                        findViewById(R.id.queue_default_text).setVisibility(View.GONE);
                        //Save Login Info
                        JSONArray queueData = obj.getJSONArray("queues");
                        for (int i=0;i<queueData.length();i++){
                            try {
                                JSONObject queueInfo = queueData.getJSONObject(i);
                                dataModels.add(new QueueDataModel(queueInfo.getInt("q_id"),queueInfo.getString("q_name")));
                                queueAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("QMS", "Invalid Queue JSON");
                            }
                        }
                    }
                    else if (obj.getInt("code") == 2) {
                        Toast.makeText(MerchantMenuActivity.this, "No queues found", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MerchantMenuActivity.this, "Failed to retrieve queue information", Toast.LENGTH_SHORT).show();
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
                        Log.d("Error.Response", error.getMessage());
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

    public void addQueue(String queueName) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$saddqueue?mid=%2$s&qname=%3$s", QMS.serverAddress , QMS.mid, queueName);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {
                        getQueueData();
                    }
                    else {
                        Toast.makeText(MerchantMenuActivity.this, "Failed to add queue", Toast.LENGTH_SHORT).show();
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
                        Log.d("Error.Response", error.getMessage());
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

    public void deleteQueue(Integer queueID) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$sdeletequeue?qid=%2$s", QMS.serverAddress , queueID);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {
                        getQueueData();
                    }
                    else {
                        Toast.makeText(MerchantMenuActivity.this, "Failed to delete queue", Toast.LENGTH_SHORT).show();
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
                        Log.d("Error.Response", error.getMessage());
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
