package com.kindabear.radiople.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.SearchAdapter;
import com.kindabear.radiople.adapter.SearchHistoryAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.UrlBuilder;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.response.BroadcastList;
import com.kindabear.radiople.response.SearchHistory;
import com.kindabear.radiople.response.SearchHistoryList;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity {

    private final static String TAG = "SearchActivity";

    private final static int QUERY_HISTORY_REQUEST_DELAY = 800;

    private RequestQueue mRequeustQueue = null;

    private PagingListView mSearchListView = null;
    private PagingListView mHistoryListView = null;

    private SearchAdapter mSearchAdapter = null;
    private SearchHistoryAdapter mHistoryAdapter = null;

    private ArrayList<Broadcast> mSearchList = new ArrayList<Broadcast>();
    private ArrayList<SearchHistory> mHistoryList = new ArrayList<SearchHistory>();

    private Handler mHistorySearchHandler = new Handler();

    private EditText mKeywordEditText = null;

    private String mCurrentKeyword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("");

        mRequeustQueue = Volley.newRequestQueue(this);

        mSearchAdapter = new SearchAdapter(this, R.layout.search_list_item, mSearchList);
        mSearchListView = (PagingListView) findViewById(R.id.listview_search);
        mSearchListView.setAdapter(mSearchAdapter);

        mHistoryAdapter = new SearchHistoryAdapter(this, R.layout.search_history_list_item, mHistoryList);
        mHistoryListView = (PagingListView) findViewById(R.id.listview_search_history);
        mHistoryListView.setAdapter(mHistoryAdapter);

        mKeywordEditText = (EditText) findViewById(R.id.edittext_query);

        mKeywordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                    return false;
                }

                if (mCurrentKeyword.isEmpty()) {
                    return false;
                }

                hideKeyword();
                search();

                return true;
            }
        });

        mKeywordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                mCurrentKeyword = s.toString().trim();

                showHistoryView();

                requestHistory();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchListView.setPagingListener(R.layout.helper_list_item, new PagingListener() {
            @Override
            public void onNextPage(String cursor) {
                search(cursor);
            }

            @Override
            public void onMoreClick(String cursor) {

            }

            @Override
            public void onErrorClick(String cursor) {

            }
        });

        mHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String keyword = mHistoryAdapter.getItem(position).keyword;
                mKeywordEditText.setText(keyword);
                mKeywordEditText.setSelection(mCurrentKeyword.length());
            }
        });

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int broadcastId = mSearchAdapter.getItem(position).id;
                Intent intent = new Intent(SearchActivity.this, BroadcastActivity.class);
                intent.putExtra("broadcast_id", broadcastId);
                startActivity(intent);
            }
        });

        hideHelperViews();
    }

    private void requestHistory() {
        String url;
        UrlBuilder builder = new ApiUrlBuilder().addPath("v1", "search", "history");

        if (!mCurrentKeyword.isEmpty()) {
            url = builder.addParam("q", mCurrentKeyword).toString();
        } else {
            url = builder.toString();
        }

        GsonRequest<SearchHistoryList> request = new GsonRequest<SearchHistoryList>(this, Request.Method.GET, url, SearchHistoryList.class,
                new Response.Listener<SearchHistoryList>() {
                    @Override
                    public void onResponse(SearchHistoryList response) {
                        if (response.keyword != null && response.keyword.length() < mCurrentKeyword.length()) {
                            return;
                        }

                        mHistoryList.clear();

                        if (response.item.size() > 0) {
                            mHistoryList.addAll(response.item);
                        }

                        mHistoryAdapter.notifyDataSetChanged();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        mRequeustQueue.add(request);
    }

    private void search() {
        showLoadingView();

        String url = new ApiUrlBuilder().addPath("v1", "search", "broadcast").addParam("q", mCurrentKeyword).toString();
        GsonRequest<BroadcastList> request = new GsonRequest<BroadcastList>(this, Request.Method.GET, url, BroadcastList.class,
                new Response.Listener<BroadcastList>() {
                    @Override
                    public void onResponse(BroadcastList response) {
                        if (response.item.size() == 0) {
                            showEmptyView(R.string.no_search_results_were_found);
                        } else {
                            mSearchList.clear();
                            mSearchList.addAll(response.item);
                            mSearchAdapter.notifyDataSetChanged();
                            mSearchListView.notifyLoadingFinished(response.paging.next);

                            showSearchView();
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
                        showNetworkErrorView(R.string.network_error_message);
                    }
                });

        mRequeustQueue.add(request);
    }

    private void search(String cursor) {
        String url = new ApiUrlBuilder().addPath("v1", "search", "broadcast").addParam("q", mCurrentKeyword).addParam("cursor", cursor).toString();
        GsonRequest<BroadcastList> request = new GsonRequest<BroadcastList>(this, Request.Method.GET, url, BroadcastList.class,
                new Response.Listener<BroadcastList>() {
                    @Override
                    public void onResponse(BroadcastList response) {
                        mSearchList.addAll(response.item);
                        mSearchAdapter.notifyDataSetChanged();
                        mSearchListView.notifyLoadingFinished(response.paging.next);

                        hideHelperViews();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        mSearchListView.notifyLoadingFailed();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSearchListView.notifyLoadingFailed();
                    }
                });

        mRequeustQueue.add(request);
    }


    private void showHistoryView() {
        mSearchListView.setVisibility(View.GONE);
        mHistoryListView.setVisibility(View.VISIBLE);
    }

    private void showSearchView() {
        mHistoryListView.setVisibility(View.GONE);
        mSearchListView.setVisibility(View.VISIBLE);
    }

    private void hideKeyword() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if (mHistoryListView.getVisibility() == View.VISIBLE) {
            showSearchView();
        } else {
            super.onBackPressed();
        }
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
