package com.kindabear.radiople.ui;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kindabear.radiople.R;
import com.kindabear.radiople.RadiopleApplication;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.User;
import com.kindabear.radiople.response.UserSession;
import com.kindabear.radiople.service.SessionService;
import com.kindabear.radiople.service.UserService;

public class BaseActivity extends AppCompatActivity {
    public Toolbar mToolbar = null;
    public ActionBar mActionBar = null;

    private OnNetworkRetryListener mOnNetworkRetryListener = null;

    private ProgressDialog mLoadingDialog = null;

    private View mHelperView = null;

    private SparseArray<View> mHelperViews = null;

    private UserSession mUserSession = null;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setMessage(getString(R.string.please_wait));
        mLoadingDialog.setCancelable(false);

        initToolbar();
        initHelperView();
    }

    public ProgressDialog getLoadingDialog() {
        return mLoadingDialog;
    }

    public void showLoadingDialog() {
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    public void hideLoadingDialog() {
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    public void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mActionBar = getSupportActionBar();
        }
    }

    public void setStatusBarColor(int color) {
        getWindow().setStatusBarColor(color);
    }

    public void initHelperView() {
        mHelperView = findViewById(R.id.view_helper);
        if (mHelperView == null) {
            return;
        }

        mHelperViews = new SparseArray<View>();
        mHelperViews.put(HelperView.LOADING, findViewById(R.id.view_loading));
        mHelperViews.put(HelperView.NETWORK_ERROR, findViewById(R.id.view_network_error));
        mHelperViews.put(HelperView.EMPTY, findViewById(R.id.view_empty));

        findViewById(R.id.button_network_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnNetworkRetryListener != null) {
                    mOnNetworkRetryListener.onRetry();
                }
            }
        });
    }

    public void setOnNetworkRetryListener(final OnNetworkRetryListener listener) {
        mOnNetworkRetryListener = listener;
    }

    private void showHelperView() {
        mHelperView.setVisibility(View.VISIBLE);
    }

    private void hideHelperView() {
        mHelperView.setVisibility(View.GONE);
    }

    public void hideHelperViews() {
        hideHelperView();

        for (int i = 0; i < 3; i++) {
            View view = mHelperViews.get(i);
            view.setVisibility(View.GONE);
        }
    }

    public void showLoadingView() {
        hideHelperViews();
        mHelperViews.get(HelperView.LOADING).setVisibility(View.VISIBLE);
        showHelperView();
    }

    public void showNetworkErrorView() {
        showNetworkErrorView(getString(R.string.network_error_message));
    }

    public void showNetworkErrorView(int textId) {
        showNetworkErrorView(getString(textId));
    }

    public void showNetworkErrorView(CharSequence message) {
        hideHelperViews();
        mHelperViews.get(HelperView.NETWORK_ERROR).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textview_network_error_message)).setText(message);
        showHelperView();
    }

    public void showEmptyView() {
        showEmptyView(getString(R.string.not_exists_contents));
    }

    public void showEmptyView(int textId) {
        showEmptyView(getString(textId));
    }

    public void showEmptyView(CharSequence message) {
        hideHelperViews();
        mHelperViews.get(HelperView.EMPTY).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.textview_empty_message)).setText(message);
        showHelperView();
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int textResId) {
        showToast(getString(textResId));
    }

    private class HelperView {
        private final static int LOADING = 0;
        private final static int NETWORK_ERROR = 1;
        private final static int EMPTY = 2;
    }

    protected UserService getUserService() {
        return ((RadiopleApplication) getApplicationContext()).getUserService();
    }

    protected SessionService getSessionService() {
        return ((RadiopleApplication) getApplicationContext()).getSessionService();
    }
}
