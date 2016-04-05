package com.kindabear.radiople.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.response.Episode;
import com.kindabear.radiople.response.Storage;
import com.kindabear.radiople.util.DateUtils;

import java.util.ArrayList;

public class EpisodeAdapter extends ArrayAdapter<Episode> {

    private ArrayList<Episode> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public EpisodeAdapter(Context context, int layoutResId, ArrayList<Episode> item) {
        super(context, layoutResId, item);
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EpisodeViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateViewHolder();
        }

        viewHolder = (EpisodeViewHolder) convertView.getTag();

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    private void onBindViewHolder(EpisodeViewHolder viewHolder, int position) {
        Episode episode = mItem.get(position);

        viewHolder.mTitleTextView.setText(episode.title);

        if (episode.getSubtitle() != null) {
            viewHolder.mSubtitleTextView.setVisibility(View.VISIBLE);
            viewHolder.mSubtitleTextView.setText(episode.getSubtitle());
        } else {
            viewHolder.mSubtitleTextView.setVisibility(View.GONE);
        }

        viewHolder.mAirDateTextView.setText(DateUtils.toString(episode.airDate, DateUtils.FORMAT_DATE));

        viewHolder.mLikeCountTextView.setText(episode.scoreboard.getLikeCount());

        if (episode.activity.isLike) {
            viewHolder.mLikeImageView.setImageResource(R.drawable.ic_action_favorite);
        } else {
            viewHolder.mLikeImageView.setImageResource(R.drawable.ic_action_favorite_disabled);
        }

        if (episode.activity.position > 0) {
            viewHolder.mHeardImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mHeardImageView.setVisibility(View.GONE);
        }

        if (episode.storage.fileType.equals(Storage.FileType.AUDIO)) {
            viewHolder.mFileInfoTextView.setText(episode.storage.extra.displayLength);
            viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
        } else if (episode.storage.fileType.equals(Storage.FileType.VIDEO)) {
            viewHolder.mFileInfoTextView.setText("비디오");
            viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
        } else if (episode.storage.fileType.equals(Storage.FileType.PDF)) {
            viewHolder.mFileInfoTextView.setText("PDF");
            viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mFileInfoTextView.setVisibility(View.GONE);
        }
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        EpisodeViewHolder viewHolder = new EpisodeViewHolder();
        viewHolder.mLikeImageView = (ImageView) convertView.findViewById(R.id.imageview_like);
        viewHolder.mTitleTextView = (TextView) convertView.findViewById(R.id.textview_title);
        viewHolder.mSubtitleTextView = (TextView) convertView.findViewById(R.id.textview_subtitle);
        viewHolder.mAirDateTextView = (TextView) convertView.findViewById(R.id.textview_air_date);
        viewHolder.mLikeCountTextView = (TextView) convertView.findViewById(R.id.textview_like_count);
        viewHolder.mHeardImageView = (ImageView) convertView.findViewById(R.id.imageview_heard);
        viewHolder.mFileInfoTextView = (TextView) convertView.findViewById(R.id.textview_file_information);
        convertView.setTag(viewHolder);
        return convertView;
    }

    private class EpisodeViewHolder {
        TextView mTitleTextView = null;
        TextView mSubtitleTextView = null;
        TextView mAirDateTextView = null;
        TextView mLikeCountTextView = null;
        ImageView mLikeImageView = null;
        TextView mFileInfoTextView = null;
        ImageView mHeardImageView = null;
    }
}
