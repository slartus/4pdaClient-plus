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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeIntents;

import org.softeg.slartus.forpdacommon.PatternExtensions;
import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.common.StringUtils;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.video.api.ParseResult;
import org.softeg.slartus.forpdaplus.video.api.VideoFormat;
import org.softeg.slartus.forpdaplus.video.api.YouTubeAPI;
import org.softeg.slartus.forpdaplus.video.api.exceptions.ApiException;
import org.softeg.slartus.forpdaplus.video.api.exceptions.IdException;
import org.softeg.slartus.forpdaplus.video.api.exceptions.ListIdException;

public class PlayerActivity extends BaseFragmentActivity {

    VideoView mVideoView;
    String mVideoId;
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
        mVideoId = getIntent().getStringExtra("_videoUrl");
        getSupportActionBar().setTitle(mVideoId);
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

    protected void createActionMenu(ParseResult parseResult) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag(MenuFragment.ID);
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment(parseResult);
            ft.add(mFragment1, MenuFragment.ID);
        }
        ft.commit();

    }

    public static Boolean isYoutube(String url) {
        return PatternExtensions.compile("youtube.com/(?:watch|v|e|embed)|youtu.be").matcher(url).find();
    }

    public static void showYoutubeChoiceDialog(final Activity activity, final CharSequence youtubeUrl) {
        int savedSelectedPlayer = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(MyApp.getContext())
                .getString("news.videoplayer", "-1"));
        if (savedSelectedPlayer != -1) {
            startVideo(savedSelectedPlayer, activity, youtubeUrl);
            return;
        }
        CharSequence[] items = {"Плеер клиента", "Проигрыватель системы"};
        final int[] selected_player = {0};
        new AlertDialogBuilder(activity)
                .setTitle("Выберите проигрыватель")
                .setSingleChoiceItems(items, selected_player[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selected_player[0] = i;

                    }
                })
                .setPositiveButton("Всегда",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int whichButton) {
                                dialogInterface.dismiss();

                                PreferenceManager.getDefaultSharedPreferences(MyApp.getContext())
                                        .edit()
                                        .putString("news.videoplayer", Integer.toString(selected_player[0]))
                                        .commit();

                                startVideo(selected_player[0], activity, youtubeUrl);
                            }
                        }
                )
                .setNeutralButton("Только сейчас",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int whichButton) {
                                dialogInterface.dismiss();

                                startVideo(selected_player[0], activity, youtubeUrl);
                            }
                        }
                )
                .create().show();
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

    public void showFormatsDialog(final ParseResult parseResult) {
        CharSequence[] titles = new CharSequence[parseResult.getFormats().size()];
        int i = 0;
        for (VideoFormat format : parseResult.getFormats()) {
            titles[i++] = format.getTitle();
        }
        new AlertDialogBuilder(getContext())
                .setTitle("Качество видео ")
                .setSingleChoiceItems(titles, -1, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        //createActionMenu(apiId, parseResult);
                        playVideo(parseResult, parseResult.getFormats().get(i).getUrl());
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .create()
                .show();
    }

    private boolean tryPlayInYoutubePlayer() {
        String version = YouTubeIntents.getInstalledYouTubeVersionName(this);
        if (version != null) {
            Toast.makeText(getContext(), "Проигрывание плеером youtube", Toast.LENGTH_SHORT).show();
            Intent intent = YouTubeIntents.createPlayVideoIntentWithOptions(this, YouTubeAPI.getYoutubeId(mVideoId).toString(), true, false);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void playVideo(ParseResult parseResult, CharSequence url) {
        playVideo(parseResult, Uri.parse(url.toString()));
    }

    private void playVideo(ParseResult parseResult, Uri pResult) {
        try {
            createActionMenu(parseResult);
            mSeekTo = mVideoView.getCurrentPosition();
            mVideoView.setVideoURI(pResult);

            mVideoView.setMediaController(new MediaController(this));

            mVideoView.setKeepScreenOn(true);

            mVideoView.requestFocus();

            mVideoView.start();
        } catch (Throwable ex) {
            Log.e(getContext(), String.format("Ошибка воспроизведения видео(%s)!", pResult), ex);
        }
    }

    public String getPlayedRequestUrl() {
        return playedRequestUrl;
    }

    /**
     * Task to figure out details by calling out to YouTube GData API.
     */
    private class QueryFormatsYouTubeTask extends AsyncTask<Void, Void, ParseResult> {

        public QueryFormatsYouTubeTask() {

        }

        private Throwable mEx;

        @Override
        protected ParseResult doInBackground(Void... pParams) {


            if (isCancelled())
                return null;

            try {
                playedRequestUrl = mVideoId;
                ParseResult info = YouTubeAPI.getInfo(mVideoId,
                        !TextUtils.isEmpty(YouTubeIntents.getInstalledYouTubeVersionName(getContext())));

                if (isCancelled())
                    return null;
                return info;
            } catch (Throwable e) {
                mEx = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(ParseResult pResult) {
            super.onPostExecute(pResult);

            if (isCancelled())
                return;

            if (mEx != null) {
                if (mEx.getClass() == ApiException.class ||
                        mEx.getClass() == IdException.class ||
                        mEx.getClass() == ListIdException.class) {
                    if (!tryPlayInYoutubePlayer())
                        org.softeg.slartus.forpdaplus.common.Log.e(getContext(), mEx);

                } else {
                    org.softeg.slartus.forpdaplus.common.Log.e(getContext(), mEx);
                }

                return;
            }

            getSupportActionBar().setTitle(pResult.getTitle());

            try {

                getSupportActionBar().setTitle(pResult.getTitle());

                if (pResult.getFormats().size() > 1) {
                    showFormatsDialog(pResult);
                    return;
                }
                if (pResult.getFormats().size() == 1) {
                    playVideo(pResult, pResult.getFormats().get(0).getUrl());
                    return;
                }
                playVideo(pResult, pResult.getVideoUrl());


            } catch (Throwable ex) {
                Log.e(getContext(), String.format("Ошибка воспроизведения видео(%s)!", mVideoId), ex);
            }
        }
    }


    public static final class MenuFragment extends Fragment {
        public static final String ID = "VideoViewPlayerFragment.MenuFragment";

        private ParseResult parseResult;

        public MenuFragment(ParseResult parseResult) {
            super();

            this.parseResult = parseResult;
        }

        public PlayerActivity getMainActivity() {
            return (PlayerActivity) getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            SubMenu subMenu;
            if (parseResult.getFormats().size() > 1) {

                subMenu = menu.addSubMenu("Качество");

                subMenu.getItem().setIcon(R.drawable.ic_menu_view);
                subMenu.getItem().setTitle("Качество");


                for (final VideoFormat format : parseResult.getFormats()) {
                    subMenu.add(format.getTitle()).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            getMainActivity().playVideo(parseResult, format.getUrl());
                            return true;
                        }
                    });
                }
                subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
            subMenu = menu.addSubMenu("Ссылка..").setIcon(R.drawable.ic_menu_share);

            addUrlMenu(getActivity(), subMenu, getMainActivity().getPlayedRequestUrl());
            subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        public void addUrlMenu(final Context context, Menu menu, final String url) {
            menu.add("Открыть в..").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
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
