package com.zhang.taxiclient;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Mr.Z on 2016/5/15 0015.
 */
public class PassengerAdapter extends BaseAdapter {

    private Context context;
    private Cursor c;


    public PassengerAdapter(Context context, Cursor c) {
        this.context = context;
        this.c = c;
    }


    @Override
    public int getCount() {
        return c.getCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.passenger_list_cell, null);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.tvDis = (TextView) convertView.findViewById(R.id.tvDis);
            holder.tvDes = (TextView) convertView.findViewById(R.id.tvDes);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        c.moveToPosition(position);

        holder.tvName.setText(c.getString(1));
        holder.tvDis.setText("距离" + c.getString(2) + "米");
        holder.tvDes.setText(c.getString(3));
        return convertView;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvDis;
        TextView tvDes;
    }
}
