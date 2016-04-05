package com.kindabear.radiople.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.NotificationAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Notification;
import com.kindabear.radiople.response.NotificationList;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.util.ArrayList;

public class NotificationActivity extends BaseActivity {

    private final static String TAG = "SubscriptionActivity";

    private PagingListView mListView = null;
    private NotificationAdapter mAdapter = null;

    private ArrayList<Notification> mList = new ArrayList<Notification>();

    private RequestQueue mRequestQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

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
                requestSubscription(cursor);
            }
        });

        mAdapter = new NotificationAdapter(this, R.layout.notification_list_item, mList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(NotificationActivity.this, BroadcastActivity.class);
//                intent.putExtra("broadcast_id", mAdapter.getItem(position).id);
//                startActivity(intent);
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

    private void requestSubscription() {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "notification").toString();
        GsonRequest<NotificationList> request = new GsonRequest<NotificationList>(this, Request.Method.GET, url, NotificationList.class,
                new Response.Listener<NotificationList>() {
                    @Override
                    public void onResponse(NotificationList notificationList) {
                        mList.addAll(notificationList.item);
                        mAdapter.notifyDataSetChanged();

                        mListView.notifyLoadingFinished(notificationList.paging.next);

                        hideHelperViews();
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
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "notification").addParam("cursor", cursor).toString();
        GsonRequest<NotificationList> request = new GsonRequest<NotificationList>(this, Request.Method.GET, url, NotificationList.class,
                new Response.Listener<NotificationList>() {
                    @Override
                    public void onResponse(NotificationList notificationList) {
                        mList.addAll(notificationList.item);
                        mAdapter.notifyDataSetChanged();

                        mListView.notifyLoadingFinished(notificationList.paging.next);
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
