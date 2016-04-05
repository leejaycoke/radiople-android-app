package com.kindabear.radiople.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.response.History;
import com.kindabear.radiople.util.DateUtils;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.ArrayList;


public class EpisodeHistoryAdapter extends ArrayAdapter<History> {

    private final static String TAG = "EpisodeHistoryAdapter";

    private Context mContext = null;
    private ArrayList<History> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public EpisodeHistoryAdapter(Context context, int layoutResId, ArrayList<History> item) {
        super(context, layoutResId, item);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateViewHolder();
        }

        viewHolder = (HistoryViewHolder) convertView.getTag();

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    private void onBindViewHolder(HistoryViewHolder viewHolder, int position) {
        History history = mItem.get(position);

        Picasso.with(mContext).load(ImageUrlHelper.create(history.episode.broadcast.iconImage, 500, 500)).fit().into(viewHolder.mIconImageView);

        viewHolder.mEpisodeTitle.setText(history.episode.title);

        viewHolder.mAudioLengthTextView.setText(history.episode.storage.extra.displayLength);

        DateTime updatedAt = DateUtils.toDateTime(history.updatedAt);
        viewHolder.mUpdatedAtTextView.setText(DateUtils.humunize(updatedAt, DateUtils.FORMAT_DATE, false));

        String likeCount = mContext.getString(R.string.integer_people_like, history.episode.scoreboard.likeCount);
        viewHolder.mLikeCountTextView.setText(likeCount);
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        HistoryViewHolder viewHolder = new HistoryViewHolder();
        viewHolder.mIconImageView = (ImageView) convertView.findViewById(R.id.imageview_icon);
        viewHolder.mEpisodeTitle = (TextView) convertView.findViewById(R.id.textview_episode_title);
        viewHolder.mAudioLengthTextView = (TextView) convertView.findViewById(R.id.textview_audio_length);
        viewHolder.mUpdatedAtTextView = (TextView) convertView.findViewById(R.id.textview_updated_at);
        viewHolder.mLikeCountTextView = (TextView) convertView.findViewById(R.id.textview_like_count);
        convertView.setTag(viewHolder);
        return convertView;
    }

    private class HistoryViewHolder {
        ImageView mIconImageView = null;
        TextView mEpisodeTitle = null;
        TextView mAudioLengthTextView = null;
        TextView mUpdatedAtTextView = null;
        TextView mLikeCountTextView = null;
    }
}
