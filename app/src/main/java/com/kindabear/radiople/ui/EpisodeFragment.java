package com.kindabear.radiople.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.EpisodeAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.response.Episode;
import com.kindabear.radiople.response.EpisodeList;
import com.kindabear.radiople.response.Storage;
import com.kindabear.radiople.view.paginglistview.PagingListView;
import com.kindabear.radiople.view.paginglistview.PagingListener;

import java.io.File;
import java.util.ArrayList;

public class EpisodeFragment extends BaseFragment {

    private View mView = null;

    private RequestQueue mRequestQueue = null;

    private Broadcast mBroadcast = null;
    private PagingListView mListView = null;
    private EpisodeAdapter mAdapter = null;

    private ArrayList<Episode> mList = null;

    public static EpisodeFragment newInstance(Broadcast broadcast) {
        EpisodeFragment fragment = new EpisodeFragment();
        Bundle args = new Bundle();
        args.putSerializable("broadcast", broadcast);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.episode_fragment, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(getActivity());

        mBroadcast = (Broadcast) getArguments().getSerializable("broadcast");

        mListView = (PagingListView) mView.findViewById(R.id.listview);
        mListView.setPagingListener(R.layout.helper_list_item, new PagingListener() {
            @Override
            public void onNextPage(String cursor) {
                requestEpisode(cursor);
            }

            @Override
            public void onMoreClick(String cursor) {

            }

            @Override
            public void onErrorClick(String cursor) {
                requestEpisode(cursor);
            }
        });

        mList = new ArrayList<Episode>();
        mAdapter = new EpisodeAdapter(getActivity(), R.layout.episode_list_item, mList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileType = mAdapter.getItem(position).storage.fileType;
                if (!fileType.equals(Storage.FileType.AUDIO)) {
                    showToast(getString(R.string.file_type_does_not_support, fileType));
                    return;
                }

                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra("episode_id", mAdapter.getItem(position).id);
                startActivity(intent);
            }
        });

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestEpisode();
            }
        });

        showLoadingView();
        requestEpisode();
    }

    private void requestEpisode() {
        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "episode").toString();
        GsonRequest<EpisodeList> request = new GsonRequest<EpisodeList>(getActivity(), Request.Method.GET, url, EpisodeList.class,
                new Response.Listener<EpisodeList>() {
                    @Override
                    public void onResponse(EpisodeList response) {
                        mList.addAll(response.item);
                        mAdapter.notifyDataSetChanged();
                        mListView.notifyLoadingFinished(response.paging.next);
                        hideHelperViews();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showToast(common.displayMessage);
                        showNetworkErrorView(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast(error.getMessage());
                        showNetworkErrorView();
                    }
                });

        mRequestQueue.add(request);
    }

    private void requestEpisode(String cursor) {
        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "episode").addParam("cursor", cursor).toString();
        GsonRequest<EpisodeList> request = new GsonRequest<EpisodeList>(getActivity(), Request.Method.GET, url, EpisodeList.class,
                new Response.Listener<EpisodeList>() {
                    @Override
                    public void onResponse(EpisodeList response) {
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
