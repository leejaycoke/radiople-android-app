package com.kindabear.radiople.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.network.OnNetworkRetryListener;
import com.kindabear.radiople.response.Broadcast;
import com.kindabear.radiople.response.MainNews;
import com.kindabear.radiople.response.ThemeBroadcasts;
import com.kindabear.radiople.view.DividerItemDecoration;
import com.kindabear.radiople.view.SizingImageView;
import com.kindabear.radiople.view.sizingimageview.OnSizeChangedListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsFragment extends BaseFragment {

    private final static String TAG = "NewsFragment";

    private View mView = null;
    private RequestQueue mRequestQueue = null;
    private ViewPager mViewPager = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.main_news_fragment, container, false);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getActivity());

        setOnNetworkRetryListener(new OnNetworkRetryListener() {
            @Override
            public void onRetry() {
                showLoadingView();
                requestMainNews();
            }
        });

        showLoadingView();
        requestMainNews();
    }

    private void requestMainNews() {
        String url = new ApiUrlBuilder().addPath("v1", "main", "news").toString();
        GsonRequest<MainNews> request = new GsonRequest<MainNews>(getActivity(), Request.Method.GET, url, MainNews.class,
                new Response.Listener<MainNews>() {
                    @Override
                    public void onResponse(MainNews mainNews) {
                        setMainNews(mainNews);
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
                        showNetworkErrorView();
                    }
                });

        mRequestQueue.add(request);
    }

    private void setMainNews(final MainNews mainNews) {
        ViewGroup contentView = (LinearLayout) mView.findViewById(R.id.linearlayout_content);

        setWideBroadcast(contentView, mainNews.themeBroadcasts.get(0));

        for (int i = 1; i < mainNews.themeBroadcasts.size(); i++) {
            setPagerBroadcast(contentView, mainNews.themeBroadcasts.get(i));
        }
    }

    private void setWideBroadcast(ViewGroup contentView, ThemeBroadcasts themeBroadcasts) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.main_news_item_wide, contentView, false);

        final Broadcast broadcast = themeBroadcasts.broadcasts.get(0);

        final SizingImageView coverImageView = (SizingImageView) view.findViewById(R.id.imageview_broadcast_cover);
        coverImageView.setOnSizeChangedListener(new OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int width, int height) {
                Picasso.with(getActivity()).load(ImageUrlHelper.create(broadcast.coverImage, width, height)).fit().into(coverImageView);
            }
        });

        TextView broadcastTitleTextView = (TextView) view.findViewById(R.id.textview_broadcast_title);
        broadcastTitleTextView.setText(broadcast.title);

        TextView themeTitleTextView = (TextView) view.findViewById(R.id.textview_theme_title);
        themeTitleTextView.setText(themeBroadcasts.theme.title);

        TextView commentCountTextView = (TextView) view.findViewById(R.id.textview_comment_count);
        String commentCount = getString(R.string.comment) + ": " + broadcast.scoreboard.commentCount;
        commentCountTextView.setText(commentCount);

        TextView subscriberCountTextView = (TextView) view.findViewById(R.id.textview_subscriber_count);
        String subscriberCount = getString(R.string.subscription) + ": " + broadcast.scoreboard.subscriptionCount;
        subscriberCountTextView.setText(subscriberCount);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BroadcastActivity.class);
                intent.putExtra("broadcast_id", broadcast.id);
                startActivity(intent);
            }
        });

        contentView.addView(view);
    }

    private void setPagerBroadcast(ViewGroup contentView, final ThemeBroadcasts themeBroadcasts) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());

        View view = inflater.inflate(R.layout.main_news_item_pager, contentView, false);
        TextView themeTitleTextView = (TextView) view.findViewById(R.id.textivew_theme_title);
        themeTitleTextView.setText(themeBroadcasts.theme.title);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);

        BlockPagerAdapter adapter = new BlockPagerAdapter(themeBroadcasts.broadcasts);
        recyclerView.setAdapter(adapter);

        contentView.addView(view);
    }

    public class BlockPagerAdapter extends RecyclerView.Adapter<BlockPagerAdapter.ViewHolder> implements View.OnClickListener {

        private final ArrayList<Broadcast> mBroadcast;

        public BlockPagerAdapter(ArrayList<Broadcast> broadcasts) {
            mBroadcast = broadcasts;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.main_news_item_pager_block, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            Broadcast broadcast = mBroadcast.get(position);

            Picasso.with(getActivity()).load(ImageUrlHelper.create(broadcast.iconImage, 500, 500)).fit().into(viewHolder.iconImageView);

            viewHolder.broadcastTitleTextView.setText(broadcast.title);

            OnBlockClickListener listener = new OnBlockClickListener(mBroadcast.get(position).id);
            viewHolder.containter.setOnClickListener(listener);
        }

        @Override
        public int getItemCount() {
            return mBroadcast.size();
        }

        @Override
        public void onClick(View v) {

        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private FrameLayout containter;
            private ImageView iconImageView;
            private TextView broadcastTitleTextView;

            public ViewHolder(View view) {
                super(view);
                this.containter = (FrameLayout) view.findViewById(R.id.container);
                this.iconImageView = (ImageView) view.findViewById(R.id.imageview_icon);
                this.broadcastTitleTextView = (TextView) view.findViewById(R.id.textview_broadcast_title);
            }
        }
    }

    private class OnBlockClickListener implements View.OnClickListener {

        private final int mBroadcastId;

        private OnBlockClickListener(int broadcastId) {
            mBroadcastId = broadcastId;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), BroadcastActivity.class);
            intent.putExtra("broadcast_id", mBroadcastId);
            startActivity(intent);
        }
    }
}