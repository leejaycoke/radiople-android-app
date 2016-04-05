package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;

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
import com.kindabear.radiople.response.SystemCheck;
import com.kindabear.radiople.response.User;
import com.kindabear.radiople.service.PushAgent;

public class SystemCheckActivity extends BaseActivity {

    private RequestQueue mRequestQueue = null;

    private SystemCheck mSystemCheck = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_check_activity);

        setStatusBarColor(Color.BLACK);

        mRequestQueue = Volley.newRequestQueue(this);

        if (getUserService().exists()) {
            Intent intent = new Intent(this, PushAgent.class);
            intent.setAction(PushAgent.Action.RUN);
            startService(intent);
        }

        requestSystemCheck();
    }

    private void requestSystemCheck() {
        String url = new ApiUrlBuilder().addPath("v1", "system", "check").toString();
        GsonRequest<SystemCheck> request = new GsonRequest<SystemCheck>(this, Request.Method.GET, url, SystemCheck.class,
                new Response.Listener<SystemCheck>() {
                    @Override
                    public void onResponse(SystemCheck systemCheck) {
                        mSystemCheck = systemCheck;
                        initEnvironment();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showNetworkErrorDialog(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showNetworkErrorDialog(getString(R.string.network_error_message));
                    }
                });

        mRequestQueue.add(request);
    }

    private void initEnvironment() {
        if (mSystemCheck.clientVersion != null && mSystemCheck.clientVersion.hasUpdate) {
            showUpdateDialog();
        } else {
            initStaticImages();
        }
    }

    private void showUpdateDialog() {
        AppCompatDialog dialog = new AlertDialog.Builder(this)
                .setMessage(mSystemCheck.clientVersion.isForce ? R.string.new_force_update_message : R.string.new_update_message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (mSystemCheck.clientVersion.isForce) {
                            finish();
                        } else {
                            initStaticImages();
                        }
                    }
                }).create();
        dialog.show();
    }

    private void initStaticImages() {
        for (com.kindabear.radiople.response.StaticImage staticImage : mSystemCheck.staticImages) {
//            mStaticImageService.create(staticImage.position, staticImage.image);
        }

        requestUserProfile();
    }

    private void requestUserProfile() {
        if (!getUserService().exists()) {
            enterMainActivity();
            return;
        }

        String url = new ApiUrlBuilder().addPath("v1", "user", "me").toString();
        GsonRequest<User> request = new GsonRequest<User>(this, Request.Method.GET, url, User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User user) {
                        getUserService().set(user);
                        enterMainActivity();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showNetworkErrorDialog(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showNetworkErrorDialog(getString(R.string.network_error_message));
                    }
                });

        mRequestQueue.add(request);
    }

    private void showNetworkErrorDialog(String message) {
        if (isFinishing()) {
            return;
        }
        AppCompatDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create();
        dialog.show();
    }

    private void enterMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
