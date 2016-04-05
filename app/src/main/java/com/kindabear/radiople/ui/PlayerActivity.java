package com.kindabear.radiople.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kindabear.radiople.R;
import com.kindabear.radiople.db.SettingService;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.response.Episode;
import com.kindabear.radiople.service.PlayerListener;
import com.kindabear.radiople.util.DateUtils;
import com.squareup.picasso.Picasso;

public class PlayerActivity extends BaseActivity implements PlayerListener {

    private final static String TAG = "PlayerActivity";

    private final static int VIBRATE_LENGTH = 50;

    private final static int HISTORY_PLAYING_OFFSET = 10000;

    private int mEpisodeId = 0;

    private PlayerService mPlayerService = null;

    private AppCompatSeekBar mSeekBar = null;

    private SettingService mSettingService = null;

    private ProgressBar mProgressBar = null;
    private ImageButton mControlButton = null;
    private ImageButton mLikeImageButton = null;
    private TextView mCurrentTimeTextView = null;
    private TextView mMaxTimeTextView = null;
    private TextView mEpisodeTitleTextView = null;
    private ImageView mCoverImageView = null;

    private boolean mIsTracking = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayerService = ((PlayerService.LocalBinder) service).getService();
            notifyPlayerServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        setStatusBarColor(Color.BLACK);

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("");

