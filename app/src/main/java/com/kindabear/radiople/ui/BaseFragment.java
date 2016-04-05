package com.kindabear.radiople.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kindabear.radiople.R;
import com.kindabear.radiople.RadiopleApplication;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.service.SessionService;
import com.kindabear.radiople.service.UserService;

public class BaseFragment extends Fragment {

    private final static String TAG = "BaseFragment";

    public View mView = null;

    private SparseArray<View> mHelperViews = null;

    private OnNetworkRetryListener mOnNetworkRetryListener = null;

    private View mHelperView = null;

    private ProgressDialog mLoadingDialog = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        mView = getView();

        initHelperView();

        mLoadingDialog = new ProgressDialog(getActivity());
        mLoadingDialog.setMessage(getString(R.string.please_wait));
        mLoadingDialog.setCancelable(false);
    }

    public void initHelperView() {
        mHelperView = mView.findViewById(R.id.view_helper);
        if (mHelperView == null) {
            return;
        }

        mHelperViews = new SparseArray<View>();
        mHelperViews.put(HelperView.LOADING, mView.findViewById(R.id.view_loading));
        mHelperViews.put(HelperView.NETWORK_ERROR, mView.findViewById(R.id.view_network_error));
        mHelperViews.put(HelperView.EMPTY, mView.findViewById(R.id.view_empty));

        mView.findViewById(R.id.button_network_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnNetworkRetryListener != null) {
                    mOnNetworkRetryListener.onRetry();
                }
            }
        });
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
        if (isAdded()) {
            hideHelperViews();
            mHelperViews.get(HelperView.LOADING).setVisibility(View.VISIBLE);
            showHelperView();
        }
    }

    public void showNetworkErrorView() {
        if (isAdded()) {
            showNetworkErrorView(getString(R.string.network_error_message));
        }
    }

    public void showNetworkErrorView(int textId) {
        if (isAdded()) {
            showNetworkErrorView(getString(textId));
        }
    }

    public void showNetworkErrorView(CharSequence message) {
        if (isAdded()) {
            hideHelperViews();
            mHelperViews.get(HelperView.NETWORK_ERROR).setVisibility(View.VISIBLE);
            ((TextView) mView.findViewById(R.id.textview_network_error_message)).setText(message);
            showHelperView();
        }
    }

    public void showEmptyView() {
        if (isAdded()) {
            showEmptyView(getString(R.string.not_exists_contents));
        }
    }

    public void showEmptyView(int textId) {
        if (isAdded()) {
            showEmptyView(getString(textId));
        }
    }

    public void showEmptyView(CharSequence message) {
        if (isAdded()) {
            hideHelperViews();
            mHelperViews.get(HelperView.EMPTY).setVisibility(View.VISIBLE);
            ((TextView) mView.findViewById(R.id.textview_empty_message)).setText(message);
            showHelperView();
        }
    }

    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
        return ((RadiopleApplication) getActivity().getApplicationContext()).getUserService();
    }

    protected SessionService getSessionService() {
        return ((RadiopleApplication) getActivity().getApplicationContext()).getSessionService();
    }
}
