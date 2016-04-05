package com.kindabear.radiople.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.response.Notification;

import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private Context mContext = null;
    private ArrayList<Notification> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public NotificationAdapter(Context context, int layoutResId, ArrayList<Notification> item) {
        super(context, layoutResId, item);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NotificationViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateViewHolder();
        }

        viewHolder = (NotificationViewHolder) convertView.getTag();

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    private void onBindViewHolder(NotificationViewHolder viewHolder, int position) {
        Notification notification = mItem.get(position);

        viewHolder.mIconImageView.setImageResource(R.drawable.ic_notification_subscription);

        viewHolder.mCreatedAtTextView.setText(notification.createdAt);
        viewHolder.mMessageTextView.setText(notification.message);
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        NotificationViewHolder viewHolder = new NotificationViewHolder();
        viewHolder.mMessageTextView = (TextView) convertView.findViewById(R.id.textview_message);
        viewHolder.mCreatedAtTextView = (TextView) convertView.findViewById(R.id.textview_created_at);
        viewHolder.mIconImageView = (ImageView) convertView.findViewById(R.id.imageview_icon);
        convertView.setTag(viewHolder);
        return convertView;
    }

    private class NotificationViewHolder {
        TextView mMessageTextView = null;
        TextView mCreatedAtTextView = null;
        ImageView mIconImageView = null;
    }
}
