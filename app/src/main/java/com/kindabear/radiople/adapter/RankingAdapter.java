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
import com.kindabear.radiople.view.SizingImageView;
import com.kindabear.radiople.view.sizingimageview.OnSizeChangedListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RankingAdapter extends ArrayAdapter<Broadcast> {

    private Context mContext = null;
    private ArrayList<Broadcast> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public RankingAdapter(Context context, int layoutResId, ArrayList<Broadcast> item) {
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

    private void onBindViewHolder(final SubscriptionViewHolder viewHolder, int position) {
        final Broadcast broadcast = mItem.get(position);

        Picasso.with(mContext).load(ImageUrlHelper.create(broadcast.coverImage, 500, 500)).fit().into(viewHolder.mIconImageView);

        viewHolder.mTitleTextView.setText(broadcast.title);

        String commentCount = mContext.getString(R.string.comment) + ": " + broadcast.scoreboard.commentCount;
        viewHolder.mCommentCountTextView.setText(commentCount);

        String subscriptionCount = mContext.getString(R.string.subscription) + ": " + broadcast.scoreboard.subscriptionCount;
        viewHolder.mSubscriberCountTextView.setText(subscriptionCount);

        viewHolder.mRankingTextView.setText(String.valueOf(position + 1));
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        SubscriptionViewHolder viewHolder = new SubscriptionViewHolder();
        viewHolder.mTitleTextView = (TextView) convertView.findViewById(R.id.textview_title);
        viewHolder.mIconImageView = (SizingImageView) convertView.findViewById(R.id.imageview_icon);
        viewHolder.mSubscriberCountTextView = (TextView) convertView.findViewById(R.id.textview_subscriber_count);
        viewHolder.mCommentCountTextView = (TextView) convertView.findViewById(R.id.textview_comment_count);
        viewHolder.mRankingTextView = (TextView) convertView.findViewById(R.id.textview_ranking);
        convertView.setTag(viewHolder);
        return convertView;
    }

    private class SubscriptionViewHolder {
        SizingImageView mIconImageView = null;
        TextView mTitleTextView = null;
        TextView mCommentCountTextView = null;
        TextView mSubscriberCountTextView = null;
        TextView mRankingTextView = null;
    }
}
