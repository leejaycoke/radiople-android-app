package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.SubscriptionAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.response.BroadcastList;
import com.kindabear.radiople.response.EmptyJson;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.util.ArrayList;

public class SubscriptionActivity extends BaseActivity {

    private final static String TAG = "SubscriptionActivity";

    private PagingListView mListView = null;
    private SubscriptionAdapter mAdapter = null;

    private ArrayList<Broadcast> mList = new ArrayList<Broadcast>();

    private RequestQueue mRequestQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mListView = (PagingListView) findViewById(R.id.listview);
        mListView.setPagingListener(R.layout.helper_list_item, new PagingListener() {
            @Override
            public void onNextPage(String cursor) {
                requestSubscription(cursor);
            }

            @Override
            public void onMoreClick(String cursor) {

            }

            @Override
            public void onErrorClick(String cursor) {

            }
        });

        mAdapter = new SubscriptionAdapter(this, R.layout.subscription_list_item, mList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SubscriptionActivity.this, BroadcastActivity.class);
                intent.putExtra("broadcast_id", mAdapter.getItem(position).id);
                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showActionDialog(position);
                return true;
            }
        });

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestSubscription();
            }
        });

        showLoadingView();

        requestSubscription();
    }

    private void showActionDialog(final int position) {
        final String[] items = new String[]{getString(R.string.unsubscribe)};
        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            unsubscribe(position);
                        }
                    }
                }).show();
    }

    private void unsubscribe(final int position) {
        showLoadingDialog();

        int broadcastId = mList.get(position).id;

        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(broadcastId), "subscription").toString();
        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(this, Request.Method.DELETE, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                        hideLoadingDialog();
                        mList.remove(position);
                        mAdapter.notifyDataSetChanged();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        hideLoadingDialog();
                        showToast(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoadingDialog();
                        showToast(R.string.network_error_message);
                    }
                });
        mRequestQueue.add(request);
    }

    private void requestSubscription() {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "subscription").toString();
        GsonRequest<BroadcastList> request = new GsonRequest<BroadcastList>(this, Request.Method.GET, url, BroadcastList.class,
                new Response.Listener<BroadcastList>() {
                    @Override
                    public void onResponse(BroadcastList response) {
                        if (response.item.size() == 0) {
                            showEmptyView(R.string.not_exists_subscription_broadcast);
                        } else {
                            mList.addAll(response.item);
                            mAdapter.notifyDataSetChanged();
                            mListView.notifyLoadingFinished(response.paging.next);
                            hideHelperViews();
                        }
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showNetworkErrorView(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showNetworkErrorView();
                    }
                });
        mRequestQueue.add(request);
    }

    private void requestSubscription(String cursor) {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "subscription").addParam("cursor", cursor).toString();
        GsonRequest<BroadcastList> request = new GsonRequest<BroadcastList>(this, Request.Method.GET, url, BroadcastList.class,
                new Response.Listener<BroadcastList>() {
                    @Override
                    public void onResponse(BroadcastList broadcastList) {
                        mList.addAll(broadcastList.item);
                        mAdapter.notifyDataSetChanged();

                        mListView.notifyLoadingFinished(broadcastList.paging.next);
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        mListView.notifyLoadingFailed();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListView.notifyLoadingFailed();
                    }
                });
        mRequestQueue.add(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
