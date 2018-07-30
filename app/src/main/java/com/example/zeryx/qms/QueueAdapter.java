package com.example.zeryx.qms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

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

        QueueDataModel dataModel = getItem(position);
        ViewHolder viewHolder;

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



        viewHolder.queueID.setText(String.valueOf(dataModel.getQueueID()));
        viewHolder.queueName.setText(dataModel.getQueueName());

        return convertView;

    }
}
