package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Patterns;
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
import com.kindabear.radiople.util.Validation;

import java.util.HashMap;

public class SignupActivity extends BaseActivity {

    private RequestQueue mRequestQueue = null;

    private TextInputLayout mEmailInputLayout = null;
    private TextInputLayout mNicknameInputLayout = null;
    private TextInputLayout mPasswordInputLayout = null;

    private AppCompatEditText mEmailEditText = null;
    private AppCompatEditText mNicknameEditText = null;
    private AppCompatEditText mPasswordEditText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        setStatusBarColor(Color.BLACK);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mEmailInputLayout = (TextInputLayout) findViewById(R.id.input_layout_email);
        mNicknameInputLayout = (TextInputLayout) findViewById(R.id.input_layout_nickname);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_password);

        mEmailEditText = (AppCompatEditText) findViewById(R.id.edittext_email);
        mNicknameEditText = (AppCompatEditText) findViewById(R.id.edittext_nickname);
        mPasswordEditText = (AppCompatEditText) findViewById(R.id.edittext_password);

        AppCompatButton btnConfirm = (AppCompatButton) findViewById(R.id.button_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    requestSignup();
                }
            }
        });
    }

    private boolean validate() {
        return validateEmail() && validateNickname() && validatePassword();
    }

    private boolean validateEmail() {
        String email = mEmailEditText.getText().toString();
        if (email.isEmpty()) {
            mEmailInputLayout.setErrorEnabled(true);
            mEmailInputLayout.setError(getString(R.string.please_enter_an_email));
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailInputLayout.setErrorEnabled(true);
            mEmailInputLayout.setError(getString(R.string.please_enter_a_valid_email));
            return false;
        }

        mEmailInputLayout.setErrorEnabled(false);
        return true;
    }

    private boolean validateNickname() {
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

    private boolean validatePassword() {
        String password = mPasswordEditText.getText().toString();
        if (password.isEmpty()) {
            mPasswordInputLayout.setErrorEnabled(true);
            mPasswordInputLayout.setError(getString(R.string.please_enter_a_new_password));
            return false;
        }

        if (!password.matches(Validation.PATTERN_PASSWORD)) {
            mPasswordInputLayout.setErrorEnabled(true);
            mPasswordInputLayout.setError(getString(R.string.invalid_password_rule));
            return false;
        }

        if (password.length() < Validation.MIN_PASSWORD_LENGTH || password.length() > Validation.MAX_PASSWORD_LENGTH) {
            mPasswordInputLayout.setErrorEnabled(true);
            mPasswordInputLayout.setError(getString(R.string.invalid_password_length));
            return false;
        }

        mPasswordInputLayout.setErrorEnabled(false);

        return true;
    }

    private void requestSignup() {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("auth", "register").toString();

        GsonRequest<UserSession> request = new GsonRequest<UserSession>(this, Request.Method.POST, url, UserSession.class,
                new Response.Listener<UserSession>() {
                    @Override
                    public void onResponse(UserSession userSession) {
                        getUserService().set(userSession.user);
                        getSessionService().set(userSession.session);
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
        params.put("email", mEmailEditText.getText().toString());
        params.put("nickname", mNicknameEditText.getText().toString());
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

    private void showSuccessDialog() {
        AppCompatDialog dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.signup_success)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create();
        dialog.show();
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
