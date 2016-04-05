package com.kindabear.radiople.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.response.SearchHistory;

import java.util.ArrayList;

public class SearchHistoryAdapter extends ArrayAdapter<SearchHistory> {

    private ArrayList<SearchHistory> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public SearchHistoryAdapter(Context context, int layoutResId, ArrayList<SearchHistory> item) {
        super(context, layoutResId, item);
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchHistoryViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateViewHolder();
        }

        viewHolder = (SearchHistoryViewHolder) convertView.getTag();

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    private void onBindViewHolder(final SearchHistoryViewHolder viewHolder, int position) {
        final SearchHistory history = mItem.get(position);

        viewHolder.mKeywordTextView.setText(history.keyword);
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        SearchHistoryViewHolder viewHolder = new SearchHistoryViewHolder();

        viewHolder.mKeywordTextView = (TextView) convertView.findViewById(R.id.textview_keyword);

        convertView.setTag(viewHolder);
        return convertView;
    }

    private class SearchHistoryViewHolder {
        TextView mKeywordTextView = null;
    }
}
