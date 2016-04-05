package com.kindabear.radiople.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kindabear.radiople.R;
import com.kindabear.radiople.RadiopleApplication;
import com.kindabear.radiople.network.ApiUrlBuilder;
import com.kindabear.radiople.network.CommonResponse;
import com.kindabear.radiople.network.CommonResponseListener;
import com.kindabear.radiople.network.GsonRequest;
import com.kindabear.radiople.network.ImageUrlHelper;
import com.kindabear.radiople.network.UrlBuilder;
import com.kindabear.radiople.response.EmptyJson;
import com.kindabear.radiople.response.Episode;
import com.kindabear.radiople.service.PlayerListener;
import com.kindabear.radiople.util.Constants;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private final static String TAG = "PlayerService";

    private final static String HEADER_ACCEPT = "audio/mpeg,audio/mp3,*/*";

    private final static String HEADER_USER_AGENT_FORMAT = "radiople/%s android/%s (%s/%s)";

    private final static int NOTIFICATION_ID = 1197980;

    private static final int SEEK_TIMER_DELAY = 200;

    private final IBinder mBinder = new LocalBinder();

    private RequestQueue mRequestQueue = null;

    private Episode mEpisode = null;

    private PlayerListener mPlayerListener = null;

    private MediaPlayer mMediaPlayer = null;

    private boolean mIsPrepared = false;
    private boolean mIsPreparing = false;

    private final static Map<String, String> HEADER = new HashMap<String, String>();

    private NotificationCompat.Builder mNotificationBuilder = null;
    private PendingIntent mResumePendingIntent = null;
    private PendingIntent mPausePendingIntent = null;
    private PendingIntent mStopPendingIntent = null;
    private PendingIntent mFastForwardPendingIntent = null;
    private PendingIntent mFastRewindPendingIntent = null;

    private ControlReceiver mControlReceiver = null;


    private Target mPicassoTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (mNotificationBuilder != null) {
                mNotificationBuilder.setLargeIcon(bitmap);
                startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private Handler mSeekbarHandler = new Handler();

    private Runnable mSeekbarTimer = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && mPlayerListener != null) {
                mPlayerListener.onPositionUpdated(mMediaPlayer.getCurrentPosition());
            }
            mSeekbarHandler.postDelayed(this, SEEK_TIMER_DELAY);
        }
    };

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initPendingIntents();

        initHeader();

        mRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.i(TAG, ">>>>>>> null");
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action == null) {
            Log.i(TAG, ">>>> action null");
            return START_STICKY;
        }

        if (action.equals(Action.READY)) {
            Log.i(TAG, ">>>>> ready");
            ready(intent.getIntExtra("episode_id", 0));
            registerInterceptor();
        } else if (action.equals(Action.PAUSE)) {
            pause();
        } else if (action.equals(Action.STOP)) {
            stop(true);
        } else if (action.equals(Action.FAST_FORWARD)) {
            seekTo(getCurrentPosition() + (20 * 1000));
        } else if (action.equals(Action.FAST_REWIND)) {
            seekTo(getCurrentPosition() - (10 * 1000));
        } else {
            start();
        }

        return START_STICKY;
    }

    private void registerInterceptor() {
        mControlReceiver = new ControlReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mControlReceiver, filter);
    }

    private void unregisterInterceptor() {
        unregisterReceiver(mControlReceiver);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        reportHistory();
        Log.i(TAG, ">>>>>>>>>>>>>>>> onCompletion");
        if (mPlayerListener != null) {
            mPlayerListener.onComplete();
        } else {
            ready(mEpisode.id, Action.READY_NEXT);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>on error");
        if (mPlayerListener != null) {
            mPlayerListener.onError(getString(R.string.error_in_audio_playing));
        } else {
            stop(true);
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPreparing = false;
        mIsPrepared = true;

        if (mPlayerListener != null) {
            mPlayerListener.onPrepared(mEpisode, mp);
        } else {
            start();
        }
    }

    private void startSeekTimer() {
        mSeekbarHandler.postDelayed(mSeekbarTimer, SEEK_TIMER_DELAY);
    }

    private void stopSeekTimer() {
        mSeekbarHandler.removeCallbacks(mSeekbarTimer);
    }

    public void setPlayerListener(PlayerListener listener) {
        mPlayerListener = listener;
    }

    public void initHeader() {
        String userAgent = String.format(HEADER_USER_AGENT_FORMAT, Constants.getAppVersion(), Constants.getOsVersion(), Constants.getModel(), Constants.getProvider(this));
        HEADER.put("User-Agent", userAgent);
        HEADER.put("Accept", HEADER_ACCEPT);
    }

    public void seekTo(int progress) {
        if (!mIsPreparing) {
            if (progress == getDuration()) {
                progress -= 1;
            }
            mMediaPlayer.seekTo(progress);
            Log.i(TAG, ">>>>>>>>>> seekTo  : " + progress + " / " + getDuration());
        }
    }

    public int getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void start() {
        if (!isControllable()) {
            return;
        }

        startSeekTimer();

        mMediaPlayer.start();

        updateNotification();

        if (mPlayerListener != null) {
            mPlayerListener.onStarted();
        }
    }

    public void pause() {
        if (!isControllable()) {
            return;
        }

        stopSeekTimer();

        mMediaPlayer.pause();

        updateNotification();

        if (mPlayerListener != null) {
            mPlayerListener.onPaused();
        }
    }

    public void ready(int episodeId) {
        ready(episodeId, Action.READY);
    }

    public void ready(String action) {
        if (action.equals(Action.READY_NEXT)) {
            ready(mEpisode.id, Action.READY_NEXT);
        } else {
            ready(mEpisode.id, Action.READY_PREV);
        }
    }

    public void ready(int episodeId, String action) {
        if (mIsPreparing) {
            return;
        }

        mIsPreparing = true;
        mIsPrepared = false;

        updateNotification();

        String url;

        UrlBuilder builder = new ApiUrlBuilder().addPath("v1", "episode", String.valueOf(episodeId));
        if (action.equals(Action.READY)) {
            url = builder.toString();
        } else if (action.equals(Action.READY_NEXT)) {
            url = builder.addPath("next").toString();
        } else {
            url = builder.addPath("prev").toString();
        }

        GsonRequest<Episode> request = new GsonRequest<Episode>(this, Request.Method.GET, url, Episode.class,
                new Response.Listener<Episode>() {
                    @Override
                    public void onResponse(Episode response) {
                        mEpisode = response;
                        preparePlayer(response.storage.url);
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {
                        mIsPreparing = false;
                        if (mPlayerListener != null) {
                            mPlayerListener.onError(common.displayMessage);
                        } else {
                            stop(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mIsPreparing = false;
                        if (mPlayerListener != null) {
                            mPlayerListener.onError(getString(R.string.network_error_message));
                        } else {
                            stop(true);
                        }
                    }
                });

        mRequestQueue.add(request);
    }

    private void preparePlayer(String url) {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mMediaPlayer.setDataSource(this, Uri.parse(url), HEADER);
        } catch (IOException e) {
            e.printStackTrace();
            if (mPlayerListener != null) {
                mPlayerListener.onError(getString(R.string.error_in_audio_playing));
            } else {
                stop(true);
            }
        }

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        mMediaPlayer.prepareAsync();
    }

    public void stop() {
        stop(false);
    }

    public void stop(boolean killSignal) {
        reportHistory();

        stopSeekTimer();

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mPlayerListener != null) {
            mPlayerListener.onStopped();
        }

        stopForeground(true);

        unregisterInterceptor();

        if (killSignal) {
            stopSelf();
        }
    }

    private void initPendingIntents() {
        Intent resumeIntent = new Intent(this, PlayerService.class);
        resumeIntent.setAction(Action.PLAY);
        mResumePendingIntent = PendingIntent.getService(this, 0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, PlayerService.class);
        pauseIntent.setAction(Action.PAUSE);
        mPausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, PlayerService.class);
        stopIntent.setAction(Action.STOP);
        mStopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent fastRewindIntent = new Intent(this, PlayerService.class);
        fastRewindIntent.setAction(Action.FAST_REWIND);
        mFastRewindPendingIntent = PendingIntent.getService(this, 0, fastRewindIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent fastForwardIntent = new Intent(this, PlayerService.class);
        fastForwardIntent.setAction(Action.FAST_FORWARD);
        mFastForwardPendingIntent = PendingIntent.getService(this, 0, fastForwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void updateNotification() {
        mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mNotificationBuilder.setShowWhen(false);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setPriority(Notification.PRIORITY_HIGH);

        if (mIsPrepared) {
            mNotificationBuilder.setStyle(new NotificationCompat.MediaStyle(mNotificationBuilder));

            mNotificationBuilder.setContentTitle(mEpisode.title);
            mNotificationBuilder.setContentText(mEpisode.broadcast.title);

            mNotificationBuilder.addAction(R.drawable.ic_av_fast_rewind, "", mFastRewindPendingIntent);

            if (isPlaying()) {
                mNotificationBuilder.addAction(R.drawable.ic_av_pause, "", mPausePendingIntent);
            } else {
                mNotificationBuilder.addAction(R.drawable.ic_av_play_arrow, "", mResumePendingIntent);
            }

            mNotificationBuilder.addAction(R.drawable.ic_av_fast_forward, "", mFastForwardPendingIntent);
            mNotificationBuilder.addAction(R.drawable.ic_av_stop, "", mStopPendingIntent);

            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("episode_id", mEpisode.id);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            mNotificationBuilder.setContentIntent(pendingIntent);

            Picasso.with(getApplicationContext()).load(ImageUrlHelper.create(mEpisode.broadcast.iconImage, 500, 500)).placeholder(R.mipmap.ic_launcher).into(mPicassoTarget);
        } else {
            mNotificationBuilder.setContentTitle(getString(R.string.app_name));
            mNotificationBuilder.setContentText(getString(R.string.player_is_preparing));
        }

        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    public boolean isControllable() {
        return !mIsPreparing && mIsPrepared;
    }

    public boolean isPreparing() {
        return mIsPreparing;
    }

    public boolean isPrepared() {
        return mIsPrepared;
    }

    public Episode getEpisode() {
        return mEpisode;
    }

    private void reportHistory() {
        if (!((RadiopleApplication) getApplicationContext()).getUserService().exists()) {
            return;
        }

        String url = new ApiUrlBuilder().addPath("v1", "episode", String.valueOf(mEpisode.id), "history").toString();

        GsonRequest<EmptyJson> request = new GsonRequest<EmptyJson>(this, Request.Method.PUT, url, EmptyJson.class,
                new Response.Listener<EmptyJson>() {
                    @Override
                    public void onResponse(EmptyJson response) {
                    }
                },
                new CommonResponseListener() {
                    @Override
                    public void onCommonResponse(int statusCode, CommonResponse common) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("position", String.valueOf(getCurrentPosition()));
        request.setParams(params);

        mRequestQueue.add(request);
    }

    public static class Action {
        public final static String READY = "ready";
        public final static String PAUSE = "pause";
        public final static String STOP = "stop";
        public final static String PLAY = "play";
        public final static String FAST_REWIND = "fast_rewind";
        public final static String FAST_FORWARD = "fast_forward";
        public final static String READY_NEXT = "ready_next";
        public final static String READY_PREV = "ready_prev";
    }

    public class ControlReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_HEADSET_PLUG)) {
                Log.i(TAG, ">>>>> good headset");
                pause();
                return;
            }

            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.i(TAG, ">>>>> phone ring");
                pause();
            }
        }
    }
}