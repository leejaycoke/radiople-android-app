package com.kindabear.radiople.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.response.Category;

import java.util.ArrayList;

public class CategoryAdapter extends ArrayAdapter<Category> {

    private ArrayList<Category> mItem = null;
    private LayoutInflater mInflater = null;
    private int mLayoutResId = 0;

    public CategoryAdapter(Context context, int layoutResId, ArrayList<Category> item) {
        super(context, layoutResId, item);
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mItem = item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CategoryViewHolder viewHolder;
        if (convertView == null) {
            convertView = onCreateViewHolder();
        }

        viewHolder = (CategoryViewHolder) convertView.getTag();

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    private void onBindViewHolder(final CategoryViewHolder viewHolder, int position) {
        final Category category = mItem.get(position);

        viewHolder.mNameTextView.setText(category.name);
    }

    private View onCreateViewHolder() {
        View convertView = mInflater.inflate(mLayoutResId, null);
        CategoryViewHolder viewHolder = new CategoryViewHolder();

        viewHolder.mNameTextView = (TextView) convertView.findViewById(R.id.textview_name);

        convertView.setTag(viewHolder);
        return convertView;
    }

    private class CategoryViewHolder {
        TextView mNameTextView = null;
    }
}
