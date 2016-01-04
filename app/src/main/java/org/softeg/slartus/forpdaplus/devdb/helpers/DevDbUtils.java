package org.softeg.slartus.forpdaplus.devdb.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.devdb.model.CommentsModel;
import org.softeg.slartus.forpdaplus.devdb.model.ReviewsModel;
import org.softeg.slartus.forpdaplus.devdb.model.DiscussionModel;
import org.softeg.slartus.forpdaplus.devdb.model.FirmwareModel;
import org.softeg.slartus.forpdaplus.devdb.model.PricesModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by isanechek on 19.12.15.
 */
public class DevDbUtils {

    public static void saveTitle(Context context, String title){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("devdbDeviceTitle", title).apply();
    }
    public static String getTitle(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString("devdbDeviceTitle", "ForPDA");
    }

    // COMMENTS
    public static void saveComments(Context context, List<CommentsModel> list) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("comments", json).apply();
    }

    public static ArrayList<CommentsModel> getComments(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sp.getString("comments", null);
        Type type = new TypeToken<ArrayList<CommentsModel>>() {}.getType();
        ArrayList<CommentsModel> arrayList = gson.fromJson(json, type);
        assert arrayList != null;
        return new ArrayList<>(arrayList);
    }

    //DISCUSSION
    public static void saveDiscussion(Context context, List<DiscussionModel> list) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("discussion", json).apply();
    }

    public static ArrayList<DiscussionModel> getDiscussion(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sp.getString("discussion", null);
        Type type = new TypeToken<ArrayList<DiscussionModel>>() {}.getType();
        ArrayList<DiscussionModel> arrayList = gson.fromJson(json, type);
        assert arrayList != null;
        return new ArrayList<>(arrayList);
    }

    //REVIEWS
    public static void saveReviews(Context context, List<ReviewsModel> list) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("reviews", json).apply();
    }

    public static ArrayList<ReviewsModel> getReviews(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sp.getString("reviews", null);
        Type type = new TypeToken<ArrayList<ReviewsModel>>() {}.getType();
        ArrayList<ReviewsModel> arrayList = gson.fromJson(json, type);
        assert arrayList != null;
        return new ArrayList<>(arrayList);
    }

    //FIRMWARE
    public static void saveFirmware(Context context, List<FirmwareModel> list) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("firmware", json).apply();
    }

    public static ArrayList<FirmwareModel> getFirmware(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sp.getString("firmware", null);
        Type type = new TypeToken<ArrayList<FirmwareModel>>() {}.getType();
        ArrayList<FirmwareModel> arrayList = gson.fromJson(json, type);
        assert arrayList != null;
        return new ArrayList<>(arrayList);
    }

    //PRICES
    public static void savePrices(Context context, List<PricesModel> list) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("prices", json).apply();
    }

    public static ArrayList<PricesModel> getPrices(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sp.getString("prices", null);
        Type type = new TypeToken<ArrayList<PricesModel>>() {}.getType();
        ArrayList<PricesModel> arrayList = gson.fromJson(json, type);
        assert arrayList != null;
        return new ArrayList<>(arrayList);
    }

    // OTHER
    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void showUrl(Context context, String url) {
        Handler mHandler = new Handler();
        IntentActivity.tryShowUrl(((MainActivity) context), mHandler, url, false, true);
    }
}
