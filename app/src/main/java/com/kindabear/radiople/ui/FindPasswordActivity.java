package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
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
import com.kindabear.radiople.response.EmptyJson;
import com.kindabear.radiople.util.Validation;

import java.util.HashMap;

public class FindPasswordActivity extends BaseActivity {

    private final static String TAG = "EditEmailActivity";

    private RequestQueue mRequestQueue = null;
    private TextInputLayout mEmailInputLayout = null;
    private AppCompatEditText mEmailEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_password_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mEmailInputLayout = (TextInputLayout) findViewById(R.id.input_layout_email);
        mEmailEditText = (AppCompatEditText) findViewById(R.id.edittext_email);

        findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    requestFind();
                }
            }
        });
    }

    private void requestFind() {
        showLoadingDialog();

        final String email = mEmailEditText.getText().toString();

        String url = new ApiUrlBuilder().addPath("auth", "find-password").toString();
        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(this, Request.Method.POST, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                        hideLoadingDialog();
                        showSuccessDialog(getString(R.string.sent_an_email_which_provide_reset_password));
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        hideLoadingDialog();
                        showErrorDialog(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoadingDialog();
                        showErrorDialog(getString(R.string.network_error_message));
                    }
                });

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        request.setParams(params);

        mRequestQueue.add(request);
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
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

        return true;
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