        // checking mobile network with setting
        mSettingService = new SettingService(this);
        if (!mSettingService.isAllowMobileNetwork()) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.not_allowed_mobile_network)
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
                return;
            }
        }

        Intent intent = getIntent();
        mEpisodeId = intent.getIntExtra("episode_id", 0);

        mEpisodeTitleTextView = (TextView) findViewById(R.id.textview_episode_title);

        mCoverImageView = (ImageView) findViewById(R.id.imageview_cover);

        mCurrentTimeTextView = (TextView) findViewById(R.id.textview_current_time);
        mMaxTimeTextView = (TextView) findViewById(R.id.textview_max_time);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);

        mControlButton = (ImageButton) findViewById(R.id.ibutton_control);

        mControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object isStoppable = mControlButton.getTag();
                if (isStoppable != null && (Boolean) isStoppable) {
                    mPlayerService.stop(true);
                    finish();
                } else {
                    if (mPlayerService.isPlaying()) {
                        mPlayerService.pause();
                    } else {
                        mPlayerService.start();
                    }
                }
            }
        });

        mControlButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VIBRATE_LENGTH);

                Object isStoppable = mControlButton.getTag();
                if (isStoppable != null && (Boolean) isStoppable) {
                    mControlButton.setBackgroundResource(R.drawable.btn_player_control);
                    if (mPlayerService.isPlaying()) {
                        mControlButton.setImageResource(R.drawable.ic_av_pause);
                    } else {
                        mControlButton.setImageResource(R.drawable.ic_av_play_arrow);
                    }
                    mControlButton.setTag(false);
                } else {
                    mControlButton.setBackgroundResource(R.drawable.btn_player_control_deletable);
                    mControlButton.setImageResource(R.drawable.ic_av_stop);
                    mControlButton.setTag(true);
                }
                return true;
            }
        });

        ImageButton nextButton = (ImageButton) findViewById(R.id.ibutton_skip_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerService.isControllable()) {
                    showLoading();
                    mPlayerService.ready(PlayerService.Action.READY_NEXT);
                }
            }
        });

        ImageButton prevButton = (ImageButton) findViewById(R.id.ibutton_skip_previous);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                mPlayerService.ready(PlayerService.Action.READY_PREV);
            }
        });

        ImageButton fastRewindButton = (ImageButton) findViewById(R.id.ibutton_fast_rewind);
        fastRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mPlayerService.getCurrentPosition() - (10 * 1000);
                mPlayerService.seekTo(position);

                if (!mPlayerService.isPlaying()) {
                    mSeekBar.setProgress(position);
                }

            }
        });

        ImageButton fastForwardButton = (ImageButton) findViewById(R.id.ibutton_fast_forward);
        fastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mPlayerService.getCurrentPosition() + (20 * 1000);
                mPlayerService.seekTo(position);

                if (!mPlayerService.isPlaying()) {
                    mSeekBar.setProgress(position);
                }
            }
        });

        mSeekBar = (AppCompatSeekBar) findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentTimeTextView.setText(DateUtils.toClock(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPlayerService.seekTo(seekBar.getProgress());
                mIsTracking = false;
            }
        });

        mSeekBar.setEnabled(false);

        showLoading();

        bindPlayerService();
    }

    private void bindPlayerService() {
        bindService(new Intent(PlayerActivity.this, PlayerService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    private void setEpisode(Episode episode) {
        mActionBar.setTitle(episode.broadcast.title);

        mEpisodeTitleTextView.setText(episode.title);
        mEpisodeTitleTextView.setSelected(true);

        String currentCoverUrl = (String) mCoverImageView.getTag();
        if (currentCoverUrl == null || !currentCoverUrl.equals(episode.broadcast.coverImage)) {
            mCoverImageView.setTag(episode.broadcast.coverImage);
            Picasso.with(this).load(ImageUrlHelper.create(episode.broadcast.coverImage, 1080, 1080)).fit().into(mCoverImageView);
        }
    }

    private void notifyPlayerServiceConnected() {
        // 플레이어가 준비 완료된 상태.
        if (mPlayerService.isControllable()) {
            // 같은 에피소드 요청인지 구분
            if (mPlayerService.getEpisode().id == mEpisodeId) {
                mPlayerService.setPlayerListener(this);
                continuePlaying();
                showToast("continue: playing");
            } else {
                mPlayerService.stop();
                mPlayerService.setPlayerListener(this);
                ready();
                showToast("new: difference episode");
            }
        } else if (mPlayerService.isPreparing()) {
            mPlayerService.setPlayerListener(this);
            showToast("continue: loading");
        } else {
            mPlayerService.setPlayerListener(this);
            ready();
            showToast("new: playing");
        }
    }

    private void continuePlaying() {
        setEpisode(mPlayerService.getEpisode());

        int duration = mPlayerService.getDuration();
        mSeekBar.setMax(duration);
        mMaxTimeTextView.setText(DateUtils.toClock(duration));

        if (mPlayerService.isPlaying()) {
            mControlButton.setImageResource(R.drawable.ic_av_pause);
        } else {
            mSeekBar.setProgress(mPlayerService.getCurrentPosition());
            mControlButton.setImageResource(R.drawable.ic_av_play_arrow);
        }
        mSeekBar.setEnabled(true);
        hideLoading();
    }

    @Override
    public void onBuffering() {
        showLoading();
    }

    @Override
    public void onPrepared(Episode episode, MediaPlayer mediaPlayer) {
        setEpisode(episode);

        showToast("onPrepared");
        int duration = mediaPlayer.getDuration();
        mSeekBar.setMax(duration);
        mMaxTimeTextView.setText(DateUtils.toClock(duration));

        mSeekBar.setEnabled(true);

        if (getUserService().exists() && episode.activity.position > 0) {
            long requestedPosition = getIntent().getLongExtra("position", 0);
            if (requestedPosition > 0) {
                mPlayerService.seekTo((int) requestedPosition);
                mPlayerService.start();
            } else {
                showHistoryExistsDialog(episode.activity.position);
            }
        } else {
            mPlayerService.start();
        }

        hideLoading();
    }

    private void showHistoryExistsDialog(final long position) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.play_continue)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPlayerService.seekTo((int) position - HISTORY_PLAYING_OFFSET);
                        mPlayerService.start();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPlayerService.start();
                    }
                }).show();
    }

    @Override
    public void onStarted() {
        mControlButton.setBackgroundResource(R.drawable.btn_player_control);
        mControlButton.setImageResource(R.drawable.ic_av_pause);
        mControlButton.setTag(false);
    }

    @Override
    public void onPaused() {
        mControlButton.setBackgroundResource(R.drawable.btn_player_control);
        mControlButton.setImageResource(R.drawable.ic_av_play_arrow);
        mControlButton.setTag(false);
    }

    @Override
    public void onComplete() {
        showLoading();
        mSeekBar.setEnabled(false);
        mPlayerService.ready(PlayerService.Action.READY_NEXT);
    }

    @Override
    public void onStopped() {
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    public void onError(String message) {
        Log.i(TAG, ">>>>>>>>>>>>>>> activity on error");
        hideLoading();
        showToast(message);
        mPlayerService.setPlayerListener(null);
        mPlayerService.stop(true);
        finish();
    }

    @Override
    public void onPositionUpdated(int position) {
        if (!mIsTracking) {
            mSeekBar.setProgress(position);
        }
    }

    @Override
    public void onEpisodePrepared(Episode episode) {

    }

    private void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void ready() {
        Intent intent = new Intent(this, PlayerService.class);
        intent.putExtra("episode_id", mEpisodeId);
        intent.setAction(PlayerService.Action.READY);
        startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mPlayerService != null) {
            mPlayerService.setPlayerListener(null);
            unbindService(mConnection);
        }
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