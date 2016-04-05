package com.kindabear.radiople.view.paginglistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.kindabear.radiople.R;

public class PagingListView extends ListView {

    private final static String TAG = "PagingListView";

    private final static int DEFAULT_OFFSET = 5;

    private Context mContext = null;

    private PagingListener mPagingListener = null;

    private boolean mLocked = true;
    private int mLoadingOffset = DEFAULT_OFFSET;
    private View mLoadingView = null;
    private View mErrorView = null;

    private String mCursor = null;

    private FrameLayout mHelperView = null;

    public PagingListView(Context context) {
        super(context);
        mContext = context;
    }

    public PagingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public PagingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setLoadingOffset(int offset) {
        mLoadingOffset = offset;
    }

    public void setPagingListener(int helperLayoutId, PagingListener pagingListener) {
        mHelperView = (FrameLayout) LayoutInflater.from(mContext).inflate(helperLayoutId, null);

        mLoadingView = mHelperView.findViewById(R.id.view_loading);

        mErrorView = mHelperView.findViewById(R.id.view_error);
        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                detectNextPage();
            }
        });

        addFooterView(mHelperView);

        mPagingListener = pagingListener;
        setOnScrollListener(new BottomScrollListener());
    }

    private void showLoadingView() {
        mErrorView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private void showErrorView() {
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
    }

    private void hideAllHelperViews() {
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
    }

    public void notifyLoadingFinished(String cursor) {
        hideAllHelperViews();
        mCursor = cursor;
        mLocked = cursor == null;
    }

    public void notifyLoadingFailed() {
        showErrorView();
    }

    private void detectNextPage() {
        showLoadingView();
        mPagingListener.onNextPage(mCursor);
    }

    private class BottomScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mLocked || mCursor == null || totalItemCount == 0) {
                return;
            }

            int count = totalItemCount - visibleItemCount;
            if (firstVisibleItem + mLoadingOffset >= count) {
                mLocked = true;
                detectNextPage();
            }
        }
    }
}
