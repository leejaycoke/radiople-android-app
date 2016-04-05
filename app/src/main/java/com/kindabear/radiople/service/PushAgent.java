package com.kindabear.radiople.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kindabear.radiople.BuildConfig;
import com.kindabear.radiople.R;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.response.EmptyJson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class PushAgent extends Service {

    private final static String TAG = "PushAgent";

    private final static int TASK_DELAY = 15000;
    private final static int TASK_PERIOD = 3600000;

    private RequestQueue mRequestQueue = null;
    private UserService mUserService = null;

    private Timer mTimer = null;

    private String mCurrentPushToken = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestQueue = Volley.newRequestQueue(this);
        mUserService = new UserService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();

            if (action.equals(Action.RUN)) {
                startTask();
            } else if (action.equals(Action.TOKEN_REFRESHED)) {
                process();
            }
        }

        return START_STICKY;
    }

    private void startTask() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mUserService.exists()) {
                        process();
                    }
                }
            }, TASK_DELAY, TASK_PERIOD);
        }
    }

    private void registerPushToken(final String pushToken) {
        String url = new ApiUrlBuilder().addPath("v1", "user", String.valueOf(mUserService.getId()), "device").toString();

        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(this, Request.Method.PUT, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                        mCurrentPushToken = pushToken;
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

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("push_token", pushToken);

        request.setParams(params);

        mRequestQueue.add(request);
    }

    private void process() {
        if (checkPlayServices()) {
            Log.i(TAG, ">>>>> process : true");
            Thread request = new Thread(new Runnable() {
                @Override
                public void run() {
                    InstanceID instanceId = InstanceID.getInstance(PushAgent.this);

                    String senderId;
                    if (BuildConfig.DEBUG) {
                        senderId = getString(R.string.development_sender_id);
                    } else {
                        senderId = getString(R.string.production_sender_id);
                    }

                    try {
                        String pushToken = instanceId.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        if (pushToken != null) {
                            Log.i(TAG, ">>>>> process : pushToken : " + pushToken);
                            if (mCurrentPushToken == null || !pushToken.equals(mCurrentPushToken)) {
                                registerPushToken(pushToken);
                            }
                        } else {
                            Log.i(TAG, ">>>>> process : pushToken null");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            request.start();
        } else {
            Log.i(TAG, ">>>>> process : failed checkPlayServices");
            stopSelf();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public final static class Action {
        public final static String RUN = "run";
        public final static String TOKEN_REFRESHED = "token_refreshed";
        public final static String STOP = "stop";
    }
}
