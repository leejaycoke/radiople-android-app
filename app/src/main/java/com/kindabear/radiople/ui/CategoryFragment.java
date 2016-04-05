package com.kindabear.radiople.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.adapter.CategoryAdapter;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Categories;
import com.kindabear.radiople.response.Category;

import java.util.ArrayList;

public class CategoryFragment extends BaseFragment {

    private RequestQueue mRequestQueue = null;

    private ArrayList<Category> mList = null;

    private CategoryAdapter mAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.category_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showLoadingView();

        mRequestQueue = Volley.newRequestQueue(getActivity());

        ListView listView = (ListView) mView.findViewById(R.id.listview);

        mList = new ArrayList<Category>();
        mAdapter = new CategoryAdapter(getActivity(), R.layout.category_list_item, mList);

        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CategoryActivity.class);
                intent.putExtra("category_id", mList.get(position).id);
                intent.putExtra("title", mList.get(position).name);
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
        String url = new ApiUrlBuilder().addPath("v1", "category").toString();
        GsonRequest<Categories> request = new GsonRequest<Categories>(getActivity(), Request.Method.GET, url, Categories.class,
                new Response.Listener<Categories>() {
                    @Override
                    public void onResponse(Categories response) {
                        mList.addAll(response.categories);
                        mAdapter.notifyDataSetChanged();
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
}
