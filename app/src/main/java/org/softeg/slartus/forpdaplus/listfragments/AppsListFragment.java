package org.softeg.slartus.forpdaplus.listfragments;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.AppItem;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.PdaApplication;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.ApplicationRelationsTable;
import org.softeg.slartus.forpdaplus.db.ApplicationsDbHelper;
import org.softeg.slartus.forpdaplus.db.DbHelper;
import org.softeg.slartus.forpdaplus.listfragments.adapters.ListAdapter;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppsListFragment extends TopicsListFragment {
    public AppsListFragment() {
        super();
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new ListAdapter(getActivity(), getMData(), getPreferences().getBoolean("showSubMain", false));
    }

    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException {
        return loadItems();
    }

    @Override
    protected void updateItem(IListItem item) {
        saveCache();
    }


    public ArrayList<AppItem> loadItems() throws IOException {
        PackageManager packageManager = getContext().getPackageManager();
        ArrayList<AppItem> apps = new ArrayList<>();
        if (packageManager == null)
            return apps;
        List<PackageInfo> applications = packageManager.getInstalledPackages(0);

        ArrayList<String> appsName = new ArrayList<>();


        for (PackageInfo application : applications) {
            if (isCancelled())
                return null;
            if (application.applicationInfo == null) continue;
            if (!filterApp(application.applicationInfo)) continue;

            CharSequence title = application.applicationInfo.loadLabel(packageManager);
            AppItem topic = new AppItem("", title);

            topic.setDescription(application.packageName);

            topic.setPackageName(application.packageName);
            apps.add(topic);
        }

        //if(!compareFromCache(apps))
        compareFromBases(appsName, apps);

        Boolean allFinded = apps.size() > 0;// если просто поставить тру, и нет apps, то неверно
        for (AppItem app : apps) {
            if (isCancelled())
                return null;
            if (app.Ids.size() == 0) {
                allFinded = false;
                break;
            }
        }

        if (!allFinded)
            compareFromSite(appsName, apps);

        sort(apps);
        //saveCache(apps);
        return apps;
    }

    private void sort(ArrayList<AppItem> apps) {
        Collections.sort(apps, (topic, topic1) -> {
            if (topic.getFindedState() != topic1.getFindedState()) {
                if (topic1.getFindedState() == AppItem.STATE_FINDED_AND_HAS_UPDATE)
                    return 1;
                if (topic.getFindedState() == AppItem.STATE_FINDED_AND_HAS_UPDATE)
                    return -1;
                if (topic1.getFindedState() == AppItem.STATE_UNFINDED)
                    return -1;
                if (topic.getFindedState() == AppItem.STATE_UNFINDED)
                    return 1;
            }
            return topic.getTitle().toString().toUpperCase().compareTo(topic1.getTitle().toString().toUpperCase());
        });
    }

    private void compareFromBases(ArrayList<String> appsName, ArrayList<AppItem> apps) {
        SQLiteDatabase db = null;
        SQLiteDatabase appsDb = null;
        try {

            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getReadableDatabase();
            ApplicationsDbHelper applicationsDbHelper = new ApplicationsDbHelper(App.getInstance());
            appsDb = applicationsDbHelper.getReadableDatabase();
            for (AppItem app : apps) {
                if (isCancelled())
                    return;
                ArrayList<PdaApplication> pdaApps;
                try {
                    pdaApps = ApplicationRelationsTable.getApplications(db, app.getPackageName());
                    for (PdaApplication pdaApplication : pdaApps) {
                        if (isCancelled())
                            return;
                        String id = Integer.toString(pdaApplication.AppUrl);
                        app.Ids.add(id);
                        app.setFindedState(AppItem.STATE_NORMAL);
                        app.setId(id);
                    }
                    if (app.Ids.size() != 0) {
                        appsName.add(null);
                        continue;
                    }
                } catch (Throwable ex) {
                    AppLog.e(null, ex);
                }
                try {
                    pdaApps = ApplicationRelationsTable.getApplications(appsDb, normalizePackName(app.getDescription().toString()));

                    for (PdaApplication pdaApplication : pdaApps) {
                        if (isCancelled())
                            return;
                        String id = Integer.toString(pdaApplication.AppUrl);
                        app.Ids.add(id);
                        app.setFindedState(AppItem.STATE_NORMAL);
                        app.setId(id);
                    }
                    //if (app.Ids.size() != 1)
                    appsName.add(app.Ids.size() != 1 ? normalizeTitle(app.getTitle()) : null);
                } catch (Throwable ex) {
                    AppLog.e(null, ex);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.close();
            if (appsDb != null)
                appsDb.close();

        }

    }

    private void compareFromSite(ArrayList<String> appsName, ArrayList<AppItem> apps) throws IOException {
        final String appCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=112220";
        final String gameCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=117270";
        // Client.getInstance().doOnOnProgressChanged(progressChangedListener, "Получение данных...");

        String gamesBody = Client.getInstance().loadPageAndCheckLogin(gameCatalogUrl, null);
        String appsBody = Client.getInstance().loadPageAndCheckLogin(appCatalogUrl, null);
        //  Client.getInstance().doOnOnProgressChanged(progressChangedListener, "Обработка данных...");
        Matcher m = Pattern.compile("http://4pda.ru/forum/index.php\\?showtopic=(\\d+)[^\"]*?. target=._blank.>(.*?)</a>(.*?)</li>", Pattern.CASE_INSENSITIVE)
                .matcher(gamesBody);
        compareFromMatcher(appsName, apps, m);

        m = Pattern.compile("<a href=\"(?:http://4pda.ru)?/forum/index.php\\?showtopic=(\\d+)\" target=._blank.>(.*?)</a>.*?(?:</b>)? - (.*?)<", Pattern.CASE_INSENSITIVE)
                .matcher(appsBody);
        compareFromMatcher(appsName, apps, m);
    }

    private void compareFromMatcher(ArrayList<String> appsName, ArrayList<AppItem> apps, Matcher m) {
        while (m.find()) {
            String id = m.group(1);
            AppItem app;
            while ((app = findById(apps, id)) != null) {
                if (isCancelled())
                    return;
                app.Ids.clear();
                app.setId(id);
                app.setDescription(Html.fromHtml(m.group(3)));
                app.setFindedState(AppItem.STATE_NORMAL);
            }

            String normTitle = normalizeTitle(m.group(2));
            for (int i = 0; i < appsName.size(); i++) {
                if (isCancelled())
                    return;
                if (normTitle.equals(appsName.get(i))) {
                    app = apps.get(i);
                    app.Ids.clear();
                    app.setId(id);
                    app.setDescription(Html.fromHtml(m.group(3)));
                    app.setFindedState(AppItem.STATE_NORMAL);

                    appsName.set(i, null);

                }
            }
        }
    }

    private Boolean filterApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true;
        } else return (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
    }

    private static final CharSequence normalizePattern = "\\d+|alpha|beta|pro|trial|free|plus|premium|donate|demo|paid|special|next|hd|ultimate|pc|lite|classic";

    public static String normalizeTitle(CharSequence title) {
        return title.toString().toLowerCase().replaceAll("\\.?(" + normalizePattern + ")$|\\.?\\d+\\.?|\\s+", "");
    }

    private static String normalizePackName(String packName) {
        return packName.toLowerCase().replaceAll("\\.?(" + normalizePattern + ")$", "").replaceAll("\\.(" + normalizePattern + ")\\.", "%");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        try {
            super.onCreateContextMenu(menu, v, menuInfo);
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            if (info.id == -1) return;
            Object o = getAdapter().getItem((int) info.id);
            if (o == null)
                return;

            //if (TextUtils.isEmpty(topic.getId())) return;
            final AppItem appItem = (AppItem) o;
            menu.add(R.string.associate_with_theme).setOnMenuItemClickListener(menuItem -> {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
                builder.title(R.string.enter_url_theme);

                final EditText input = new EditText(getContext());

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(appItem.getId());
                builder.customView(input, true);

                builder.positiveText(android.R.string.ok);
                builder.negativeText(android.R.string.cancel);
                builder.onPositive((dialog, which) -> {
                    String text = input.getText() == null ? "" : input.getText().toString();
                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(getContext(), R.string.empty_url, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Matcher m = Pattern.compile("showtopic=(\\d+)").matcher(text.trim());
                    if (!m.find()) {
                        m = Pattern.compile("(\\d+)").matcher(text.trim());
                        if (m.find()) {
                            if (m.group(1).length() != text.trim().length()) {
                                Toast.makeText(getContext(), R.string.incorrect_url, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.incorrect_url, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    ApplicationRelationsTable.addRealtion(appItem.getPackageName(), m.group(1));
                    appItem.setFindedState(AppItem.STATE_FINDED);
                    appItem.setId(m.group(1));
                    getAdapter().notifyDataSetChanged();
                });
                builder.onNegative((dialog, which) -> dialog.cancel());

                builder.show();
                return true;
            });
        } catch (Throwable ex) {
            AppLog.e(getContext(), ex);
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu.findItem(R.id.list_settings_item) != null)
            menu.findItem(R.id.list_settings_item).setVisible(false);
    }

    private AppItem findById(ArrayList<AppItem> apps, CharSequence id) {
        for (AppItem app : apps) {
            if (app.Ids.contains(id))
                return app;
        }
        return null;
    }
}
