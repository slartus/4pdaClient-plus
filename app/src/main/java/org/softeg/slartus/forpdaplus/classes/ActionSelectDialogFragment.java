package org.softeg.slartus.forpdaplus.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Arrays;

/**
 * Created by radiationx on 16.02.16.
 */
public class ActionSelectDialogFragment {


    public interface OkListener {
        public void execute(CharSequence value);
    }

    public static void showSaveNavigateActionDialog(final Context context,
                                                    final String preferenceKey,
                                                    final String selectedAction,
                                                    final Runnable showTopicAction) {
        final String navigateAction = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(preferenceKey, null);
        if (navigateAction == null || !selectedAction.equals(navigateAction)) {
            showSelectDialog(context, preferenceKey, selectedAction, showTopicAction);
        } else {
            showTopicAction.run();
        }

    }

    public static void showSelectDialog(final Context context,
                                        final String preferenceKey,
                                        final String selectedAction,
                                        final Runnable showTopicAction) {
        new MaterialDialog.Builder(context)
                .content("Назначить по умолчанию?")
                .positiveText("Да")
                .negativeText("Нет")
                .neutralText("Спрашивать")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit().putString(preferenceKey, selectedAction).commit();

                        showTopicAction.run();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        showTopicAction.run();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit().putString(preferenceKey, null).commit();

                        showTopicAction.run();
                    }
                })
                .show();
    }

    public static void execute(final Context context,
                               final String title,
                               final String preferenceKey,
                               CharSequence[] titles,
                               final CharSequence[] values,
                               final OkListener okListener,
                               final String hintForChangeDefault) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String value = prefs.getString(preferenceKey, null);

        if (Arrays.asList(values).contains(value)) {
            okListener.execute(value);
            return;
        }


        final int[] selectedValue = {0};
        new MaterialDialog.Builder(context)
                .title(title)
                .items(titles)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        selectedValue[0] = which;
                        return true;
                    }
                })
                .alwaysCallSingleChoiceCallback()
                .positiveText("Всегда")
                .neutralText("Только сейчас")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        final CharSequence newValue = values[selectedValue[0]];
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit()
                                .putString(preferenceKey, newValue.toString())
                                .commit();

                        if (!TextUtils.isEmpty(hintForChangeDefault))
                            new AlertDialog.Builder(context)
                                    .setTitle("Подсказка")
                                    .setMessage(hintForChangeDefault)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();

                                            okListener.execute(newValue);
                                        }
                                    })
                                    .create().show();
                        else
                            okListener.execute(newValue);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        okListener.execute(values[selectedValue[0]]);
                    }
                })
                .show();
    }
}
