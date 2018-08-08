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
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private UserTicketAdapter userTicketAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    final ArrayList<UserTicketDataModel> dataModels = new ArrayList<>();
    private IntentIntegrator qrScan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        this.setTitle(String.format("Welcome, %1$s %2$s",QMS.lastName,QMS.firstName));

        getTicketData();

        qrScan = new IntentIntegrator(this);
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        qrScan.setPrompt("Scan a barcode");
        qrScan.setBeepEnabled(false);
        qrScan.setBarcodeImageEnabled(true);
        qrScan.setCaptureActivity(ScanQRActivity.class);

        userTicketAdapter = new UserTicketAdapter(dataModels,getApplicationContext());

        ListView userTicketListView = findViewById(R.id.user_ticket_list);
        userTicketListView.setAdapter(userTicketAdapter);

        mSwipeRefreshLayout = findViewById(R.id.user_ticket_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTicketData();
            }
        });

        userTicketListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final UserTicketDataModel dataModel = dataModels.get(position);
                //Toast.makeText(MenuActivity.this, String.valueOf(dataModel.getTicketID()),Toast.LENGTH_SHORT).show();
            }
        });

        userTicketListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final UserTicketDataModel dataModel = dataModels.get(position);
                CharSequence options[] = new CharSequence[] {"Delete Ticket"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                builder.setTitle(String.valueOf(dataModel.getTicketID()));
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                //deleteQueue(dataModel.getQueueID());
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_scan_qr:
                qrScan.initiateScan();
                return true;
            case R.id.user_logout:
                SharedPrefs.getInstance().clearAllDefaults();
                Intent intent = new Intent(MenuActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                MenuActivity.this.startActivity(intent);
                finish();
                return true;
            default:
                super.onOptionsItemSelected(item);

        }
        return true;
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    addTicket(obj.getString("q_id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("QMS", "Invalid Ticket JSON");
                    Toast.makeText(this, "Error occurred, please try again", Toast.LENGTH_SHORT).show();
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    private void getTicketData() {

        dataModels.clear();

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$sgettickets?uid=%2$s", QMS.serverAddress , QMS.uid);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (obj.getInt("code") == 0) {
                        findViewById(R.id.user_ticket_default_text).setVisibility(View.GONE);
                        JSONArray ticketData = obj.getJSONArray("tickets");
                        for (int i=0;i<ticketData.length();i++){
                            try {
                                JSONObject ticketInfo = ticketData.getJSONObject(i);
                                dataModels.add(new UserTicketDataModel(ticketInfo.getInt("t_id"),ticketInfo.getInt("q_id")));
                                userTicketAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("QMS", "Invalid Ticket JSON");
                            }
                        }
                    }
                    else if (obj.getInt("code") == 2) {
                        Toast.makeText(MenuActivity.this, "No tickets found", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MenuActivity.this, "Failed to retrieve ticket information", Toast.LENGTH_SHORT).show();
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

    private void addTicket(String qid) {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$saddticket?qid=%2$s&uid=%3$s", QMS.serverAddress ,qid , QMS.uid);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (obj.getInt("code") == 0) {
                        Toast.makeText(MenuActivity.this, "Successfully entered into a queue", Toast.LENGTH_SHORT).show();
                        getTicketData();
                    }
                    else {
                        Toast.makeText(MenuActivity.this, "Failed to enter into a queue", Toast.LENGTH_SHORT).show();
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
