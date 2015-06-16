package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShortUserInfo {
    private Activity mActivity;
    private SharedPreferences prefs;
    private CircleImageView imgAvatar;
    private ImageView infoRefresh;
    private ImageView userBackground;
    private TextView userNick;
    private TextView qmsMessages;
    private TextView loginButton;
    private TextView userRep;
    private RelativeLayout textWrapper;
    private Handler mHandler = new Handler();

    public ShortUserInfo(Activity activity) {
        prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());

        mActivity = activity;
        userNick = (TextView) findViewById(R.id.userNick);
        qmsMessages = (TextView) findViewById(R.id.qmsMessages);
        loginButton = (TextView) findViewById(R.id.loginButton);
        userRep = (TextView) findViewById(R.id.userRep);
        textWrapper = (RelativeLayout) findViewById(R.id.textWrapper);
        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatara);
        infoRefresh = (ImageView) findViewById(R.id.infoRefresh);
        userBackground = (ImageView) findViewById(R.id.userBackground);

        if(prefs.getBoolean("isUserBackground",false)){
            File imgFile = new File(prefs.getString("userBackground",""));
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                userBackground.setImageBitmap(myBitmap);
            }else {
                userBackground.setImageResource(R.drawable.user_bacground);
            }
        }

        if(isOnline()){
            if(Client.getInstance().getLogined()) {
                new updateAsyncTask().execute();
                imgAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProfileWebViewActivity.startActivity(getContext(), Client.getInstance().UserId, Client.getInstance().getUser());
                    }
                });

                Client.getInstance().addOnUserChangedListener(new Client.OnUserChangedListener() {
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
                Client.getInstance().addOnMailListener(new Client.OnMailListener() {
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
                if(isOnline() & Client.getInstance().getLogined()){
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
        int qmsCount = Client.getInstance().getQmsCount();
        if (qmsCount != 0) {
            qmsMessages.setText("Новые сообщения QMS: " + qmsCount);
        } else {
            qmsMessages.setText("Нет новых сообщений QMS");
        }
    }

    private class updateAsyncTask extends AsyncTask<String, Void, Void> {
        String[] strings;

        @Override
        protected Void doInBackground(String... urls) {
            try {
                strings = getUserInfo(Client.getInstance(), Client.getInstance().UserId);
                if ((strings[0] == null) & (strings[1] == null)) {
                    strings[1] = prefs.getString("shortUserInfoRep", "-100500");
                    strings[0] = "http://s.4pda.to/img/qms/logo.png";
                }
            } catch (IOException e) {
                AppLog.e(getContext(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (Client.getInstance().getLogined()) {
                loginButton.setVisibility(View.GONE);
                textWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListFragmentActivity.showListFragment(getContext(), QmsContactsBrickInfo.NAME, null);
                    }
                });
                userNick.setText(Client.getInstance().getUser());
                userRep.setText("Репутация: " + strings[1]);
                prefs.edit().putString("shortUserInfoRep", strings[1]).apply();
                refreshQms();
                new Picasso.Builder(App.getInstance())
                        .downloader(new Downloader() {
                            @Override
                            public Response load(Uri uri, int networkPolicy) throws IOException {
                                HttpResponse httpResponse = new HttpHelper().getDownloadResponse(uri.toString(), 0);
                                return new Response(httpResponse.getEntity().getContent(), false, httpResponse.getEntity().getContentLength());
                            }
                            @Override
                            public void shutdown() {
                            }
                        })
                        .build()
                        .load(strings[0])
                        .error(R.drawable.no_image)
                        .into(imgAvatar);
                prefs.edit().putBoolean("isLoadShortUserInfo", true).apply();
                prefs.edit().putString("shortAvatarUrl", strings[0]).apply();
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
    public static String[] getUserInfo(IHttpClient httpClient, CharSequence userID) throws IOException {
        String page = httpClient.performGet("http://4pda.ru/forum/index.php?showuser=" + userID);
        Document doc = Jsoup.parse(page);
        org.jsoup.nodes.Element element = doc.select("div#main").first();
        return new String[]{element.select("div.user-box > div.photo > img").first().absUrl("src"),
                element.select("div.statistic-box span[id*=\"ajaxrep\"]").first().text()};
    }
}