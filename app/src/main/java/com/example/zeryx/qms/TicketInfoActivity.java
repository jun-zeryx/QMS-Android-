package com.example.zeryx.qms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class TicketInfoActivity extends AppCompatActivity {

    public static Integer qid;
    public static String queueName;
    private MerchantTicketAdapter merchantTicketAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    final ArrayList<MerchantTicketDataModel> dataModels = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_info);
        this.setTitle(String.format("Queue: %1$s",TicketInfoActivity.queueName));

        getTicketData();

        merchantTicketAdapter = new MerchantTicketAdapter(dataModels,getApplicationContext());

        ListView merchantTicketListView = findViewById(R.id.merchant_ticket_list);
        merchantTicketListView.setAdapter(merchantTicketAdapter);

        mSwipeRefreshLayout = findViewById(R.id.merchant_ticket_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTicketData();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTicketData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ticket_info_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.gen_qr_code:
                Intent intent = new Intent(TicketInfoActivity.this, QRCodeGenerator.class);
                intent.putExtra("queueID",TicketInfoActivity.qid);
                TicketInfoActivity.this.startActivity(intent);
                return true;
            default:

                super.onOptionsItemSelected(item);

        }
        return true;
    }

    private void getTicketData() {

        dataModels.clear();

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = String.format("http://%1$sgettickets?qid=%2$s", QMS.serverAddress , TicketInfoActivity.qid);

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    mSwipeRefreshLayout.setRefreshing(false);
                    if (obj.getInt("code") == 0) {
                        findViewById(R.id.merchant_ticket_default_text).setVisibility(View.GONE);
                        //Save Login Info
                        JSONArray ticketData = obj.getJSONArray("tickets");
                        for (int i=0;i<ticketData.length();i++){
                            try {
                                JSONObject ticketInfo = ticketData.getJSONObject(i);
                                dataModels.add(new MerchantTicketDataModel(ticketInfo.getInt("t_id"),ticketInfo.getInt("u_id")));
                                merchantTicketAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("QMS", "Invalid Ticket JSON");
                            }
                        }
                    }
                    else if (obj.getInt("code") == 2) {
                        Toast.makeText(TicketInfoActivity.this, "No tickets found", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(TicketInfoActivity.this, "Failed to retrieve ticket information", Toast.LENGTH_SHORT).show();
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
