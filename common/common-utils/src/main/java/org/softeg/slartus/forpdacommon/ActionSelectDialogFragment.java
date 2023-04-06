package org.softeg.slartus.forpdacommon;/*
 * Created by slinkin on 21.04.2014.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Arrays;

public class ActionSelectDialogFragment {


    public interface OkListener {
        void execute(CharSequence value);
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
        new AlertDialog.Builder(context)
                .setTitle(R.string.default_action)
                .setMessage(R.string.assign_default)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit().putString(preferenceKey, selectedAction).apply();

                        showTopicAction.run();

                    }
                })
                .setNeutralButton(R.string.ask, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        PreferenceManager.getDefaultSharedPreferences(context)
                                .edit().putString(preferenceKey, null).apply();

                        showTopicAction.run();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        showTopicAction.run();
                    }
                })
                .create().show();
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
        new AlertDialog.Builder(context)
                .setSingleChoiceItems(titles, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedValue[0] = i;
                    }
                })
                .setTitle(title)
                .setPositiveButton(R.string.always,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();

                                final CharSequence newValue = values[selectedValue[0]];
                                PreferenceManager.getDefaultSharedPreferences(context)
                                        .edit()
                                        .putString(preferenceKey, newValue.toString())
                                        .apply();

                                if (!TextUtils.isEmpty(hintForChangeDefault))
                                    new AlertDialog.Builder(context)
                                            .setTitle(R.string.hint)
                                            .setMessage(hintForChangeDefault)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
                        }
                )
                .setNeutralButton(R.string.only_now,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();

                                okListener.execute(values[selectedValue[0]]);
                            }
                        }
                )
                .create()
                .show();
    }
}
