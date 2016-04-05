package com.kindabear.radiople.ui;

import android.content.Intent;
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
import com.kindabear.radiople.adapter.EpisodeHistoryAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.History;
import com.kindabear.radiople.response.HistoryList;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.util.ArrayList;

public class EpisodeHistoryActivity extends BaseActivity {

    private final static String TAG = "EpisodeHistoryActivity";

    private PagingListView mListView = null;
    private EpisodeHistoryAdapter mAdapter = null;

    private ArrayList<History> mList = new ArrayList<History>();

    private RequestQueue mRequestQueue = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mListView = (PagingListView) findViewById(R.id.listview);
        mListView.setPagingListener(R.layout.helper_list_item, new PagingListener() {
            @Override
            public void onNextPage(String cursor) {
                requestHistory(cursor);
            }

            @Override
            public void onMoreClick(String cursor) {

            }

            @Override
            public void onErrorClick(String cursor) {

            }
        });

        mAdapter = new EpisodeHistoryAdapter(this, R.layout.history_list_item, mList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(EpisodeHistoryActivity.this, PlayerActivity.class);
                intent.putExtra("episode_id", mAdapter.getItem(position).episode.id);
                intent.putExtra("position", mAdapter.getItem(position).position);
                startActivity(intent);
            }
        });

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestHistory();
            }
        });

        showLoadingView();

        requestHistory();
    }

    private void requestHistory() {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "history").toString();
        GsonRequest<HistoryList> history = new GsonRequest<HistoryList>(this, Request.Method.GET, url, HistoryList.class,
                new Response.Listener<HistoryList>() {
                    @Override
                    public void onResponse(HistoryList response) {
                        if (response.item.size() > 0) {
                            mList.addAll(response.item);
                            mAdapter.notifyDataSetChanged();

                            mListView.notifyLoadingFinished(response.paging.next);

                            hideHelperViews();
                        } else {
                            showEmptyView(R.string.not_exists_history);
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
                        showNetworkErrorView(R.string.network_error_message);
                    }
                });

        mRequestQueue.add(history);
    }

    private void requestHistory(String cursor) {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "history").addParam("cursor", cursor).toString();
        GsonRequest<HistoryList> history = new GsonRequest<HistoryList>(this, Request.Method.GET, url, HistoryList.class,
                new Response.Listener<HistoryList>() {
                    @Override
                    public void onResponse(HistoryList response) {
                        if (response.item.size() > 0) {
                            mList.addAll(response.item);
                            mAdapter.notifyDataSetChanged();
                        }

                        mListView.notifyLoadingFinished(response.paging.next);
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

        mRequestQueue.add(history);
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
