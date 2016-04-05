package com.kindabear.radiople.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.kindabear.radiople.R;
import com.kindabear.radiople.RadiopleApplication;
import com.kindabear.radiople.response.UserSession;
import com.kindabear.radiople.service.SessionService;
import com.kindabear.radiople.service.UserService;
import com.kindabear.radiople.util.Constants;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {

    private final static String TAG = "GsonRequest";

    public static final int DEFAULT_TIMEOUT_MS = 15000;
    public static final int DEFAULT_MAX_RETRIES = 1;
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    private Context mContext = null;

    private Gson mGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private Class<T> mClazz = null;

    private Listener<T> mResponseListener = null;
    private CommonResponseListener mCommonListener = null;

    private final HashMap<String, String> mParams = new HashMap<String, String>();
    private final HashMap<String, String> mHeaders = new HashMap<String, String>();

    private final SessionService mSessionService;

    public GsonRequest(Context context, int method, String url, Class<T> clazz, Listener<T> responseListener, CommonResponseListener commonListener, ErrorListener errorListener) {
        super(method, url, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT));

        mContext = context;
        mClazz = clazz;
        mResponseListener = responseListener;
        mCommonListener = commonListener;

        mSessionService = new SessionService(context);

        setUserAgent();
        setAuthorization();
    }

    private void setUserAgent() {
        SessionService sessionService = ((RadiopleApplication) mContext.getApplicationContext()).getSessionService();
        if (sessionService.getAccessToken() != null) {
            mHeaders.put("Authorization", "Bearer " + sessionService.getAccessToken());
        }
    }

    private void setAuthorization() {
        mHeaders.put("User-Agent", String.format("radiople/%s android/%s (%s/%s)", Constants.getAppVersion(), Constants.getOsVersion(), Constants.getModel(), Constants.getProvider(mContext)));
    }

    public void setParams(HashMap<String, String> params) {
        mParams.clear();

        for (String key : params.keySet()) {
            mParams.put(key, params.get(key));
        }
    }

    @Override
    protected void deliverResponse(T response) {
        mResponseListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        Log.i(TAG, "gson request error");
        if (error.networkResponse == null) {
            Log.i(TAG, "error.networkResponse == null");
            super.deliverError(error);
        } else {
            int statusCode = error.networkResponse.statusCode;
            if (statusCode == StatusCode.SERVER_ERROR) {
                super.deliverError(error);
            } else {
                CommonResponse response = parseCommonResponse(error.networkResponse);
                if (response.code.equals(ErrorCode.EXPIRED_TOKEN)) {
                    refreshAccessToken();
                } else {
                    if (response.code.equals(ErrorCode.UNAUTHORIZED)) {
                        mSessionService.deleteAll();
                    }
                    mCommonListener.onCommonResponse(statusCode, response);
                }
            }
        }
    }

    private void refreshAccessToken() {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        String url = new ApiUrlBuilder().addPath("auth", "refresh-access-token").toString();

        GsonRequest<UserSession> request = new GsonRequest<UserSession>(mContext, Request.Method.GET, url, UserSession.class,
                new Response.Listener<UserSession>() {
                    @Override
                    public void onResponse(UserSession userSession) {
                        mSessionService.set(userSession.session);
                        retry();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        mCommonListener.onCommonResponse(statusCode, common);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        GsonRequest.this.deliverError(error);
                    }
                });

        requestQueue.add(request);
    }

    private void retry() {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        GsonRequest<T> request = new GsonRequest<T>(mContext, getMethod(), getUrl(), mClazz,
                new Response.Listener<T>() {
                    @Override
                    public void onResponse(T response) {
                        mResponseListener.onResponse(response);
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        mCommonListener.onCommonResponse(statusCode, common);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        GsonRequest.this.deliverError(error);
                    }
                });

        if (getMethod() != Request.Method.GET) {
            request.setParams(getParams());
        }

        requestQueue.add(request);
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    public HashMap<String, String> getParams() {
        return mParams != null ? mParams : null;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(mGson.fromJson(json, mClazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, e.getMessage());
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            Log.i(TAG, e.getMessage());
            return Response.error(new ParseError(e));
        }
    }

    private CommonResponse parseCommonResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return mGson.fromJson(json, CommonResponse.class);
        } catch (Exception e) {
            CommonResponse common = new CommonResponse();
            common.code = String.valueOf(response.statusCode);
            common.displayMessage = mContext.getString(R.string.network_error_message);
            return common;
        }
    }
}
