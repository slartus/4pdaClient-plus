package org.softeg.slartus.forpdaplus;

import android.app.Activity;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.softeg.slartus.forpdaplus.classes.FastBlur;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShortUserInfo {
    public Activity mActivity;
    public SharedPreferences prefs;
    public CircleImageView imgAvatar;
    public ImageView imgAvatarSquare, infoRefresh, userBackground, openLink;
    public TextView userNick, qmsMessages, loginButton, userRep;
    public RelativeLayout textWrapper;
    public Handler mHandler = new Handler();
    public Client client;
    public boolean isSquare;
    public String avatarUrl = "";
    private View view;

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            blur(bitmap, userBackground, avatarUrl);
            prefs.edit().putString("userAvatarUrl",avatarUrl).apply();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    public ShortUserInfo(Activity activity, View view) {
        prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        client = Client.getInstance();
        mActivity = activity;
        this.view = view;
        userNick = (TextView) findViewById(R.id.userNick);
        qmsMessages = (TextView) findViewById(R.id.qmsMessages);
        loginButton = (TextView) findViewById(R.id.loginButton);
        userRep = (TextView) findViewById(R.id.userRep);
        textWrapper = (RelativeLayout) findViewById(R.id.textWrapper);
        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatar);
        imgAvatarSquare = (ImageView) findViewById(R.id.imgAvatarSquare);
        infoRefresh = (ImageView) findViewById(R.id.infoRefresh);
        openLink = (ImageView) findViewById(R.id.openLink);
        userBackground = (ImageView) findViewById(R.id.userBackground);
        isSquare = prefs.getBoolean("isSquareAvarars",false);

        openLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url;
                url = readFromClipboard(getContext());
                new MaterialDialog.Builder(getContext())
                        .title("Перейти по ссылке")
                        .input("Вставьте ссылку", isPdaLink(url) ? url : null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {

                            }
                        })
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                        .positiveText("Открыть")
                        .negativeText("Отмена")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                if(!IntentActivity.tryShowUrl((MainActivity)getContext(), ((MainActivity)getContext()).getHandler(), dialog.getInputEditText().getText()+"", false, false)){
                                    Toast.makeText(getContext(), "Не умею обрабатывать такие ссылки", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
            }
        });


        File imgFile = new File(prefs.getString("userInfoBg",""));
        if(imgFile.exists()){
            Picasso.with(activity).load(imgFile).into(userBackground);
        }
        client.checkLoginByCookies();
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
        return view.findViewById(id);
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
                        QmsContactsBrickInfo brickInfo = new QmsContactsBrickInfo();
                        MainActivity.addTab(brickInfo.getTitle(), brickInfo.getName(), brickInfo.createFragment());
                        //ListFragmentActivity.showListFragment(getContext(), QmsContactsBrickInfo.NAME, null);
                    }
                });
                userNick.setText(client.getUser());
                userRep.setVisibility(View.VISIBLE);
                userRep.setText("Репутация: " + reputation);

                refreshQms();
                //Log.e("kek", avatarUrl+" : "+prefs.getString("userAvatarUrl",""));
                if(prefs.getBoolean("isUserBackground", false)){
                    File imgFile = new File(prefs.getString("userInfoBg",""));
                    if(imgFile.exists()){
                        Picasso.with(getContext()).load(imgFile).into(userBackground);
                    }
                }else {
                    if(!avatarUrl.equals(prefs.getString("userAvatarUrl",""))|prefs.getString("userInfoBg","").equals("")){
                        //Log.e("kek", "true");
                        Picasso.with(App.getContext()).load(avatarUrl).into(target);
                    }else {
                        File imgFile = new File(prefs.getString("userInfoBg",""));
                        if(imgFile.exists()){
                            Picasso.with(getContext()).load(imgFile).into(userBackground);
                        }
                    }
                }


                if(isSquare){
                    Picasso.with(App.getContext()).load(avatarUrl).into(imgAvatarSquare);
                }else {
                    Picasso.with(App.getContext()).load(avatarUrl).into(imgAvatar);
                }
                prefs.edit()
                        .putString("shortUserInfoRep", reputation)
                        .apply();
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
        return netInfo != null && netInfo.isConnectedOrConnecting()
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected();
    }

    private void blur(Bitmap bkg, ImageView view, String url) {
        bkg = Bitmap.createScaledBitmap(bkg, view.getMeasuredWidth(), view.getMeasuredHeight(), false);

        float scaleFactor = 3;
        int radius = 64;

        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);

        overlay = FastBlur.doBlur(overlay, radius, true);
        view.setImageBitmap(overlay);
        storeImage(overlay, url);
    }
    private void storeImage(Bitmap image, String url) {
        File pictureFile = getOutputMediaFile(url);
        if (pictureFile == null) {
            Log.d("kek", "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("kek", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("kek", "Error accessing file: " + e.getMessage());
        }
    }
    private  File getOutputMediaFile(String url){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Preferences.System.getSystemDir());

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        Long tsLong = System.currentTimeMillis()/1000;
        String name = tsLong.toString();
        Matcher m = Pattern.compile("http:\\/\\/s.4pda.to\\/(.*?)\\.").matcher(url);
        if(m.find()){
            name = m.group(1);
        }
        String file = mediaStorageDir.getPath() + File.separator + name + ".png";
        prefs.edit().putString("userInfoBg", file).apply();
        return new File(file);
    }

    private boolean isPdaLink(String url) {
        if (Pattern.compile("4pda.ru/([^/$?&]+)", Pattern.CASE_INSENSITIVE).matcher(url).find())
            return true;
        return false;
    }

    public static String readFromClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            android.content.ClipDescription description = clipboard.getPrimaryClipDescription();
            android.content.ClipData data = clipboard.getPrimaryClip();
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                return String.valueOf(data.getItemAt(0).getText());
        }
        return null;
    }
}