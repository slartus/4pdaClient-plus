package org.softeg.slartus.forpdaplus.video;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.common.StringUtils;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.video.api.Quality;
import org.softeg.slartus.forpdaplus.video.api.VideoItem;
import org.softeg.slartus.forpdaplus.video.api.YouTubeAPI;
import org.softeg.slartus.forpdaplus.video.api.exceptions.ApiException;
import org.softeg.slartus.forpdaplus.video.api.exceptions.IdException;
import org.softeg.slartus.forpdaplus.video.api.exceptions.ListIdException;

public class PlayerActivity extends BaseFragmentActivity {

    VideoView mVideoView;
    String mVideoUrl;
    ProgressBar pb;
    int quality_ = 1;
    int qualiti_connect;
    private MenuFragment mFragment1;
    /**
     * Background task on which all of the interaction with YouTube is done
     */

    protected QueryFormatsYouTubeTask mQueryFormatsYouTubeTask;
    private String playedRequestUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        ScreenOrientation();
        mVideoUrl = getIntent().getStringExtra("_videoUrl");
        getSupportActionBar().setTitle(mVideoUrl);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // set the flag to keep the screen ON so that the video can play without the screen being turned off
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (isWiFi(this))
            quality_ = 2;

        qualiti_connect = quality_;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;
        }

        return true;
    }

    public static void showActivity(Activity activity, CharSequence youtubeUrl) {

        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra("_videoUrl", youtubeUrl);
        activity.startActivity(intent);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getVideoFormats();
    }

    protected void createActionMenu(VideoItem parseResult) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag(MenuFragment.ID);
        if (mFragment1 == null) {
            mFragment1 = MenuFragment.getInstance(parseResult);
            ft.add(mFragment1, MenuFragment.ID);
        }
        ft.commit();

    }

    public static Boolean isYoutube(String url) {
        return PatternExtensions.compile("youtube.com/(?:watch|v|e|embed)|youtu.be").matcher(url).find();
    }

    public static void showYoutubeChoiceDialog(final Activity activity, final CharSequence youtubeUrl) {
        int savedSelectedPlayer = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(App.getContext())
                .getString("news.videoplayer", "-1"));
        if (savedSelectedPlayer != -1) {
            startVideo(savedSelectedPlayer, activity, youtubeUrl);
            return;
        }
        CharSequence[] items = {"Плеер клиента", "Проигрыватель системы"};
        final int[] selected_player = {0};
        new MaterialDialog.Builder(activity)
                .title("Выберите проигрыватель")
                .items(items)
                .itemsCallbackSingleChoice(selected_player[0], new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int i, CharSequence text) {
                        selected_player[0] = i;
                        return true; // allow selection
                    }
                })
                .alwaysCallSingleChoiceCallback()
                .positiveText("Всегда")
                .neutralText("Только сейчас")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        PreferenceManager.getDefaultSharedPreferences(App.getContext())
                                .edit()
                                .putString("news.videoplayer", Integer.toString(selected_player[0]))
                                .commit();

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

                PlayerActivity.showActivity(activity, youtubeUrl);
                break;
            case 1:

                IntentActivity.showInDefaultBrowser(activity, youtubeUrl.toString());
                break;
        }
    }


    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ScreenOrientation();

    }

    private void getVideoFormats() {
        mQueryFormatsYouTubeTask = new QueryFormatsYouTubeTask();
        mQueryFormatsYouTubeTask.execute();
    }

    private void ScreenOrientation() {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            getSupportActionBar().show();
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        }
    }

    private void setOrientation(int orientation) {
        setRequestedOrientation(orientation);
    }

    private int mSeekTo = 0;

    protected void initView() {
        setContentView(R.layout.player);

        mVideoView = (VideoView) findViewById(R.id.videoView);
        pb = (ProgressBar) findViewById(R.id.progressBar);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer pMp) {
                if (mSeekTo > 0)
                    mVideoView.seekTo(mSeekTo);
                if (mQueryFormatsYouTubeTask != null && mQueryFormatsYouTubeTask.isCancelled())
                    return;
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    public void run() {
                        PlayerActivity.this.pb.setVisibility(View.GONE);
                    }
                });
            }
        });

        // add listeners for finish of video
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer pMp) {
                if (mQueryFormatsYouTubeTask != null && mQueryFormatsYouTubeTask.isCancelled())
                    return;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mVideoView != null)
            mVideoView.stopPlayback();

        // clear the flag that keeps the screen ON
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected static boolean isWiFi(Context context) {
        boolean haveConnectedWifi = false;
        //	boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    return true;
        }

        return false;
    }

    public void showFormatsDialog(final VideoItem videoItem) {
        CharSequence[] titles = new CharSequence[videoItem.getQualities().size()];
        int i = 0;
        for (Quality format : videoItem.getQualities()) {
            titles[i++] = format.getTitle();
        }
        new MaterialDialog.Builder(getContext())
                .title("Качество видео ")
                .items(titles)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int i, CharSequence text) {
                        String path = videoItem.getFilePath(videoItem.getQualities().get(i).getFileName());
                        //createActionMenu(apiId, parseResult);
                        playVideo(videoItem, path);
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
    }

    private boolean tryPlayInYoutubePlayer() {
//        String version = YouTubeIntents.getInstalledYouTubeVersionName(this);
//        if (version != null) {
//            Toast.makeText(getContext(), "Проигрывание плеером youtube", Toast.LENGTH_SHORT).show();
//            Intent intent = YouTubeIntents.createPlayVideoIntentWithOptions(this, YouTubeAPI.getYoutubeId(mVideoId).toString(), true, false);
//            startActivity(intent);
//            finish();
//            return true;
//        }
        return false;
    }

    private void playVideo(VideoItem parseResult, CharSequence url) {
        playVideo(parseResult, Uri.parse(url.toString()));
    }

    private void playVideo(VideoItem parseResult, Uri pResult) {
        try {
            createActionMenu(parseResult);
            mSeekTo = mVideoView.getCurrentPosition();
            mVideoView.setVideoURI(pResult);

            mVideoView.setMediaController(new MediaController(this));

            mVideoView.setKeepScreenOn(true);

            mVideoView.requestFocus();

            mVideoView.start();
        } catch (Throwable ex) {
            AppLog.toastE(getContext(), ex);
        }
    }

    public String getPlayedRequestUrl() {
        return playedRequestUrl;
    }

    /**
     * Task to figure out details by calling out to YouTube GData API.
     */
    private class QueryFormatsYouTubeTask extends AsyncTask<Void, Void, VideoItem> {

        public QueryFormatsYouTubeTask() {

        }

        private Throwable mEx;

        @Override
        protected VideoItem doInBackground(Void... pParams) {


            if (isCancelled())
                return null;

            try {
                playedRequestUrl = mVideoUrl;

                VideoItem videoItem = new VideoItem();
                videoItem.setTitle(mVideoUrl);
                videoItem.setUrl(mVideoUrl);

                YouTubeAPI.parse(videoItem, YouTubeAPI.getYoutubeId(playedRequestUrl).toString());

                if (isCancelled())
                    return null;
                return videoItem;
            } catch (Throwable e) {
                mEx = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(VideoItem pResult) {
            super.onPostExecute(pResult);

            if (isCancelled())
                return;

            if (mEx != null) {
                if (mEx.getClass() == ApiException.class ||
                        mEx.getClass() == IdException.class ||
                        mEx.getClass() == ListIdException.class) {
                    if (!tryPlayInYoutubePlayer())
                        AppLog.e(getContext(), mEx);

                } else {
                    AppLog.e(getContext(), mEx);
                }

                return;
            }

            getSupportActionBar().setTitle(pResult.getTitle());

            try {

                getSupportActionBar().setTitle(pResult.getTitle());

                if (pResult.getQualities().size() > 1) {
                    showFormatsDialog(pResult);
                    return;
                }
                if (pResult.getQualities().size() == 1) {
                    String path = pResult.getDefaultVideoUrl().toString();
                    playVideo(pResult, path);
                    return;
                }
                playVideo(pResult, pResult.getUrl());


            } catch (Throwable ex) {
                AppLog.toastE(getContext(), ex);
            }
        }
    }


    public static final class MenuFragment extends Fragment {
        public static final String ID = "VideoViewPlayerFragment.MenuFragment";

        private VideoItem parseResult;

        public static MenuFragment getInstance(VideoItem videoItem) {
            MenuFragment menuFragment = new MenuFragment();
            Bundle args = new Bundle();
            args.putParcelable("VideoItem", videoItem);
            menuFragment.setArguments(args);
            return menuFragment;
        }

        public PlayerActivity getMainActivity() {
            return (PlayerActivity) getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);
            Bundle args = getArguments();
            if (args != null && args.containsKey("VideoItem"))
                parseResult = args.getParcelable("VideoItem");
            args = savedInstanceState;
            if (args != null && args.containsKey("VideoItem"))
                parseResult = args.getParcelable("VideoItem");
        }

        @Override
        public void onSaveInstanceState(android.os.Bundle outState) {
            outState.putParcelable("VideoItem",parseResult);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            SubMenu subMenu;
            if (parseResult!=null&&parseResult.getQualities().size() > 1) {

                subMenu = menu.addSubMenu("Качество");

                subMenu.getItem().setIcon(R.drawable.ic_menu_view);
                subMenu.getItem().setTitle("Качество");


                for (final Quality format : parseResult.getQualities()) {
                    subMenu.add(format.getTitle()).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            String path = parseResult.getFilePath(format.getFileName());

                            getMainActivity().playVideo(parseResult, path);
                            return true;
                        }
                    });
                }
                subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            subMenu = menu.addSubMenu("Ссылка...").setIcon(R.drawable.ic_menu_share);

            addUrlMenu(getActivity(), subMenu, getMainActivity().getPlayedRequestUrl());
            subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        public void addUrlMenu(final Context context, Menu menu, final String url) {
            menu.add("Открыть в...").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ExtUrl.showInBrowser(context, url);
                    return true;
                }
            });

            menu.add("Поделиться ссылкой").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ExtUrl.shareIt(context, url, url, url);
                    return true;
                }
            });

            menu.add("Скопировать ссылку").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    StringUtils.copyToClipboard(context, url);
                    return true;
                }
            });
        }

    }

}
