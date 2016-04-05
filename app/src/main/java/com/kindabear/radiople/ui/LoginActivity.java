package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatEditText;
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
import com.kindabear.radiople.response.UserSession;
import com.kindabear.radiople.service.PushAgent;
import com.kindabear.radiople.service.UserService;
import com.kindabear.radiople.util.Validation;

import java.util.HashMap;

public class LoginActivity extends BaseActivity {

    private RequestQueue mRequestQueue = null;

    private TextInputLayout mEmailInputLayout = null;
    private TextInputLayout mPasswordInputLayout = null;

    private AppCompatEditText mEmailEditText = null;
    private AppCompatEditText mPasswordEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mRequestQueue = Volley.newRequestQueue(this);

        setStatusBarColor(Color.BLACK);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mEmailInputLayout = (TextInputLayout) findViewById(R.id.input_layout_email);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_password);

        mEmailEditText = (AppCompatEditText) findViewById(R.id.edittext_email);

        mPasswordEditText = (AppCompatEditText) findViewById(R.id.edittext_password);

        findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    requestLogin();
                }
            }
        });

        findViewById(R.id.button_find_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, FindPasswordActivity.class));
            }
        });
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

    private boolean validate() {
        String email = mEmailEditText.getText().toString();
        if (email.isEmpty()) {
            mEmailInputLayout.setErrorEnabled(true);
            mEmailInputLayout.setError(getString(R.string.please_enter_an_email));
            return false;
        }

        if (!email.matches(Validation.PATTERN_EMAIL)) {
            mEmailInputLayout.setErrorEnabled(true);
            mEmailInputLayout.setError(getString(R.string.please_enter_a_valid_email));
            return false;
        }

        mEmailInputLayout.setErrorEnabled(false);

        if (mPasswordEditText.getText().toString().isEmpty()) {
            mPasswordInputLayout.setErrorEnabled(true);
            mPasswordInputLayout.setError(getString(R.string.please_enter_a_password));
            return false;
        }

        mPasswordInputLayout.setErrorEnabled(false);

        return true;
    }

    private void requestLogin() {
        showLoadingDialog();
        String url = new ApiUrlBuilder().addPath("auth", "login").toString();
        GsonRequest<UserSession> request = new GsonRequest<UserSession>(this, Request.Method.POST, url, UserSession.class,
                new Response.Listener<UserSession>() {
                    @Override
                    public void onResponse(UserSession userSession) {
                        getUserService().set(userSession.user);
                        getSessionService().set(userSession.session);

                        Intent pushService = new Intent(LoginActivity.this, PushAgent.class);
                        pushService.setAction(PushAgent.Action.RUN);
                        startService(pushService);

                        hideLoadingDialog();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showErrorDialog(common.displayMessage);
                        hideLoadingDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showErrorDialog(getString(R.string.network_error_message));
                        hideLoadingDialog();
                    }
                });

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", mEmailEditText.getText().toString());
        params.put("password", mPasswordEditText.getText().toString());

        request.setParams(params);

        mRequestQueue.add(request);
    }

    private void showErrorDialog(String message) {
        AppCompatDialog dialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

}
