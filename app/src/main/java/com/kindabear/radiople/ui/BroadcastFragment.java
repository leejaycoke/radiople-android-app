package com.kindabear.radiople.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.util.DateUtils;
import com.kindabear.radiople.view.SizingImageView;
import com.kindabear.radiople.view.sizingimageview.OnSizeChangedListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class BroadcastFragment extends BaseFragment {

    private View mView = null;
    private RequestQueue mRequestQueue = null;

    private Broadcast mBroadcast = null;

    private AppCompatButton mSubscribeButton = null;
    private TextView mPeopleSubscriptionCountTextView = null;

    private TextView mPeopleRatingCountTextView = null;
    private TextView mRatingAverageTextView = null;
    private TextView mMyRatingPointTextView = null;

    private AppCompatRatingBar mRatingBar = null;

    public static BroadcastFragment newInstance(Broadcast broadcast) {
        BroadcastFragment fragment = new BroadcastFragment();
        Bundle args = new Bundle();
        args.putSerializable("broadcast", broadcast);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.broadcast_fragment, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(getActivity());

        mBroadcast = (Broadcast) getArguments().getSerializable("broadcast");

        initBroacast();

        hideHelperViews();
    }

    private void initBroacast() {
        getActivity().setTitle(mBroadcast.title);

        final SizingImageView coverImageView = (SizingImageView) mView.findViewById(R.id.imageview_cover);
        coverImageView.setOnSizeChangedListener(new OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                Picasso.with(getActivity()).load(ImageUrlHelper.create(mBroadcast.coverImage, width, height)).fit().into(coverImageView);
            }
        });

        TextView categoryTextView = (TextView) mView.findViewById(R.id.textview_category);
        categoryTextView.setText(mBroadcast.category.name);

        TextView castingTextView = (TextView) mView.findViewById(R.id.textview_broadcast_casting);
        castingTextView.setText(mBroadcast.getCasting());

        TextView titleTextView = (TextView) mView.findViewById(R.id.textview_broadcast_title);
        titleTextView.setText(mBroadcast.title);

        TextView descriptionTextView = (TextView) mView.findViewById(R.id.textview_description);
        if (mBroadcast.description != null) {
            descriptionTextView.setText(mBroadcast.description);
        } else {
            descriptionTextView.setText(getString(R.string.not_exists_broadcast_description));
        }

        TextView latestAirDateTextView = (TextView) mView.findViewById(R.id.textview_latest_air_date);
        if (mBroadcast.latestAirDate != null) {
            String latestUpdate = getString(R.string.latest_air_date) + ": " + DateUtils.humunize(mBroadcast.latestAirDate, DateUtils.FORMAT_DATE, false);
            latestAirDateTextView.setText(latestUpdate);
        } else {
            latestAirDateTextView.setText(getString(R.string.not_exists_latest_air_date));
        }

        mPeopleSubscriptionCountTextView = (TextView) mView.findViewById(R.id.textview_people_subscription_count);

        mSubscribeButton = (AppCompatButton) mView.findViewById(R.id.button_subscribe);

        mSubscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getUserService().exists()) {
                    showRequiredLoginDialog();
                    return;
                }
                if (mBroadcast.activity.isSubscriber) {
                    cancelSubscription();
                } else {
                    subscribe();
                }
            }
        });

        mRatingBar = (AppCompatRatingBar) mView.findViewById(R.id.ratingbar);
        mRatingAverageTextView = (TextView) mView.findViewById(R.id.textview_rating_average);
        mPeopleRatingCountTextView = (TextView) mView.findViewById(R.id.textview_people_rating_count);

        LinearLayout ratingView = (LinearLayout) mView.findViewById(R.id.rating_view);
        if (getUserService().exists()) {
            ratingView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRatingDialog();
                }
            });
        } else {
            ratingView.setClickable(false);
        }

        mMyRatingPointTextView = (TextView) mView.findViewById(R.id.textview_my_rating_point);

        initSubscription();
        initRating();
    }

    private void showRequiredLoginDialog() {
        AppCompatDialog dialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.needs_login)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivityForResult(intent, 1);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void initSubscription() {
        mPeopleSubscriptionCountTextView.setText(getString(R.string.people_subscription_count, mBroadcast.scoreboard.subscriptionCount));

        if (mBroadcast.activity.isSubscriber) {
            mSubscribeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_done, 0, 0, 0);
            mSubscribeButton.setText(getString(R.string.cancel_subscription));
        } else {
            mSubscribeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_content_add, 0, 0, 0);
            mSubscribeButton.setText(getString(R.string.subscribe));
        }
    }

    private void initRating() {
        mRatingBar.setRating(mBroadcast.scoreboard.getRatingAverage());
        mRatingAverageTextView.setText(String.valueOf(mBroadcast.scoreboard.getRatingAverage()));
        mPeopleRatingCountTextView.setText(String.valueOf(mBroadcast.scoreboard.ratingCount));

        if (mBroadcast.activity.isRated()) {
            mMyRatingPointTextView.setText(getString(R.string.my_rating_point, mBroadcast.activity.ratingPoint));
            mMyRatingPointTextView.setVisibility(View.VISIBLE);
        } else {
            mMyRatingPointTextView.setVisibility(View.GONE);
        }
    }

    private void showRatingDialog() {
        View ratingView = LayoutInflater.from(getActivity()).inflate(R.layout.rating_view, null);
        final AppCompatRatingBar ratingBar = (AppCompatRatingBar) ratingView.findViewById(R.id.ratingbar);
        final TextView ratingPointTextView = (TextView) ratingView.findViewById(R.id.textview_rating_point);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating > 0) {
                    ratingPointTextView.setText(String.valueOf(rating));
                } else {
                    ratingPointTextView.setText(R.string.cancel);
                }
            }
        });

        if (mBroadcast.activity.isRated()) {
            ratingBar.setRating(mBroadcast.activity.ratingPoint);
            ratingPointTextView.setText(String.valueOf(mBroadcast.activity.ratingPoint));
        } else {
            ratingBar.setRating(0);
        }

        AppCompatDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(ratingView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        float point = ratingBar.getRating();
                        if (point > 0.0f) {
                            if (mBroadcast.activity.ratingPoint != point) {
                                rate(point);
                            }
                        } else {
                            if (mBroadcast.activity.isRated()) {
                                cancelRating();
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        dialog.show();
    }

    private void rate(final float rating) {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "rating").toString();

        GsonRequest<Broadcast> request = new GsonRequest<Broadcast>(getActivity(), Request.Method.PUT, url, Broadcast.class,
                new Response.Listener<Broadcast>() {
                    @Override
                    public void onResponse(Broadcast broadcast) {
                        mBroadcast = broadcast;
                        initRating();
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

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("point", String.valueOf(rating));

        request.setParams(params);

        mRequestQueue.add(request);
    }

    private void cancelRating() {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "rating").toString();

        GsonRequest<Broadcast> request = new GsonRequest<Broadcast>(getActivity(), Request.Method.DELETE, url, Broadcast.class,
                new Response.Listener<Broadcast>() {
                    @Override
                    public void onResponse(Broadcast broadcast) {
                        mBroadcast = broadcast;
                        initRating();
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

    private void subscribe() {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "subscription").toString();

        GsonRequest<Broadcast> request = new GsonRequest<Broadcast>(getActivity(), Request.Method.PUT, url, Broadcast.class,
                new Response.Listener<Broadcast>() {
                    @Override
                    public void onResponse(Broadcast broadcast) {
                        mBroadcast = broadcast;
                        initSubscription();
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

    private void cancelSubscription() {
        showLoadingDialog();

        String url = new ApiUrlBuilder().addPath("v1", "broadcast", String.valueOf(mBroadcast.id), "subscription").toString();

        GsonRequest<Broadcast> request = new GsonRequest<Broadcast>(getActivity(), Request.Method.DELETE, url, Broadcast.class,
                new Response.Listener<Broadcast>() {
                    @Override
                    public void onResponse(Broadcast broadcast) {
                        mBroadcast = broadcast;
                        initSubscription();
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
}
