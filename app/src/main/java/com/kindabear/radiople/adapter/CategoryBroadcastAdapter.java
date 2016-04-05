package com.kindabear.radiople.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.util.DateUtils;
import com.kindabear.radiople.view.SizingImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryBroadcastAdapter extends ArrayAdapter<Broadcast> {

    private Context mContext = null;
    private ArrayList<Broadcast> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public CategoryBroadcastAdapter(Context context, int layoutResId, ArrayList<Broadcast> item) {
        super(context, layoutResId, item);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SubscriptionViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateViewHolder();
        }

        viewHolder = (SubscriptionViewHolder) convertView.getTag();

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    private void onBindViewHolder(SubscriptionViewHolder viewHolder, int position) {
        Broadcast broadcast = mItem.get(position);

        Picasso.with(mContext).load(ImageUrlHelper.create(broadcast.iconImage, 500, 500)).fit().into(viewHolder.mIconImageView);

        viewHolder.mTitleTextView.setText(broadcast.title);

        viewHolder.mCastingTextView.setText(broadcast.getCasting());

        viewHolder.mEpisodeCountTextView.setText(mContext.getString(R.string.total_episode_count_is, broadcast.scoreboard.episodeCount));
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        SubscriptionViewHolder viewHolder = new SubscriptionViewHolder();
        viewHolder.mTitleTextView = (TextView) convertView.findViewById(R.id.textview_title);
        viewHolder.mIconImageView = (SizingImageView) convertView.findViewById(R.id.imageview_icon);
        viewHolder.mCastingTextView = (TextView) convertView.findViewById(R.id.textview_casting);
        viewHolder.mEpisodeCountTextView = (TextView) convertView.findViewById(R.id.textview_episode_count);
        convertView.setTag(viewHolder);
        return convertView;
    }

    private class SubscriptionViewHolder {
        public SizingImageView mIconImageView = null;
        public TextView mTitleTextView = null;
        public TextView mCastingTextView = null;
        public TextView mEpisodeCountTextView = null;
    }
}
