package org.softeg.slartus.forpdaplus.video.youtube;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by isanechek on 1/21/18
 */
public class YoutubeActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener,
        YouTubePlayer.OnFullscreenListener,
        YouTubePlayer.PlayerStateChangeListener {
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    public static final String EXTRA_VIDEO_ID = "video_id";
    public static final String YT_KEY = "AIzaSyDdEhmWHgdE1L-k7-_Dy6zaxNOY95twfR4"; // key - завязан на пакет и sh1

    private final static String reg = "(?:youtube(?:-nocookie)?\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})";
    private static String getVideoId(@NonNull String videoUrl) {
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);
        if (matcher.find())
            return matcher.group(1);
        return "";
    }
    public static void showYoutubeChoiceDialog(final Activity activity, final CharSequence youtubeUrl) {
        int savedSelectedPlayer = Integer.parseInt(App.getInstance().getPreferences()
                .getString("news.videoplayer", "-1"));
        if (savedSelectedPlayer != -1) {
            startVideo(savedSelectedPlayer, activity, youtubeUrl);
            return;
        }
        CharSequence[] items = {App.getContext().getString(R.string.client_player), App.getContext().getString(R.string.system_player)};
        final int[] selected_player = {0};
        new MaterialDialog.Builder(activity)
                .title(R.string.select_player)
                .items(items)
                .itemsCallbackSingleChoice(selected_player[0], (dialog, view, i, text) -> {
                    selected_player[0] = i;
                    return true; // allow selection
                })
                .alwaysCallSingleChoiceCallback()
                .positiveText(R.string.always)
                .neutralText(R.string.only_now)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        App.getInstance().getPreferences()
                                .edit()
                                .putString("news.videoplayer", Integer.toString(selected_player[0]))
                                .apply();

                        startVideo(selected_player[0], activity, youtubeUrl);
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        startVideo(selected_player[0], activity, youtubeUrl);
                    }
                })
                .show();
    }

    private static void startVideo(int selectedPlayer, Activity activity, CharSequence youtubeUrl) {
        switch (selectedPlayer) {
            case 0:
                startActivity(activity, youtubeUrl.toString());
                break;
            case 1:
                IntentActivity.showInDefaultBrowser(activity, youtubeUrl.toString());
                break;
        }
    }

    private static AudioManager audioManager;
    private String videoId;
    private YouTubePlayerView playerView;
    private YouTubePlayer player;

    public static void startActivity(Context context, String url) {
        String yId = getVideoId(url);
        Intent i = new Intent(context, YoutubeActivity.class);
        i.putExtra(EXTRA_VIDEO_ID, yId);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        videoId = getIntent().getStringExtra(EXTRA_VIDEO_ID);
        if (videoId.isEmpty()) {
            FrameLayout emptyContainer = new FrameLayout(this);
            emptyContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            emptyContainer.setBackgroundResource(android.R.color.black);
            TextView emptyText = new TextView(this);
            emptyText.setText("Извините, не могу обработать url.");
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setTextSize(18f);
            emptyText.setTextColor(Color.WHITE);
            emptyContainer.addView(emptyText);
            addContentView(emptyContainer, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            return;
        }

        playerView = new YouTubePlayerView(this);
        playerView.initialize(YT_KEY, this);
        playerView.setBackgroundResource(android.R.color.black);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        addContentView(playerView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onFullscreen(boolean b) {
        if (b) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        this.player = youTubePlayer;
        player.setOnFullscreenListener(this);
        player.setPlayerStateChangeListener(this);
        player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                | YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
                | YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);

        player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

        if (!b)
            player.loadVideo(videoId);

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(
                    "There was an error initializing the YouTubePlayer (%1$s)",
                    errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            playerView.initialize(YT_KEY, this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (player != null)
                player.setFullscreen(true);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && player != null) {
            player.setFullscreen(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            adjustMusicVolume(true, true);
            hide();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            adjustMusicVolume(false, true);
            hide();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onLoading() {}

    @Override
    public void onLoaded(String s) {}

    @Override
    public void onAdStarted() {}

    @Override
    public void onVideoStarted() {
        hide();
    }

    @Override
    public void onVideoEnded() {}

    @Override
    public void onError(YouTubePlayer.ErrorReason reason) {
        Log.e("onError", "onError : " + reason.name());
        if (YouTubePlayer.ErrorReason.NOT_PLAYABLE.equals(reason))
            startVideo(videoId);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {}

    private void hide() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void adjustMusicVolume(boolean up, boolean showInterface) {
        int direction = up ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER;
        int flag = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | (showInterface ? AudioManager.FLAG_SHOW_UI : 0);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, flag);
    }

    private void startVideo(@NonNull String videoId) {
        Uri video_uri = Uri.parse("http://youtu.be/" + videoId);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        if (list.isEmpty())
            intent = new Intent(Intent.ACTION_VIEW, video_uri);

        startActivity(intent);
    }
}
