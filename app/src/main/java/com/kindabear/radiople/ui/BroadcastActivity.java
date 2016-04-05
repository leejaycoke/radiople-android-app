package com.kindabear.radiople.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Broadcast;

public class BroadcastActivity extends BaseActivity {

    private RequestQueue mRequestQueue = null;
    private Broadcast mBroadcast = null;
    private int mBroadcastId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.broadcast_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        mBroadcastId = intent.getIntExtra("broadcast_id", 0);

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestBroadcast();
            }
        });

        showLoadingView();

        requestBroadcast();
    }

    private void requestBroadcast() {
        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcastId)).toString();
        GsonRequest<Broadcast> request = new GsonRequest<Broadcast>(this, Request.Method.GET, url, Broadcast.class,
                new Response.Listener<Broadcast>() {
                    @Override
                    public void onResponse(Broadcast broadcast) {
                        mBroadcast = broadcast;
                        initTableLayout();
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

    private void initTableLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new BroadcastFragmentAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setVisibility(View.VISIBLE);
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

    public class BroadcastFragmentAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = new String[]{getString(R.string.about_broadcast), getString(R.string.episode), getString(R.string.comment)};
        private final int PAGE_COUNT = 3;

        public BroadcastFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BroadcastFragment.newInstance(mBroadcast);
                case 1:
                    return EpisodeFragment.newInstance(mBroadcast);
                default:
                    return CommentFragment.newInstance(mBroadcast);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }
}
