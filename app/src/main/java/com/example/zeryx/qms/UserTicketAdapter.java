package com.example.zeryx.qms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserTicketAdapter extends ArrayAdapter<UserTicketDataModel> {
    private ArrayList<UserTicketDataModel> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView ticketID;
        TextView merchantID;
        TextView waitingQueue;
    }

    public UserTicketAdapter(ArrayList<UserTicketDataModel> data, Context context) {
        super(context, R.layout.user_ticket_row, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final UserTicketDataModel dataModel = getItem(position);
        final UserTicketAdapter.ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new UserTicketAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.user_ticket_row,parent,false);

            viewHolder.ticketID = convertView.findViewById(R.id.row_user_ticket_id);
            viewHolder.merchantID = convertView.findViewById(R.id.row_user_ticket_mid);
            viewHolder.waitingQueue = convertView.findViewById(R.id.row_user_ticket_waiting);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (UserTicketAdapter.ViewHolder) convertView.getTag();
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = String.format("http://%1$sgetqueue?qid=%2$s", QMS.serverAddress ,String.valueOf(dataModel.getQueueID()));
        final String[] queueName = new String[1];
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {
                        JSONObject merchantData = obj.getJSONObject("queue");
                        queueName[0] = String.format("%1$s",merchantData.getString("q_name"));
                        viewHolder.merchantID.setText(queueName[0]);
                    }
                    else {
                        Toast.makeText(mContext, "Unable to retrieve queue name", Toast.LENGTH_SHORT).show();
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
                        Log.d("Error.Response", "Unable to connect to server");
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

        String url2 = String.format("http://%1$sgettickets?qid=%2$s", QMS.serverAddress ,String.valueOf(dataModel.getQueueID()));
        StringRequest postRequest2 = new StringRequest(Request.Method.POST, url2, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {
                        JSONArray arr = obj.getJSONArray("tickets");
                        for (int i=0;i<arr.length();i++) {
                            JSONObject arrObj = arr.getJSONObject(i);
                            if (arrObj.getInt("t_id") == dataModel.getTicketID()) {
                                viewHolder.waitingQueue.setText(String.valueOf(i));
                            }
                        }
                    }
                    else {
                        Toast.makeText(mContext, "Unable to retrieve waiting list", Toast.LENGTH_SHORT).show();
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
                        Log.d("Error.Response", "Unable to connect to server");
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
        queue.add(postRequest2);

        viewHolder.ticketID.setText(String.valueOf(dataModel.getTicketID()));


        return convertView;

    }
}
