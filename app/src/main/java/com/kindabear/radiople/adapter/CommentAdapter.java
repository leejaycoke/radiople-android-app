package com.kindabear.radiople.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.response.Comment;
import com.kindabear.radiople.util.DateUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends ArrayAdapter<Comment> {

    private Context mContext = null;
    private ArrayList<Comment> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public CommentAdapter(Context context, int layoutResId, ArrayList<Comment> item) {
        super(context, layoutResId, item);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateViewHolder();
        }

        viewHolder = (CommentViewHolder) convertView.getTag();

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    private void onBindViewHolder(final CommentViewHolder viewHolder, int position) {
        final Comment comment = mItem.get(position);

        viewHolder.mContentTextView.setText(comment.content);
        viewHolder.mNicknameTextView.setText(comment.user.nickname);
        viewHolder.mCreatedAtTextView.setText(DateUtils.humunize(comment.createdAt));

        if (comment.user.profileImage != null) {
            Picasso.with(mContext).load(ImageUrlHelper.create(comment.user.profileImage, 300, 300)).placeholder(R.drawable.ic_default_profile).fit().into(viewHolder.mProfileImageView);
        } else {
            Picasso.with(mContext).load(R.drawable.ic_default_profile).fit().into(viewHolder.mProfileImageView);
        }
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        CommentViewHolder viewHolder = new CommentViewHolder();
        viewHolder.mContentTextView = (TextView) convertView.findViewById(R.id.textview_content);
        viewHolder.mNicknameTextView = (TextView) convertView.findViewById(R.id.textview_nickname);
        viewHolder.mCreatedAtTextView = (TextView) convertView.findViewById(R.id.textview_created_at);
        viewHolder.mProfileImageView = (CircleImageView) convertView.findViewById(R.id.imageview_profile);
        convertView.setTag(viewHolder);
        return convertView;
    }

    private class CommentViewHolder {
        TextView mNicknameTextView = null;
        TextView mContentTextView = null;
        TextView mCreatedAtTextView = null;
        CircleImageView mProfileImageView = null;
    }
}

