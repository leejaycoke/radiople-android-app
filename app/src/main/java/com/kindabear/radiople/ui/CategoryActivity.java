package com.kindabear.radiople.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.CategoryBroadcastAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.response.BroadcastList;
import com.kindabear.radiople.util.Constants;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.util.ArrayList;

public class CategoryActivity extends BaseActivity {

    private final static String TAG = "SubscriptionActivity";

    private PagingListView mListView = null;
    private CategoryBroadcastAdapter mAdapter = null;

    private ArrayList<Broadcast> mList = new ArrayList<Broadcast>();

    private RequestQueue mRequestQueue = null;

    private int mCategorytId = 0;
    private String mSort = Constants.Sort.POPULAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");

        mCategorytId = intent.getIntExtra("category_id", 0);

        mActionBar.setTitle(title);

        mRequestQueue = Volley.newRequestQueue(this);

        mListView = (PagingListView) findViewById(R.id.listview);
        mListView.setPagingListener(R.layout.helper_list_item, new PagingListener() {
            @Override
            public void onNextPage(String cursor) {
                requestBroadcast(cursor);
            }

            @Override
            public void onMoreClick(String cursor) {
            }

            @Override
            public void onErrorClick(String cursor) {
                requestBroadcast(cursor);
            }
        });

        mAdapter = new CategoryBroadcastAdapter(this, R.layout.category_broadcast_list_item, mList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CategoryActivity.this, BroadcastActivity.class);
                intent.putExtra("broadcast_id", mAdapter.getItem(position).id);
                startActivity(intent);
            }
        });

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestBroadcast();
            }
        });

        mActionBar.setSubtitle(getSubtitleBySort(mSort));

        requestBroadcast();
    }

    private void requestBroadcast() {
        showLoadingView();

        String url = new ApiUrlBuilder().addPath("v1", "category", String.valueOf(mCategorytId), "broadcast").addParam("sort", mSort).toString();
        GsonRequest<BroadcastList> request = new GsonRequest<BroadcastList>(this, Request.Method.GET, url, BroadcastList.class,
                new Response.Listener<BroadcastList>() {
                    @Override
                    public void onResponse(BroadcastList response) {
                        if (response.item.size() > 0) {
                            mList.clear();
                            mList.addAll(response.item);
                            mAdapter.notifyDataSetChanged();
                            mListView.notifyLoadingFinished(response.paging.next);
                            hideHelperViews();
                        } else {
                            showEmptyView(R.string.not_exists_broadcast);
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

    private void requestBroadcast(String cursor) {
        String url = new ApiUrlBuilder().addPath("v1", "category", String.valueOf(mCategorytId), "broadcast").addParam("cursor", cursor).addParam("sort", mSort).toString();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_broadcast_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            String reqSort = getSortByItemId(item.getItemId());
            if (!reqSort.equals(mSort)) {
                mSort = reqSort;
                mActionBar.setSubtitle(getSubtitleBySort(mSort));
                requestBroadcast();
            }
        }

        return true;
    }

    private String getSortByItemId(int itemId) {
        switch (itemId) {
            case R.id.action_list_by_popular:
                return Constants.Sort.POPULAR;
            case R.id.action_list_by_rating:
                return Constants.Sort.RATING;
            case R.id.action_list_by_subscription_count:
                return Constants.Sort.SUBSCRIPTION_COUNT;
            default:
                return Constants.Sort.LATEST_AIR_DATE;
        }
    }

    private String getSubtitleBySort(String sort) {
        if (sort.equals(Constants.Sort.POPULAR)) {
            return getString(R.string.list_by_popular);
        } else if (sort.equals(Constants.Sort.RATING)) {
            return getString(R.string.list_by_rating);
        } else if (sort.equals(Constants.Sort.SUBSCRIPTION_COUNT)) {
            return getString(R.string.list_by_subscription_count);
        } else {
            return getString(R.string.list_by_latest_air_date);
        }
    }
}
