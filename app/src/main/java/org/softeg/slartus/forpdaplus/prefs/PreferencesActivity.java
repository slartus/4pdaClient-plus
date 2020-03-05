package org.softeg.slartus.forpdaplus.prefs;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import org.softeg.slartus.forpdacommon.ExternalStorage;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.FilePath;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.InputFilterMinMax;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier;
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager;
import org.softeg.slartus.forpdaplus.styles.CssStyle;
import org.softeg.slartus.forpdaplus.styles.StyleInfoActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.SingleSubject;
import ru.slartus.http.PersistentCookieStore;

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 10:47
 */
public class PreferencesActivity extends BasePreferencesActivity {
    //private EditText red, green, blue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == NOTIFIERS_SERVICE_SOUND_REQUEST_CODE) {
                Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Preferences.Notifications.setSound(uri);
            }
    }

    public static final int NOTIFIERS_SERVICE_SOUND_REQUEST_CODE = App.getInstance().getUniqueIntValue();


    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {


       /* @Override
        public void onActivityCreated(android.os.Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.news_list_prefs, false);
        }*/

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            //  ((PreferenceScreen)findPreference("common")).addPreference(new CheckBoxPreference(getContext()));

            findPreference("path.system_path").setOnPreferenceClickListener(this);
            findPreference("appstyle").setOnPreferenceClickListener(this);
            findPreference("accentColor").setOnPreferenceClickListener(this);
            findPreference("mainAccentColor").setOnPreferenceClickListener(this);
            findPreference("webViewFont").setOnPreferenceClickListener(this);
            findPreference("userBackground").setOnPreferenceClickListener(this);
            findPreference("visibleMenuItems").setOnPreferenceClickListener(this);
            findPreference("About.AppVersion").setOnPreferenceClickListener(this);
            findPreference("cookies.path.SetSystemPath").setOnPreferenceClickListener(this);
            findPreference("cookies.path.SetAppPath").setOnPreferenceClickListener(this);
            findPreference("cookies.delete").setOnPreferenceClickListener(this);
            findPreference("About.History").setOnPreferenceClickListener(this);
            findPreference("About.ShareIt").setOnPreferenceClickListener(this);
            findPreference("About.AddRep").setOnPreferenceClickListener(this);
            findPreference("About.AddRepTwo").setOnPreferenceClickListener(this);
            findPreference("About.AddRepThree").setOnPreferenceClickListener(this);
            findPreference("About.ShowTheme").setOnPreferenceClickListener(this);
            findPreference("About.CheckNewVersion").setOnPreferenceClickListener(this);
            findPreference("About.OpenThemeForPda").setOnPreferenceClickListener(this);

            Preference preference = findPreference("notifiers.silent_mode.start_time");
            if (preference != null) {
                preference.setOnPreferenceClickListener(this);
                Calendar clndr = Preferences.Notifications.SilentMode.getStartTime();
                preference.setSummary(String.format("%02d:%02d", clndr.get(Calendar.HOUR_OF_DAY), clndr.get(Calendar.MINUTE)));
            }
            preference = findPreference("notifiers.silent_mode.end_time");
            if (preference != null) {
                preference.setOnPreferenceClickListener(this);
                Calendar clndr = Preferences.Notifications.SilentMode.getEndTime();
                preference.setSummary(String.format("%02d:%02d", clndr.get(Calendar.HOUR_OF_DAY), clndr.get(Calendar.MINUTE)));
            }

            preference = findPreference("notifiers.service.use_sound");
            if (preference != null) {
                preference.setOnPreferenceChangeListener((preference1, o) -> {
                    Boolean useSound = (Boolean) o;
                    findPreference("notifiers.service.is_default_sound").setEnabled(useSound);
                    findPreference("notifiers.service.sound").setEnabled(useSound);
                    return true;
                });
            }
            preference = findPreference("notifiers.service.is_default_sound");
            if (preference != null) {
                preference.setOnPreferenceChangeListener((preference12, o) -> {
                    Boolean isDefault = (Boolean) o;
                    findPreference("notifiers.service.sound").setEnabled(!isDefault);
                    return true;
                });
            }
            findPreference("notifiers.service.sound").setOnPreferenceClickListener(this);


            final Preference downloadsPathPreference = findPreference("downloads.path");
            downloadsPathPreference.setSummary(DownloadsService.getDownloadDir());
            ((EditTextPreference) downloadsPathPreference)
                    .setText(DownloadsService.getDownloadDir());
            downloadsPathPreference.setOnPreferenceChangeListener((preference13, o) -> {

                String prevValue = App.getInstance().getPreferences().getString("downloads.path", "");
                m_PathPermission = SingleSubject.create();
                App.getInstance().addToDisposable(
                        m_PathPermission
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((aBoolean, throwable) -> {
                                    if (aBoolean) {
                                        Toast.makeText(getActivity(), R.string.path_edited_success, Toast.LENGTH_SHORT).show();
                                    } else {
                                        App.getInstance().getPreferences().edit().putString("downloads.path", prevValue).apply();
                                        ((EditTextPreference) downloadsPathPreference).setText(prevValue);
                                    }
                                    if (throwable != null) {
                                        AppLog.e(throwable);
                                    }
                                }));
                checkDownloadsPath(o);

                return true;

            });


            DonateActivity.setDonateClickListeners(this);

            findPreference("showExitButton").setOnPreferenceClickListener(this);

        }


        @Override
        public boolean onPreferenceClick(Preference preference) {
            final String key = preference.getKey();
            switch (key) {
                case "path.system_path":
                    showSelectDirDialog();
                    return true;
                case "About.AppVersion":
                    showAbout();
                    return true;
                case "cookies.delete":
                    showCookiesDeleteDialog();
                    return true;
                case "About.History":
                    showAboutHistory();
                    return true;
                case "About.ShareIt":
                    showShareIt();
                    return true;
                case "About.AddRep":
                    if (showAddRep("236113", "slartus")) return true;
                    return true;
                case "About.AddRepTwo":
                    if (showAddRep("2556269", "Radiation15")) return true;
                    return true;
                case "About.AddRepThree":
                    if (showAddRep("1726458", "iSanechek")) return true;
                    return true;
                case "About.ShowTheme":
                    showTheme("271502");
                    return true;
                case "appstyle":
                    showStylesDialog();
                    return true;
                case "accentColor":
                    showAccentColorDialog();
                    return true;
                case "mainAccentColor":
                    showMainAccentColorDialog();
                    return true;
                case "webViewFont":
                    webViewFontDialog();
                    return true;
                case "userBackground":
                    pickUserBackground();
                    return true;
                case "visibleMenuItems":
                    setMenuItems();
                    return true;
                case "notifiers.service.sound":
                    pickRingtone(NOTIFIERS_SERVICE_SOUND_REQUEST_CODE, Preferences.Notifications.getSound());
                    return true;
                case "notifiers.silent_mode.start_time":
                    Calendar calendar = Preferences.Notifications.SilentMode.getStartTime();
                    new TimePickerDialog(getActivity(), (timePicker, hourOfDay, minute) -> {
                        Preferences.Notifications.SilentMode.setStartTime(hourOfDay, minute);
                        findPreference(key).setSummary(String.format("%02d:%02d", hourOfDay, minute));
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                    return true;
                case "notifiers.silent_mode.end_time":
                    Calendar endcalendar = Preferences.Notifications.SilentMode.getEndTime();
                    new TimePickerDialog(getActivity(), (timePicker, hourOfDay, minute) -> {
                        Preferences.Notifications.SilentMode.setEndTime(hourOfDay, minute);
                        findPreference(key).setSummary(String.format("%02d:%02d", hourOfDay, minute));
                    }, endcalendar.get(Calendar.HOUR_OF_DAY), endcalendar.get(Calendar.MINUTE), true).show();
                    return true;
                case "About.CheckNewVersion":
                    checkUpdates();
                    return true;
                case "About.OpenThemeForPda":
                    showTheme("820313");
                    return true;
            }

            return false;
        }

        private void checkUpdates() {
            NotifiersManager notifiersManager = new NotifiersManager();
            new ForPdaVersionNotifier(notifiersManager, 0, true).start(getActivity());
        }


        private void setMenuItems() {
            final SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

            String[] items = preferences.getString("selectedMenuItems", ListCore.DEFAULT_MENU_ITEMS).split(",");
            ArrayList<BrickInfo> allItems = ListCore.getAllMenuBricks();

            if (ListCore.checkIndex(items, allItems.size())) {
                items = ListCore.DEFAULT_MENU_ITEMS.split(",");
            }

            Integer[] selectedItems = new Integer[items.length];

            for (int i = 0; i < items.length; i++)
                selectedItems[i] = Integer.parseInt(items[i]);


            ArrayList<String> namesArray = new ArrayList<>();
            for (BrickInfo item : allItems)
                namesArray.add(item.getTitle());

            final Integer[][] finalItems = new Integer[1][1];
            finalItems[0] = selectedItems;

            new MaterialDialog.Builder(getActivity())
                    .title(R.string.select_items)
                    .items(namesArray.toArray(new CharSequence[namesArray.size()]))
                    .itemsCallbackMultiChoice(selectedItems, (materialDialog, integers, charSequences) -> {
                        finalItems[0] = integers;
                        return true;
                    })
                    .alwaysCallMultiChoiceCallback()
                    .positiveText(R.string.accept)
                    .onPositive((materialDialog, dialogAction) -> {
                        if (finalItems[0] == null || finalItems[0].length == 0) return;
                        preferences.edit().putString("selectedMenuItems", Arrays.toString(finalItems[0]).replace(" ", "").replace("[", "").replace("]", "")).apply();
                    })
                    .neutralText(R.string.reset)
                    .onNeutral((materialDialog, dialogAction) -> preferences.edit().putString("selectedMenuItems", ListCore.DEFAULT_MENU_ITEMS).apply())
                    .show();
        }

        private static final int MY_INTENT_CLICK = 302;

        private void pickUserBackground() {
            new MaterialDialog.Builder(getActivity())
                    .content(R.string.pick_image)
                    .positiveText(R.string.choose)
                    .negativeText(R.string.cancel)
                    .neutralText(R.string.reset)
                    .onPositive((dialog, which) -> {
                        try {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, MY_INTENT_CLICK);
                        } catch (ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), R.string.no_app_for_get_image_file, Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            AppLog.e(getActivity(), ex);
                        }
                    })
                    .onNeutral((dialog, which) -> App.getInstance().getPreferences()
                            .edit()
                            .putString("userInfoBg", "")
                            .putBoolean("isUserBackground", false)
                            .apply())
                    .show();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                if (requestCode == MY_INTENT_CLICK) {
                    if (null == data) return;
                    Uri selectedImageUri = data.getData();
                    String selectedImagePath = FilePath.getPath(App.getContext(), selectedImageUri);
                    if (selectedImagePath != null)
                        App.getInstance().getPreferences()
                                .edit()
                                .putString("userInfoBg", selectedImagePath)
                                .putBoolean("isUserBackground", true)
                                .apply();
                    else
                        Toast.makeText(getActivity(), "Не могу прикрепить файл", Toast.LENGTH_SHORT).show();

                }
            }
        }

        public void webViewFontDialog() {
            try {
                final SharedPreferences prefs = App.getInstance().getPreferences();
                final int[] selected = {prefs.getInt("webViewFont", 0)};
                final CharSequence[] name = {""};
                final boolean[] dialogShowed = {false};
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.choose_font)
                        .items(new String[]{App.getContext().getString(R.string.font_from_style), App.getContext().getString(R.string.system_font), App.getContext().getString(R.string.enter_font_name)})
                        .itemsCallbackSingleChoice(selected[0], (dialog, view, which, text) -> {
                            selected[0] = which;
                            switch (which) {
                                case 0:
                                    name[0] = "";
                                    break;
                                case 1:
                                    name[0] = "inherit";
                                    break;
                                case 2:
                                    if (dialogShowed[0]) return true;
                                    dialogShowed[0] = true;
                                    new MaterialDialog.Builder(getActivity())
                                            .inputType(InputType.TYPE_CLASS_TEXT)
                                            .input(App.getContext().getString(R.string.font_name), prefs.getString("webViewFontName", ""), (dialog1, input) -> name[0] = input)
                                            .positiveText(R.string.ok)
                                            .onPositive((materialDialog, dialogAction) -> prefs.edit().putString("webViewFontName", name[0].toString()).apply())
                                            .show();
                            }
                            return true;
                        })
                        .alwaysCallSingleChoiceCallback()
                        .positiveText(R.string.accept)
                        .negativeText(R.string.cancel)
                        .onPositive((materialDialog, dialogAction) -> prefs.edit().putString("webViewFontName", name[0].toString())
                                .putInt("webViewFont", selected[0]).apply())
                        .show();

            } catch (Exception ex) {
                AppLog.e(getActivity(), ex);
            }
        }

        private void showMainAccentColorDialog() {
            try {
                final SharedPreferences prefs = App.getInstance().getPreferences();
                String string = prefs.getString("mainAccentColor", "pink");
                int position = -1;
                switch (string) {
                    case "pink":
                        position = 0;
                        break;
                    case "blue":
                        position = 1;
                        break;
                    case "gray":
                        position = 2;
                        break;
                }
                final int[] selected = {0};
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.pick_accent_color)
                        .items(new String[]{App.getContext().getString(R.string.blue), App.getContext().getString(R.string.pink), App.getContext().getString(R.string.gray)})
                        .itemsCallbackSingleChoice(position, (dialog, view, which, text) -> {
                            selected[0] = which;
                            return true;
                        })
                        .alwaysCallSingleChoiceCallback()
                        .positiveText(R.string.accept)
                        .negativeText(R.string.cancel)
                        .onPositive((dialog, which) -> {
                            switch (selected[0]) {
                                case 0:
                                    prefs.edit().putString("mainAccentColor", "pink").apply();
                                    if (!prefs.getBoolean("accentColorEdited", false)) {
                                        prefs.edit()
                                                .putInt("accentColor", Color.rgb(2, 119, 189))
                                                .putInt("accentColorPressed", Color.rgb(0, 89, 159))
                                                .apply();
                                    }
                                    break;
                                case 1:
                                    prefs.edit().putString("mainAccentColor", "blue").apply();
                                    if (!prefs.getBoolean("accentColorEdited", false)) {
                                        prefs.edit()
                                                .putInt("accentColor", Color.rgb(233, 30, 99))
                                                .putInt("accentColorPressed", Color.rgb(203, 0, 69))
                                                .apply();
                                    }
                                    break;
                                case 2:
                                    prefs.edit().putString("mainAccentColor", "gray").apply();
                                    if (!prefs.getBoolean("accentColorEdited", false)) {
                                        prefs.edit()
                                                .putInt("accentColor", Color.rgb(117, 117, 117))
                                                .putInt("accentColorPressed", Color.rgb(87, 87, 87))
                                                .apply();
                                    }
                                    break;
                            }
                        })
                        .show();

            } catch (Exception ex) {
                AppLog.e(getActivity(), ex);
            }

        }

        private void showAccentColorDialog() {

            try {
                final SharedPreferences prefs = App.getInstance().getPreferences();

                int prefColor = (int) Long.parseLong(String.valueOf(prefs.getInt("accentColor", Color.rgb(2, 119, 189))), 10);
                //int prefColor = (int) Long.parseLong(String.valueOf(prefs.getInt("accentColor", Color.rgb(96, 125, 139))), 10);
                final int[] colors = {(prefColor >> 16) & 0xFF, (prefColor >> 8) & 0xFF, (prefColor) & 0xFF};

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                View view = inflater.inflate(R.layout.color_editor, null);
                final EditText redTxt = view.findViewById(R.id.redText);
                final EditText greenTxt = view.findViewById(R.id.greenText);
                final EditText blueTxt = view.findViewById(R.id.blueText);

                final LinearLayout preview = view.findViewById(R.id.preview);

                final SeekBar red = view.findViewById(R.id.red);
                final SeekBar green = view.findViewById(R.id.green);
                final SeekBar blue = view.findViewById(R.id.blue);

                redTxt.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
                greenTxt.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
                blueTxt.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});

                redTxt.setText(String.valueOf(colors[0]));
                greenTxt.setText(String.valueOf(colors[1]));
                blueTxt.setText(String.valueOf(colors[2]));

                red.setProgress(colors[0]);
                green.setProgress(colors[1]);
                blue.setProgress(colors[2]);

                preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]));

                redTxt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (redTxt.getText().toString().equals("")) {
                            colors[0] = 0;
                        } else {
                            colors[0] = Integer.parseInt(redTxt.getText().toString());
                        }
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]));
                        red.setProgress(colors[0]);
                        redTxt.setSelection(redTxt.getText().length());
                    }
                });
                greenTxt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (greenTxt.getText().toString().equals("")) {
                            colors[1] = 0;
                        } else {
                            colors[1] = Integer.parseInt(greenTxt.getText().toString());
                        }
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]));
                        green.setProgress(colors[1]);
                        greenTxt.setSelection(greenTxt.getText().length());
                    }
                });
                blueTxt.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (blueTxt.getText().toString().equals("")) {
                            colors[2] = 0;
                        } else {
                            colors[2] = Integer.parseInt(blueTxt.getText().toString());
                        }
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], colors[2]));
                        blue.setProgress(colors[2]);
                        blueTxt.setSelection(blueTxt.getText().length());
                    }
                });

                red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        redTxt.setText(String.valueOf(progress));
                        preview.setBackgroundColor(Color.rgb(progress, colors[1], colors[2]));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        colors[0] = seekBar.getProgress();
                    }
                });
                green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        greenTxt.setText(String.valueOf(progress));
                        preview.setBackgroundColor(Color.rgb(colors[0], progress, colors[2]));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        colors[1] = seekBar.getProgress();
                    }
                });
                blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        blueTxt.setText(String.valueOf(progress));
                        preview.setBackgroundColor(Color.rgb(colors[0], colors[1], progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        colors[2] = seekBar.getProgress();
                    }
                });

                new MaterialDialog.Builder(getActivity())
                        .title(R.string.color)
                        .customView(view, true)
                        .positiveText(R.string.accept)
                        .negativeText(R.string.cancel)
                        .neutralText(R.string.reset)
                        .onPositive((dialog, which) -> {
                            int[] colorPressed = {colors[0] - 30, colors[1] - 30, colors[2] - 30};
                            if (colorPressed[0] < 0) colorPressed[0] = 0;
                            if (colorPressed[1] < 0) colorPressed[1] = 0;
                            if (colorPressed[2] < 0) colorPressed[2] = 0;
                            if (Color.rgb(colors[0], colors[1], colors[2]) != prefs.getInt("accentColor", Color.rgb(2, 119, 189))) {
                                prefs.edit().putBoolean("accentColorEdited", true).apply();
                            }
                            prefs.edit()
                                    .putInt("accentColor", Color.rgb(colors[0], colors[1], colors[2]))
                                    .putInt("accentColorPressed", Color.rgb(colorPressed[0], colorPressed[1], colorPressed[2]))
                                    .apply();
                        })
                        .onNeutral((dialog, which) -> prefs.edit()
                                .putInt("accentColor", Color.rgb(2, 119, 189))
                                .putInt("accentColorPressed", Color.rgb(0, 89, 159))
                                .putBoolean("accentColorEdited", false)
                                //.putInt("accentColor", Color.rgb(96, 125, 139))
                                //.putInt("accentColorPressed", Color.rgb(76, 95, 109))
                                .apply())
                        .show();
            } catch (Exception ex) {
                AppLog.e(getActivity(), ex);
            }

        }

        /*private int PICK_IMAGE_REQUEST = 1;
        private void pickUserBackground() {

            try {
                Intent intent = new Intent();
// Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);


            } catch (Exception ex) {
                AppLog.e(getActivity(), ex);
            }

        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                Uri uri = data.getData();
                App.getInstance().getPreferences()
                        .edit()
                        .putString("userBackground", uri.toString())
                        .commit();
            }
        }*/
        private void showStylesDialog() {

            try {
                final String currentValue = App.getInstance().getCurrentTheme();

                ArrayList<CharSequence> newStyleNames = new ArrayList<>();
                final ArrayList<CharSequence> newstyleValues = new ArrayList<>();

                getStylesList(getActivity(), newStyleNames, newstyleValues);


                final int[] selected = {newstyleValues.indexOf(currentValue)};
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.theme_style)
                        .cancelable(true)
                        .items(newStyleNames.toArray(new CharSequence[newStyleNames.size()]))
                        .itemsCallbackSingleChoice(newstyleValues.indexOf(currentValue), (dialog, view, i, text) -> {
                            selected[0] = i;
                            return true; // allow selection
                        })
                        .alwaysCallSingleChoiceCallback()
                        .positiveText(getString(R.string.AcceptStyle))
                        .neutralText(getString(R.string.Information))
                        .onPositive((dialog, which) -> {
                            if (selected[0] == -1) {
                                Toast.makeText(getActivity(), getString(R.string.ChooseStyle), Toast.LENGTH_LONG).show();
                                return;
                            }
                            App.getInstance().getPreferences()
                                    .edit()
                                    .putString("appstyle", newstyleValues.get(selected[0]).toString())
                                    .apply();
                        })
                        .onNeutral((dialog, which) -> {
                            if (selected[0] == -1) {
                                Toast.makeText(getActivity(), getString(R.string.ChooseStyle), Toast.LENGTH_LONG).show();
                                return;
                            }
                            String stylePath = newstyleValues.get(selected[0]).toString();
                            stylePath = App.getInstance().getThemeCssFileName(stylePath);
                            String xmlPath = stylePath.replace(".css", ".xml");
                            CssStyle cssStyle = CssStyle.parseStyle(getActivity(), xmlPath);
                            if (!cssStyle.ExistsInfo) {
                                Toast.makeText(getActivity(), getString(R.string.StyleDoesNotContainDesc), Toast.LENGTH_SHORT).show();

                                return;
                            }

                            //dialogInterface.dismiss();
                            StyleInfoActivity.showStyleInfo(getActivity(), newstyleValues.get(selected[0]).toString());
                        })
                        .show();
            } catch (Exception ex) {
                AppLog.e(getActivity(), ex);
            }

        }

        private void showAbout() {

            String text = "<b>Неофициальный клиент для сайта <a href=\"http://www.4pda.ru\">4pda.ru</a></b><br/><br/>\n" +
                    "<b>Автор: </b> Артём Слинкин aka <a href=\"http://4pda.ru/forum/index.php?showuser=236113\">slartus</a><br/>\n" +
                    "<b>E-mail:</b> <a href=\"mailto:slartus+4pda@gmail.com\">slartus+4pda@gmail.com</a><br/><br/>\n" +
                    "<b>Разработчик(v3.x): </b> Евгений Низамиев aka <a href=\"http://4pda.ru/forum/index.php?showuser=2556269\">Radiation15</a><br/>\n" +
                    "<b>E-mail:</b> <a href=\"mailto:radiationx@yandex.ru\">radiationx@yandex.ru</a><br/><br/>\n" +
                    "<b>Разработчик(v3.x):</b> Александр Тайнюк aka <a href=\"http://4pda.ru/forum/index.php?showuser=1726458\">iSanechek</a><br/>\n" +
                    "<b>E-mail:</b> <a href=\"mailto:devuicore@gmail.com\">devuicore@gmail.com</a><br/><br/>\n" +
                    "<b>Помощник разработчиков: </b> Алексей Шолохов aka <a href=\"http://4pda.ru/forum/index.php?showuser=96664\">Морфий</a>\n" +
                    "<b>E-mail:</b> <a href=\"mailto:asolohov@gmail.com\">asolohov@gmail.com</a><br/><br/>\n" +
                    "<b>Благодарности: </b> <br/>\n" +
                    "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=1657987\">__KoSyAk__</a></b> Иконка программы<br/>\n" +
                    "* <b>Пользователям 4pda</b> (тестирование, идеи, поддержка)\n" +
                    "<br/><br/>" +
                    "Copyright 2011-2016 Artem Slinkin <slartus@gmail.com>";

            new MaterialDialog.Builder(getActivity())
                    .title(getProgramFullName())
                    .content(Html.fromHtml(text))
                    .positiveText(android.R.string.ok)
                    .show();
            //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            //textView.setTextSize(12);

            //textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        private void pickRingtone(int requestCode, Uri defaultSound) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, App.getContext().getString(R.string.pick_audio));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultSound);
            if (getActivity() != null)
                getActivity().startActivityForResult(intent, requestCode);
        }

        private SingleSubject<Boolean> m_PathPermission = SingleSubject.create();

        private void checkExternalPathPermission(String dirPath) {
            Dexter.withActivity(getActivity())
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            tryCreateTempFile(dirPath);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            Toast.makeText(App.getInstance(), R.string.error_external_storage, Toast.LENGTH_SHORT).show();
                            m_PathPermission.onSuccess(false);
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            //Toast.makeText(App.getInstance(), R.string.error_external_storage, Toast.LENGTH_SHORT).show();
                            if (token != null)
                                token.continuePermissionRequest();
                        }
                    })
                    .check();
        }

        private void tryCreateTempFile(String dirPath) {
            try {
                File dir = new File(dirPath);
                File file = new File(org.softeg.slartus.forpdacommon.FileUtils.getUniqueFilePath(dirPath, "4pda.tmp"));

                if (!dir.exists() && !dir.mkdirs()) {
                    m_PathPermission.onSuccess(false);
                    throw new NotReportException(getString(R.string.FailedToCreateFolderInPath));
                }

                if (!file.createNewFile()) {
                    m_PathPermission.onSuccess(false);
                    throw new NotReportException(getString(R.string.FailedToCreateFileInPath));
                }
                file.delete();
                m_PathPermission.onSuccess(true);

            } catch (Throwable ex) {
                m_PathPermission.onSuccess(false);
                AppLog.e(getActivity(), new NotReportException(ex.toString()));
            }
        }

        private void checkDownloadsPath(Object o) {
            try {
                String dirPath = o.toString();
                if (!dirPath.endsWith(File.separator))
                    dirPath += File.separator;

                List<File> externalLocations = ExternalStorage.getAllStorageLocations();
                for (File externalLocation : externalLocations) {
                    if (dirPath.startsWith(externalLocation.getAbsolutePath()) || dirPath.startsWith(externalLocation.getCanonicalPath())) {
                        checkExternalPathPermission(dirPath);
                        return;
                    }
                }
                tryCreateTempFile(dirPath);

            } catch (Throwable ex) {
                m_PathPermission.onSuccess(false);
                AppLog.e(getActivity(), new NotReportException(ex.toString()));
            }
        }

        private void showTheme(String themeId) {
            getActivity().finish();
            ThemeFragment.showTopicById(themeId);
        }

        private boolean showAddRep(String id, String nick) {
            if (!Client.getInstance().getLogined()) {
                Toast.makeText(getActivity(), getString(R.string.NeedToLogin), Toast.LENGTH_SHORT).show();
                return true;
            }
            Handler mHandler = new Handler();
            ForumUser.startChangeRep(getActivity(), mHandler, id, nick, "0", "add", getString(R.string.RaiseReputation));
            return false;
        }

        private void showShareIt() {
            Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
            sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Recomend));
            sendMailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.RecommendText));
            sendMailIntent.setType("text/plain");

            startActivity(Intent.createChooser(sendMailIntent, getString(R.string.SendBy_)));
        }

        private void showAboutHistory() {
            StringBuilder sb = new StringBuilder();
            try {

                BufferedReader br = new BufferedReader(new InputStreamReader(App.getInstance().getAssets().open("history.txt"), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

            } catch (IOException e) {
                AppLog.e(getActivity(), e);
            }
            new MaterialDialog.Builder(getActivity())
                    .title(getString(R.string.ChangesHistory))
                    .content(sb)
                    .positiveText(android.R.string.ok)
                    .show();
            //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            //textView.setTextSize(12);
        }

        private void showCookiesDeleteDialog() {
            new MaterialDialog.Builder(getActivity())
                    .title(getString(R.string.ConfirmTheAction))
                    .content(getString(R.string.SureDeleteFile))
                    .cancelable(true)
                    .positiveText(getString(R.string.Delete))
                    .negativeText(getString(android.R.string.no))
                    .onPositive((dialog, which) -> {
                        try {
                            File f = new File(getCookieFilePath());
                            if (!f.exists()) {
                                Toast.makeText(getActivity(), getString(R.string.CookiesFileNotFound) +
                                        ": " + getCookieFilePath(), Toast.LENGTH_LONG).show();
                            }
                            if (f.delete())
                                Toast.makeText(getActivity(), getString(R.string.CookiesFileDeleted) +
                                        ": " + getCookieFilePath(), Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getActivity(), getString(R.string.FailedDeleteCookies) +
                                        ": " + getCookieFilePath(), Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            AppLog.e(getActivity(), ex);
                        }
                    })
                    .show();
        }

        private void showSelectDirDialog() {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.dir_select_dialog, null);
            final RadioButton rbInternal = view.findViewById(R.id.rbInternal);
            final RadioButton rbExternal = view.findViewById(R.id.rbExternal);
            final RadioButton rbCustom = view.findViewById(R.id.rbCustom);
            final EditText txtPath = view.findViewById(R.id.txtPath);
            txtPath.setText(Preferences.System.getSystemDir());
            CompoundButton.OnCheckedChangeListener checkedChangeListener = (compoundButton, b) -> {
                if (b) {
                    if (compoundButton.getId() == rbInternal.getId()) {
                        txtPath.setText(App.getInstance().getFilesDir().getPath());
                        txtPath.setEnabled(false);
                    } else if (compoundButton.getId() == rbExternal.getId()) {
                        try {
                            txtPath.setText(App.getInstance().getExternalFilesDir(null).getPath());
                            txtPath.setEnabled(false);
                        } catch (Throwable ex) {
                            AppLog.e(getActivity(), ex);
                        }
                    } else if (compoundButton.getId() == rbCustom.getId()) {
                        txtPath.setEnabled(true);
                    }
                }
            };

            rbInternal.setOnCheckedChangeListener(checkedChangeListener);
            rbExternal.setOnCheckedChangeListener(checkedChangeListener);
            rbCustom.setOnCheckedChangeListener(checkedChangeListener);
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.path_to_data)
                    .customView(view, true)
                    .cancelable(true)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog, which) -> {
                        try {
                            String dir = txtPath.getText().toString();
                            dir = dir.replace("/", File.separator);
                            org.softeg.slartus.forpdacommon.FileUtils.checkDirPath(dir);
                            Preferences.System.setSystemDir(dir);
                        } catch (Throwable ex) {
                            AppLog.e(getActivity(), ex);
                        }
                    })
                    .show();
        }


    }

    private static String getAppCookiesPath() {

        return Preferences.System.getSystemDir() + "4pda_cookies";
    }

    public static String getCookieFilePath() {
        String res = App.getInstance().getPreferences().getString("cookies.path", "");

        if (TextUtils.isEmpty(res))
            res = getAppCookiesPath();

        return res.replace("/", File.separator);
    }


    public static void getStylesList(Context context, ArrayList<CharSequence> newStyleNames, ArrayList<CharSequence> newstyleValues) {
        String xmlPath;
        CssStyle cssStyle;


        String[] styleNames = context.getResources().getStringArray(R.array.appthemesArray);
        String[] styleValues = context.getResources().getStringArray(R.array.appthemesValues);
        for (int i = 0; i < styleNames.length; i++) {
            CharSequence styleName = styleNames[i];
            CharSequence styleValue = styleValues[i];

            xmlPath = App.getInstance().getThemeCssFileName(styleValue.toString()).replace(".css", ".xml").replace("/android_asset/", "");
            cssStyle = CssStyle.parseStyleFromAssets(context, xmlPath);
            if (cssStyle.ExistsInfo)
                styleName = cssStyle.Title;
            newStyleNames.add(styleName);
            newstyleValues.add(styleValue);
        }


        File file = new File(Preferences.System.getSystemDir() + "styles/");
        getStylesList(newStyleNames, newstyleValues, file);
    }


    private static void getStylesList(ArrayList<CharSequence> newStyleNames,
                                      ArrayList<CharSequence> newstyleValues, File file) {
        String cssPath;
        String xmlPath;
        CssStyle cssStyle;
        if (file.exists()) {
            File[] cssFiles = file.listFiles();
            if (cssFiles == null)
                return;
            for (File cssFile : cssFiles) {
                if (cssFile.isDirectory()) {
                    getStylesList(newStyleNames, newstyleValues, cssFile);
                    continue;
                }
                cssPath = cssFile.getPath();
                if (!cssPath.toLowerCase().endsWith(".css")) continue;
                xmlPath = cssPath.replace(".css", ".xml");

                cssStyle = CssStyle.parseStyleFromFile(xmlPath);

                String title = cssStyle.Title;


                newStyleNames.add(title);
                newstyleValues.add(cssPath);

            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        App.resStartNotifierServices();
        PersistentCookieStore.Companion.getInstance(App.getInstance()).reload();
    }

    public static PackageInfo getPackageInfo() {
        String packageName = App.getInstance().getPackageName();
        try {

            return App.getInstance().getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e1) {
            AppLog.e(App.getInstance(), e1);
        }
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = packageName;
        packageInfo.versionName = "unknown";
        packageInfo.versionCode = 1;
        return packageInfo;
    }

    public static String getProgramFullName() {
        String programName = App.getInstance().getString(R.string.app_name);

        PackageInfo pInfo = getPackageInfo();

        programName += " v" + pInfo.versionName + " c" + pInfo.versionCode;

        return programName;
    }

}
