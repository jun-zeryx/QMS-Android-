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

public class QueueAdapter extends ArrayAdapter<QueueDataModel>  {

    private ArrayList<QueueDataModel> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView queueID;
        TextView queueName;
    }

    public QueueAdapter (ArrayList<QueueDataModel> data, Context context) {
        super(context, R.layout.merchant_queue_row, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final QueueDataModel dataModel = getItem(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.merchant_queue_row,parent,false);

            viewHolder.queueID = convertView.findViewById(R.id.row_queue_id);
            viewHolder.queueName = convertView.findViewById(R.id.row_queue_name);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = String.format("http://%1$sgettickets?qid=%2$s", QMS.serverAddress ,String.valueOf(dataModel.getQueueID()));

        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Response JSON from server
                Log.d("QMS", response);
                try {

                    JSONObject obj = new JSONObject(response);

                    if (obj.getInt("code") == 0) {
                        JSONArray arrObj = obj.getJSONArray("tickets");
                        viewHolder.queueID.setText(String.valueOf(arrObj.length()));
                    }
                    else if (obj.getInt("code") == 2) {
                        viewHolder.queueID.setText("0");
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
                        Toast.makeText(mContext, "Unable to connect to server", Toast.LENGTH_SHORT).show();
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

        //viewHolder.queueID.setText(String.valueOf(dataModel.getQueueID()));
        viewHolder.queueName.setText(dataModel.getQueueName());

        return convertView;

    }

}
