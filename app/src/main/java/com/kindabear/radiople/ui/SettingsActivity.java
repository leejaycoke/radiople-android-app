package com.kindabear.radiople.ui;

import android.os.Bundle;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.db.SettingService;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Setting;

public class SettingsActivity extends BaseActivity {

    private RequestQueue mRequestQueue = null;

    private SettingService mSettingService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mSettingService = new SettingService(this);

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                requestSetting();
            }
        });

        if (getUserService().exists() && mSettingService.isEmpty()) {
            showLoadingView();
            requestSetting();
        } else {
            startSettingsFragment();
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

    private void startSettingsFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new SettingsFragment()).commit();
    }

    private void requestSetting() {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "setting").toString();
        GsonRequest<Setting> request = new GsonRequest<Setting>(this, Request.Method.GET, url, Setting.class,
                new Response.Listener<Setting>() {
                    @Override
                    public void onResponse(Setting response) {
                        mSettingService.setAllPush(response.allPush);
                        mSettingService.setUserPush(response.userPush);
                        mSettingService.setSubscriptionPush(response.subscriptionPush);
                        startSettingsFragment();
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
}
