package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.FileRequest;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.ImageUrlBuilder;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.EmptyJson;
import com.kindabear.radiople.response.Url;
import com.kindabear.radiople.response.User;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends BaseActivity {

    private final static String TAG = "UserActivity";

    private final static int INTENT_EDIT_EMAIL = 0;
    private final static int INTENT_EDIT_NICKNAME = 1;

    private RequestQueue mRequestQueue = null;

    private User mUser = null;

    private LinearLayout mEmailValidationView = null;

    private TextView mEmailTextView = null;
    private TextView mNicknameTextView = null;
    private TextView mEmailValidationTextView = null;
    private CircleImageView mProfileImageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

        mActionBar.setDisplayHomeAsUpEnabled(true);

        mRequestQueue = Volley.newRequestQueue(this);

        mEmailTextView = (TextView) findViewById(R.id.textview_email);
        mNicknameTextView = (TextView) findViewById(R.id.textview_nickname);
        mEmailValidationTextView = (TextView) findViewById(R.id.textview_email_validation);
        mProfileImageView = (CircleImageView) findViewById(R.id.imageview_profile);

        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crop.pickImage(UserActivity.this);
            }
        });

        mEmailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, EditEmailActivity.class);
                startActivityForResult(intent, INTENT_EDIT_EMAIL);
            }
        });

        mNicknameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, EditNicknameActivity.class);
                startActivityForResult(intent, INTENT_EDIT_NICKNAME);
            }
        });

        findViewById(R.id.textview_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserActivity.this, EditPasswordActivity.class));
            }
        });

        mEmailValidationView = (LinearLayout) findViewById(R.id.email_validation_view);

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                requestUserProfile();
            }
        });

        showLoadingView();

        requestUserProfile();
    }

    private void initUser() {
        Picasso.with(this)
                .load(ImageUrlHelper.create(mUser.profileImage, 500, 500))
                .placeholder(R.drawable.ic_default_profile)
                .fit()
                .into(mProfileImageView);

        mNicknameTextView.setText(mUser.nickname);

        mEmailTextView.setText(mUser.email);

        if (mUser.isVerified) {
            mEmailValidationView.setVisibility(View.GONE);
            mEmailValidationTextView.setOnClickListener(null);
        } else {
            mEmailValidationView.setVisibility(View.VISIBLE);
            mEmailValidationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEmailValidationDialog();
                }
            });
        }
    }

    private void requestUserProfile() {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me").toString();
        GsonRequest<User> request = new GsonRequest<User>(this, Request.Method.GET, url, User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        mUser = response;
                        getUserService().set(response);
                        initUser();
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
                        showNetworkErrorView(R.string.network_error_message);
                    }
                });

        mRequestQueue.add(request);
    }

    private void showEmailValidationDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.resending_email_validation_email, mUser.email))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        resendEmailValidation();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void resendEmailValidation() {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("auth", "email-validation").toString();
        GsonRequest<User> request = new GsonRequest<User>(this, Request.Method.POST, url, User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        showToast(getString(R.string.sent_an_email_which_provide_validate_email, mUser.email));
                        hideLoadingDialog();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        showToast(common.displayMessage);
                        hideLoadingDialog();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast(R.string.network_error_message);
                        hideLoadingDialog();
                    }
                });

        mRequestQueue.add(request);
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void updateProfileImage(final String imageUrl) {
        String url = new ApiUrlBuilder().addPath("v1", "user", "me", "profile_image").toString();
        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(this, Request.Method.PUT, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                        Picasso.with(UserActivity.this).load(ImageUrlHelper.create(imageUrl, 500, 500)).fit().into(mProfileImageView);
                        hideLoadingDialog();
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        hideLoadingDialog();
                        showToast(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoadingDialog();
                        showToast(R.string.network_error_message);
                    }
                });

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("profile_image", imageUrl);
        request.setParams(params);

        mRequestQueue.add(request);
    }

    private void uploadProfileImage(Uri uri) {
        showLoadingDialog();

        String url = new ImageUrlBuilder().addParam("service", "api").addParam("access_token", getSessionService().getAccessToken()).toString();
        FileRequest<Url> request = new FileRequest<Url>(this, Request.Method.PUT, url, Url.class,
                new Response.Listener<Url>() {
                    @Override
                    public void onResponse(Url response) {
                        updateProfileImage(response.url);
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        hideLoadingDialog();
                        showToast(common.displayMessage);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoadingDialog();
                        showToast(R.string.network_error_message);
                    }
                });

        request.setFile("file", new File(uri.getPath()));

        mRequestQueue.add(request);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == INTENT_EDIT_EMAIL) {
                mUser.email = data.getStringExtra("email");
                mUser.isVerified = false;
                initUser();
            } else if (requestCode == INTENT_EDIT_NICKNAME) {
                mUser.nickname = data.getStringExtra("nickname");
                initUser();
            } else if (requestCode == Crop.REQUEST_PICK) {
                beginCrop(data.getData());
            } else if (requestCode == Crop.REQUEST_CROP) {
                uploadProfileImage(Crop.getOutput(data));
            }
        }
    }
}
