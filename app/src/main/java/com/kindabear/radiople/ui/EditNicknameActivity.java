package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.content.Intent;
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

public class EditNicknameActivity extends BaseActivity {

    private final static String TAG = "EditNicknameActivity";

    private RequestQueue mRequestQueue = null;
    private TextInputLayout mNicknameInputLayout = null;
    private AppCompatEditText mNicknameEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_nickname_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mNicknameInputLayout = (TextInputLayout) findViewById(R.id.input_layout_nickname);
        mNicknameEditText = (AppCompatEditText) findViewById(R.id.edittext_nickname);

//        final int userId = new UserService(this).getId();

        findViewById(R.id.button_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    requestEdit(1);
                }
            }
        });
    }

    private void requestEdit(int userId) {
        showLoadingDialog();

        final String nickname = mNicknameEditText.getText().toString();

        String url = new ApiUrlBuilder().addPath("v1", "user", String.valueOf(userId), "nickname").toString();
        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(this, Request.Method.PUT, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                        hideLoadingDialog();

                        showToast(R.string.changed_successfully);

                        Intent intent = getIntent();
                        intent.putExtra("nickname", nickname);
                        setResult(RESULT_OK, intent);
                        finish();
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
        params.put("nickname", nickname);
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

    private boolean validate() {
        String nickname = mNicknameEditText.getText().toString();
        if (nickname.isEmpty()) {
            mNicknameInputLayout.setErrorEnabled(true);
            mNicknameInputLayout.setError(getString(R.string.please_enter_a_nickname));
            return false;
        }

        if (nickname.length() < Validation.MIN_NICKNAME_LENGTH || nickname.length() > Validation.MAX_NICKNAME_LENGTH) {
            mNicknameInputLayout.setErrorEnabled(true);
            mNicknameInputLayout.setError(getString(R.string.invalid_nickname_length));
            return false;
        }

        if (!nickname.matches(Validation.PATTERN_NICKNAME)) {
            mNicknameInputLayout.setErrorEnabled(true);
            mNicknameInputLayout.setError(getString(R.string.invalid_nickname_rule));
            return false;
        }

        mNicknameInputLayout.setErrorEnabled(false);

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
