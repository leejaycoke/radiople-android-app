package com.kindabear.radiople.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.RankingAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.response.BroadcastList;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.util.ArrayList;

public class RankingFragment extends BaseFragment {

    private RequestQueue mRequestQueue = null;

    private ArrayList<Broadcast> mList = null;

    private RankingAdapter mAdapter = null;

    private PagingListView mListView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ranking_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showLoadingView();

        mRequestQueue = Volley.newRequestQueue(getActivity());

        mListView = (PagingListView) mView.findViewById(R.id.listview);

        mList = new ArrayList<Broadcast>();
        mAdapter = new RankingAdapter(getActivity(), R.layout.ranking_list_item, mList);

        mListView.setAdapter(mAdapter);

        mListView.setPagingListener(R.layout.helper_list_item, new PagingListener() {
            @Override
            public void onNextPage(String cursor) {
                requestRanking(cursor);
            }

            @Override
            public void onMoreClick(String cursor) {

            }

            @Override
            public void onErrorClick(String cursor) {
                requestRanking(cursor);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), BroadcastActivity.class);
                intent.putExtra("broadcast_id", mList.get(position).id);
                startActivity(intent);
            }
        });

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestRanking();
            }
        });

        requestRanking();
    }

    private void requestRanking() {
        String url = new ApiUrlBuilder().addPath("v1", "main", "ranking").toString();
        GsonRequest<BroadcastList> request = new GsonRequest<BroadcastList>(getActivity(), Request.Method.GET, url, BroadcastList.class,
                new Response.Listener<BroadcastList>() {
                    @Override
                    public void onResponse(BroadcastList response) {
                        mList.addAll(response.item);
                        mAdapter.notifyDataSetChanged();
                        mListView.notifyLoadingFinished(response.paging.next);
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
                        showNetworkErrorView(R.string.network_error_message);
                    }
                });

        mRequestQueue.add(request);
    }

    private void requestRanking(String cursor) {
        String url = new ApiUrlBuilder().addPath("v1", "main", "ranking").addParam("cursor", cursor).toString();
        GsonRequest<BroadcastList> request = new GsonRequest<BroadcastList>(getActivity(), Request.Method.GET, url, BroadcastList.class,
                new Response.Listener<BroadcastList>() {
                    @Override
                    public void onResponse(BroadcastList response) {
                        mList.addAll(response.item);
                        mAdapter.notifyDataSetChanged();
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

        mRequestQueue.add(request);
    }

}
