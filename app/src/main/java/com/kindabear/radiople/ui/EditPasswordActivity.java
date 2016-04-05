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

public class EditPasswordActivity extends BaseActivity {

    private RequestQueue mRequestQueue = null;

    private TextInputLayout mCurrentPasswordInputLayout = null;
    private TextInputLayout mNewPasswordInputLayout = null;
    private TextInputLayout mNewConfirmPasswordInputLayout = null;

    private AppCompatEditText mCurrentPasswordEditText = null;
    private AppCompatEditText mNewPasswordEditText = null;
    private AppCompatEditText mNewConfirmPasswordEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_password_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mCurrentPasswordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_current_password);
        mNewPasswordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_new_password);
        mNewConfirmPasswordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_new_password_confirm);

        mCurrentPasswordEditText = (AppCompatEditText) findViewById(R.id.edittext_current_password);
        mNewPasswordEditText = (AppCompatEditText) findViewById(R.id.edittext_new_password);
        mNewConfirmPasswordEditText = (AppCompatEditText) findViewById(R.id.edittext_new_password_confirm);

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

        String url = new ApiUrlBuilder().addPath("v1", "user", String.valueOf(userId), "password").toString();
        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(this, Request.Method.PUT, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                        hideLoadingDialog();
                        showSuccessDialog();
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
        params.put("current_password", mCurrentPasswordEditText.getText().toString());
        params.put("new_password", mNewPasswordEditText.getText().toString());

        request.setParams(params);

        mRequestQueue.add(request);
    }

    private void showSuccessDialog() {
        showToast(R.string.changed_successfully);
        finish();
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
        return validateCurrentPassword() && validateNewPassword() && validateNewConfirmPassword();
    }

    private boolean validateCurrentPassword() {
        String currentPassword = mCurrentPasswordEditText.getText().toString();
        if (currentPassword.isEmpty()) {
            mCurrentPasswordInputLayout.setErrorEnabled(true);
            mCurrentPasswordInputLayout.setError(getString(R.string.please_enter_a_current_password));
            return false;
        }

        mCurrentPasswordInputLayout.setErrorEnabled(false);

        return true;
    }

    private boolean validateNewPassword() {
        String newPassword = mNewPasswordEditText.getText().toString();
        if (newPassword.isEmpty()) {
            mNewPasswordInputLayout.setErrorEnabled(true);
            mNewPasswordInputLayout.setError(getString(R.string.please_enter_a_new_password));
            return false;
        }

        if (!newPassword.matches(Validation.PATTERN_PASSWORD)) {
            mNewPasswordInputLayout.setErrorEnabled(true);
            mNewPasswordInputLayout.setError(getString(R.string.invalid_password_rule));
            return false;
        }

        if (newPassword.length() < Validation.MIN_PASSWORD_LENGTH || newPassword.length() > Validation.MAX_PASSWORD_LENGTH) {
            mNewPasswordInputLayout.setErrorEnabled(true);
            mNewPasswordInputLayout.setError(getString(R.string.invalid_password_length));
            return false;
        }

        mNewPasswordInputLayout.setErrorEnabled(false);

        return true;
    }

    private boolean validateNewConfirmPassword() {
        String newConfirmPassword = mNewConfirmPasswordEditText.getText().toString();
        if (newConfirmPassword.isEmpty()) {
            mNewConfirmPasswordInputLayout.setErrorEnabled(true);
            mNewConfirmPasswordInputLayout.setError(getString(R.string.please_enter_a_new_password_confirm));
            return false;
        }

        if (!newConfirmPassword.matches(Validation.PATTERN_PASSWORD)) {
            mNewConfirmPasswordInputLayout.setErrorEnabled(true);
            mNewConfirmPasswordInputLayout.setError(getString(R.string.invalid_password_rule));
            return false;
        }

        if (!mNewPasswordEditText.getText().toString().equals(newConfirmPassword)) {
            mNewConfirmPasswordInputLayout.setErrorEnabled(true);
            mNewConfirmPasswordInputLayout.setError(getString(R.string.password_does_not_match_the_confirm_password));
            return false;
        }

        mNewConfirmPasswordInputLayout.setErrorEnabled(false);

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
