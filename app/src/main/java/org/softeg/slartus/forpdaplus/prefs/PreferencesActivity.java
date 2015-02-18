package org.softeg.slartus.forpdaplus.prefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.styles.CssStyle;
import org.softeg.slartus.forpdaplus.styles.StyleInfoActivity;
import org.softeg.slartus.forpdaplus.topicview.ThemeActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 10:47
 */
public class PreferencesActivity extends BasePreferencesActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == NOTIFIERS_SERVICE_SOUND_REQUEST_CODE) {
                Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Preferences.Notifications.setSound(uri);
            }
    }

    public static final int NOTIFIERS_SERVICE_SOUND_REQUEST_CODE = MyApp.getInstance().getUniqueIntValue();


    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {


        @Override
        public void onActivityCreated(android.os.Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.news_list_prefs, false);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            //  ((PreferenceScreen)findPreference("common")).addPreference(new CheckBoxPreference(getContext()));

            findPreference("path.system_path").setOnPreferenceClickListener(this);
            findPreference("appstyle").setOnPreferenceClickListener(this);
            findPreference("About.AppVersion").setOnPreferenceClickListener(this);
            findPreference("cookies.path.SetSystemPath").setOnPreferenceClickListener(this);
            findPreference("cookies.path.SetAppPath").setOnPreferenceClickListener(this);
            findPreference("cookies.delete").setOnPreferenceClickListener(this);
            findPreference("About.History").setOnPreferenceClickListener(this);
            findPreference("About.ShareIt").setOnPreferenceClickListener(this);
            findPreference("About.AddRep").setOnPreferenceClickListener(this);
            findPreference("About.ShowTheme").setOnPreferenceClickListener(this);

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
                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        Boolean useSound = (Boolean) o;
                        findPreference("notifiers.service.is_default_sound").setEnabled(useSound);
                        findPreference("notifiers.service.sound").setEnabled(useSound);
                        return true;
                    }
                });
            }
            preference = findPreference("notifiers.service.is_default_sound");
            if (preference != null) {
                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        Boolean isDefault = (Boolean) o;
                        findPreference("notifiers.service.sound").setEnabled(!isDefault);
                        return true;
                    }
                });
            }
            findPreference("notifiers.service.sound").setOnPreferenceClickListener(this);


            final Preference downloadsPathPreference = findPreference("downloads.path");
            downloadsPathPreference.setSummary(DownloadsService.getDownloadDir(MyApp.getInstance()));
            ((EditTextPreference) downloadsPathPreference)
                    .setText(DownloadsService.getDownloadDir(MyApp.getInstance()));
            downloadsPathPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object o) {

                    if (showDownloadsPath(o)) {
                        downloadsPathPreference
                                .setSummary(o.toString());
                        Toast.makeText(getActivity(), "Путь успешно изменён", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;

                }
            });


            DonateActivity.setDonateClickListeners(this);
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
                    if (showAddRep()) return true;
                    return true;
                case "About.ShowTheme":
                    showTheme();
                    return true;
                case "appstyle":
                    showStylesDialog();
                    return true;
                case "notifiers.service.sound":
                    pickRingtone(NOTIFIERS_SERVICE_SOUND_REQUEST_CODE, Preferences.Notifications.getSound());
                    return true;
                case "notifiers.silent_mode.start_time":
                    Calendar calendar = Preferences.Notifications.SilentMode.getStartTime();
                    new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            Preferences.Notifications.SilentMode.setStartTime(hourOfDay, minute);
                            findPreference(key).setSummary(String.format("%02d:%02d", hourOfDay, minute));
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
                    return true;
                case "notifiers.silent_mode.end_time":
                    Calendar endcalendar = Preferences.Notifications.SilentMode.getEndTime();
                    new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            Preferences.Notifications.SilentMode.setEndTime(hourOfDay, minute);
                            findPreference(key).setSummary(String.format("%02d:%02d", hourOfDay, minute));
                        }
                    }, endcalendar.get(Calendar.HOUR_OF_DAY), endcalendar.get(Calendar.MINUTE), true).show();
                    return true;
            }

            return false;
        }

        private void showStylesDialog() {

            try {
                final String currentValue = MyApp.getInstance().getCurrentTheme();

                ArrayList<CharSequence> newStyleNames = new ArrayList<>();
                final ArrayList<CharSequence> newstyleValues = new ArrayList<>();

                getStylesList(getActivity(), newStyleNames, newstyleValues);


                final int[] selected = {newstyleValues.indexOf(currentValue)};
                new AlertDialogBuilder(getActivity())
                        .setTitle("Стиль")
                        .setCancelable(true)
                        .setSingleChoiceItems(newStyleNames.toArray(new CharSequence[newStyleNames.size()]), newstyleValues.indexOf(currentValue), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                selected[0] = i;
                            }
                        })
                        .setPositiveButton(getString(R.string.AcceptStyle), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (selected[0] == -1) {
                                    Toast.makeText(getActivity(), getString(R.string.ChooseStyle), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                dialogInterface.dismiss();
                                PreferenceManager.getDefaultSharedPreferences(getActivity())
                                        .edit()
                                        .putString("appstyle", newstyleValues.get(selected[0]).toString())
                                        .commit();

                            }
                        })
                        .setNeutralButton(getString(R.string.Information), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (selected[0] == -1) {
                                    Toast.makeText(getActivity(), getString(R.string.ChooseStyle), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                String stylePath = newstyleValues.get(selected[0]).toString();
                                stylePath = MyApp.getInstance().getThemeCssFileName(stylePath);
                                String xmlPath = stylePath.replace(".css", ".xml");
                                CssStyle cssStyle = CssStyle.parseStyle(getActivity(), xmlPath);
                                if (!cssStyle.ExistsInfo) {
                                    Toast.makeText(getActivity(), getString(R.string.StyleDoesNotContainDesc), Toast.LENGTH_SHORT).show();

                                    return;
                                }

                                //dialogInterface.dismiss();
                                StyleInfoActivity.showStyleInfo(getActivity(), newstyleValues.get(selected[0]).toString());
                            }
                        })
                        .create().show();
            } catch (Exception ex) {
                Log.e(getActivity(), ex);
            }

        }

        private void showAbout() {

            String text = "Неофициальный клиент для сайта <a href=\"http://www.4pda.ru\">4pda.ru</a><br/><br/>\n" +
                    "<b>Автор: </b> Артём Слинкин aka slartus<br/>\n" +
                    "<b>E-mail:</b> <a href=\"mailto:slartus+4pda@gmail.com\">slartus+4pda@gmail.com</a><br/><br/>\n" +
                    "<b>Дизайнер стилей: </b> <a href=\"http://4pda.ru/forum/index.php?showuser=96664\">Морфий</a><br/>\n" +
                    "<b>Благодарности: </b> <br/>\n" +
                    "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=474658\">zlodey.82</a></b> иконка программы<br/>\n" +
                    "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=1429916\">sbarrofff</a></b> иконка программы<br/>\n" +
                    "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=680839\">SPIDER3220</a></b> (иконки, баннеры)<br/>\n" +
                    "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=1392892\">ssmax2015</a></b> (иконки, баннеры)<br/>\n" +
                    "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=2523\">e202</a></b> (иконки сообщения для черной темы)<br/>\n" +
                    "* <b><a href=\"http://4pda.ru/forum/index.php?showuser=2040700\">Remie-l</a></b> (новые стили для топиков)<br/>\n" +
                    "* <b><a href=\"http://www.4pda.ru\">пользователям 4pda</a></b> (тестирование, идеи, поддержка)\n" +
                    "<br/>" +
                    "Copyright 2014 Artem Slinkin <slartus@gmail.com>";

            AlertDialog dialog = new AlertDialogBuilder(getActivity())
                    .setIcon(R.drawable.icon)
                    .setTitle(getProgramFullName(getActivity()))
                    .setMessage(Html.fromHtml(text))
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            dialog.show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            textView.setTextSize(12);

            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        private void pickRingtone(int requestCode, Uri defaultSound) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Выберите звук");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultSound);
            if (getActivity() != null)
                getActivity().startActivityForResult(intent, requestCode);
        }

        private boolean showDownloadsPath(Object o) {
            try {
                String dirPath = o.toString();
                if (!dirPath.endsWith(File.separator))
                    dirPath += File.separator;
                File dir = new File(dirPath);
                File file = new File(FileUtils.getUniqueFilePath(dirPath, "4pda.tmp"));

                if (!dir.exists() && !dir.mkdirs())
                    throw new NotReportException(getString(R.string.FailedToCreateFolderInPath));

                if (!file.createNewFile())
                    throw new NotReportException(getString(R.string.FailedToCreateFileInPath));
                file.delete();
                return true;
            } catch (Throwable ex) {
                Log.e(getActivity(), new NotReportException(ex.toString()));
            }
            return false;
        }

        private void showTheme() {
            ThemeActivity.showTopicById(getActivity(), "271502");
        }

        private boolean showAddRep() {
            if (!Client.getInstance().getLogined()) {
                Toast.makeText(getActivity(), getString(R.string.NeedToLogin), Toast.LENGTH_SHORT).show();
                return true;
            }
            Handler mHandler = new Handler();
            ForumUser.startChangeRep(getActivity(), mHandler, "236113", "slartus", "0", "add", getString(R.string.RaiseReputation));
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

                BufferedReader br = new BufferedReader(new InputStreamReader(MyApp.getInstance().getAssets().open("history.txt"), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

            } catch (IOException e) {
                Log.e(getActivity(), e);
            }
            AlertDialog dialog = new AlertDialogBuilder(getActivity())
                    .setIcon(R.drawable.icon)
                    .setTitle(getString(R.string.ChangesHistory))
                    .setMessage(sb)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            dialog.show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            textView.setTextSize(12);
        }

        private void showCookiesDeleteDialog() {
            new AlertDialogBuilder(getActivity())
                    .setTitle(getString(R.string.ConfirmTheAction))
                    .setMessage(getString(R.string.SureDeleteFile))
                    .setCancelable(true)
                    .setPositiveButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                dialogInterface.dismiss();
                                File f = new File(getCookieFilePath(getActivity()));
                                if (!f.exists()) {
                                    Toast.makeText(getActivity(), getString(R.string.CookiesFileNotFound) +
                                            ": " + getCookieFilePath(MyApp.getInstance()), Toast.LENGTH_LONG).show();
                                }
                                if (f.delete())
                                    Toast.makeText(getActivity(), getString(R.string.CookiesFileDeleted) +
                                            ": " + getCookieFilePath(MyApp.getInstance()), Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(getActivity(), getString(R.string.FailedDeleteCookies) +
                                            ": " + getCookieFilePath(MyApp.getInstance()), Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                Log.e(getActivity(), ex);
                            }
                        }
                    })
                    .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }

        private void showSelectDirDialog() {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dir_select_dialog, null);
            final RadioButton rbInternal = (RadioButton) view.findViewById(R.id.rbInternal);
            final RadioButton rbExternal = (RadioButton) view.findViewById(R.id.rbExternal);
            final RadioButton rbCustom = (RadioButton) view.findViewById(R.id.rbCustom);
            final EditText txtPath = (EditText) view.findViewById(R.id.txtPath);
            txtPath.setText(Preferences.System.getSystemDir());
            CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        if (compoundButton.getId() == rbInternal.getId()) {
                            txtPath.setText(MyApp.getInstance().getFilesDir().getPath());
                            txtPath.setEnabled(false);
                        } else if (compoundButton.getId() == rbExternal.getId()) {
                            try {
                                txtPath.setText(MyApp.getInstance().getExternalFilesDir(null).getPath());
                                txtPath.setEnabled(false);
                            } catch (Throwable ex) {
                                Log.e(getActivity(), ex);
                            }
                        } else if (compoundButton.getId() == rbCustom.getId()) {
                            txtPath.setEnabled(true);
                        }
                    }
                }
            };

            rbInternal.setOnCheckedChangeListener(checkedChangeListener);
            rbExternal.setOnCheckedChangeListener(checkedChangeListener);
            rbCustom.setOnCheckedChangeListener(checkedChangeListener);
            new AlertDialogBuilder(getActivity())
                    .setTitle("Путь к папке с данными")
                    .setView(view)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try{
                                String dir=txtPath.getText().toString();
                                dir=dir.replace("/", File.separator);
                                FileUtils.checkDirPath(dir);
                                Preferences.System.setSystemDir(dir);
                                dialogInterface.dismiss();
                            }catch (Throwable ex){
                                Log.e(getActivity(),ex);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }


    }

    private Context getContext() {
        return this;
    }



    private static String getAppCookiesPath() throws IOException {

        return Preferences.System.getSystemDir() + "4pda_cookies";
    }

    public static String getCookieFilePath(Context context) throws IOException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String res = prefs.getString("cookies.path", "");

        if (TextUtils.isEmpty(res))
            res = getAppCookiesPath();

        return res.replace("/", File.separator);
    }




    public static void getStylesList(Context context, ArrayList<CharSequence> newStyleNames, ArrayList<CharSequence> newstyleValues) throws IOException {
        String xmlPath;
        CssStyle cssStyle;


        String[] styleNames = context.getResources().getStringArray(R.array.appthemesArray);
        String[] styleValues = context.getResources().getStringArray(R.array.appthemesValues);
        for (int i = 0; i < styleNames.length; i++) {
            CharSequence styleName = styleNames[i];
            CharSequence styleValue = styleValues[i];

            xmlPath = MyApp.getInstance().getThemeCssFileName(styleValue.toString()).replace(".css", ".xml").replace("/android_asset/", "");
            cssStyle = CssStyle.parseStyleFromAssets(context, xmlPath);
            if (cssStyle.ExistsInfo)
                styleName = cssStyle.Title;
            newStyleNames.add(styleName);
            newstyleValues.add(styleValue);
        }


        File file = new File(Preferences.System.getSystemDir() + "styles/");
        getStylesList(newStyleNames, newstyleValues, file);
    }


    private static void getStylesList(ArrayList<CharSequence> newStyleNames, ArrayList<CharSequence> newstyleValues, File file) {
        String cssPath;
        String xmlPath;
        CssStyle cssStyle;
        if (file.exists()) {
            File[] cssFiles = file.listFiles();
            assert cssFiles != null;
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
        MyApp.resStartNotifierServices();
    }



    public static String getProgramFullName(Context context) {
        String programName = context.getString(R.string.app_name);
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);

            programName += " v" + pInfo.versionName + " c" + pInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(context, e1);
        }
        return programName;
    }

}
