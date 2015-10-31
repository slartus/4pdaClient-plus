package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.ProfileFragment;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShortUserInfo {
    public Activity mActivity;
    public SharedPreferences prefs;
    public CircleImageView imgAvatar;
    public ImageView imgAvatarSquare, infoRefresh,userBackground;
    public TextView userNick, qmsMessages, loginButton, userRep;
    public RelativeLayout textWrapper;
    public Handler mHandler = new Handler();
    public Client client;
    public boolean isSquare;

    public ShortUserInfo(Activity activity) {
        prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        client = Client.getInstance();
        mActivity = activity;
        userNick = (TextView) findViewById(R.id.userNick);
        qmsMessages = (TextView) findViewById(R.id.qmsMessages);
        loginButton = (TextView) findViewById(R.id.loginButton);
        userRep = (TextView) findViewById(R.id.userRep);
        textWrapper = (RelativeLayout) findViewById(R.id.textWrapper);
        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatar);
        imgAvatarSquare = (ImageView) findViewById(R.id.imgAvatarSquare);
        infoRefresh = (ImageView) findViewById(R.id.infoRefresh);
        userBackground = (ImageView) findViewById(R.id.userBackground);
        isSquare = prefs.getBoolean("isSquareAvarars",false);


        if(prefs.getBoolean("isUserBackground",false)){
            File imgFile = new File(prefs.getString("userBackground",""));
            if(imgFile.exists()){
                Picasso.with(App.getContext()).load(imgFile).into(userBackground);
            }else {
                userBackground.setImageResource(R.drawable.user_background);
            }
        }

        if(isOnline()){
            if(client.getLogined()) {
                new updateAsyncTask().execute();
                if(isSquare){
                    imgAvatarSquare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProfileFragment.showProfile(client.UserId, client.getUser());
                        }
                    });
                }else {
                    imgAvatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProfileFragment.showProfile(client.UserId, client.getUser());
                        }
                    });
                }


                client.addOnUserChangedListener(new Client.OnUserChangedListener() {
                    @Override
                    public void onUserChanged(String user, Boolean success) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshQms();
                            }
                        });
                    }
                });
                client.addOnMailListener(new Client.OnMailListener() {
                    @Override
                    public void onMail(int count) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshQms();
                            }
                        });
                    }
                });
            }else {
                loginButton.setVisibility(View.VISIBLE);
                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LoginDialog.showDialog(getContext(), null);
                    }
                });
            }
        }else {
            loginButton.setText("Проверьте соединение");
        }
        infoRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline() & client.getLogined()){
                    new updateAsyncTask().execute();
                }
            }
        });
    }

    private Context getContext() {
        return mActivity;
    }

    private View findViewById(int id) {
        return mActivity.findViewById(id);
    }

    private void refreshQms() {
        int qmsCount = client.getQmsCount();
        if (qmsCount != 0) {
            qmsMessages.setText("Новые сообщения QMS: " + qmsCount);
        } else {
            qmsMessages.setText("Нет новых сообщений QMS");
        }
    }

    private class updateAsyncTask extends AsyncTask<String, Void, Void> {
        String avatarUrl = "";
        String reputation = "";

        @Override
        protected Void doInBackground(String... urls) {
            try {
                Document doc = Jsoup.parse(client.performGet("http://4pda.ru/forum/index.php?showuser=" + client.UserId));
                if(doc.select("div.user-box > div.photo > img").first()!=null){
                    avatarUrl = doc.select("div.user-box > div.photo > img").first().absUrl("src");
                }
                if(doc.select("div.statistic-box span[id*=\"ajaxrep\"]").first()!=null){
                    reputation = doc.select("div.statistic-box span[id*=\"ajaxrep\"]").first().text();
                }
            } catch (IOException e) {
                AppLog.e(getContext(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(avatarUrl.equals("")|reputation.equals("")){
                Toast.makeText(getContext(),"Не удалось загрузить данные",Toast.LENGTH_SHORT).show();
                loginButton.setText("Произошла ошибка");
                qmsMessages.setVisibility(View.GONE);
            }else if (client.getLogined()) {
                qmsMessages.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.GONE);
                textWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListFragmentActivity.showListFragment(getContext(), QmsContactsBrickInfo.NAME, null);
                    }
                });
                userNick.setText(client.getUser());
                userRep.setVisibility(View.VISIBLE);
                userRep.setText("Репутация: " + reputation);
                prefs.edit().putString("shortUserInfoRep", reputation).apply();
                refreshQms();
                if(isSquare){
                    Picasso.with(App.getContext()).load(avatarUrl).into(imgAvatarSquare);
                }else {
                    Picasso.with(App.getContext()).load(avatarUrl).into(imgAvatar);
                }
                //prefs.edit().putBoolean("isLoadShortUserInfo", true).apply();
                //prefs.edit().putString("shortAvatarUrl", avatarUrl).apply();
            }else {
                userRep.setVisibility(View.GONE);
                loginButton.setVisibility(View.VISIBLE);
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        }
        return false;
    }
}