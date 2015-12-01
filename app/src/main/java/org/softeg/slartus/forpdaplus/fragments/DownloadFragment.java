package org.softeg.slartus.forpdaplus.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdacommon.FileUtils;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.DownloadTask;
import org.softeg.slartus.forpdaplus.classes.DownloadTasks;
import org.softeg.slartus.forpdaplus.classes.common.Functions;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.BaseTable;
import org.softeg.slartus.forpdaplus.db.DbHelper;
import org.softeg.slartus.forpdaplus.db.DownloadsTable;
import org.softeg.slartus.forpdaplus.download.DownloadsService;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by radiationx on 16.11.15.
 */
public class DownloadFragment extends GeneralFragment implements AdapterView.OnItemClickListener {
    @Override
    public Menu getMenu() {
        return null;
    }

    @Override
    public boolean closeTab() {
        return false;
    }

    ListView m_ListView;
    private static android.os.Handler mHandler = new android.os.Handler();
    public static final String TEMPLATE = "DownloadsTab";
    public static final String TITLE = "Загрузки";
    private View m_ListFooter;
    private TextView txtLoadMoreThemes, txtPullToLoadMore;
    private ImageView imgPullToLoadMore;

    public static void newInstance(){
        MainActivity.addTab(TITLE, TEMPLATE, new DownloadFragment());
    }

    private DownloadTasks getDownloadTasks() {
        DownloadTasks downloadTasks = Client.getInstance().getDownloadTasks();

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getReadableDatabase();

            String selection = null;
            String[] selectionArgs = null;
            if (downloadTasks.size() > 0) {
                selection = DownloadsTable.COLUMN_ID + "<?";
                selectionArgs = new String[]{downloadTasks.get(downloadTasks.size() - 1).getId() + ""};
            }

            downloadTasks.setFullSize(BaseTable.getRowsCount(db, DownloadsTable.TABLE_NAME));

            assert db != null;
            c = db.query(DownloadsTable.TABLE_NAME, null, selection, selectionArgs, null, null, DownloadsTable.COLUMN_CREATEDATETIME + " DESC", "20");

            if (c.moveToFirst()) {
                int columnIdIndex = c.getColumnIndex(DownloadsTable.COLUMN_ID);
                int columnUrlIndex = c.getColumnIndex(DownloadsTable.COLUMN_URL);
                int columnFilePathIndex = c.getColumnIndex(DownloadsTable.COLUMN_FILEPATH);
                int columnContentLengthIndex = c.getColumnIndex(DownloadsTable.COLUMN_CONTENTLEGTH);
                int columnCreateDateIndex = c.getColumnIndex(DownloadsTable.COLUMN_CREATEDATETIME);
                int columnLastDateIndex = c.getColumnIndex(DownloadsTable.COLUMN_ENDDATETIME);
                int columnDonwloadFilePathIndex = c.getColumnIndex(DownloadsTable.COLUMN_DOWNLOADFILEPATH);
                int columnDonwloadedContentLengthIndex = c.getColumnIndex(DownloadsTable.COLUMN_DOWNLOADEDCONTENTLENGTH);
                int columnStateIndex = c.getColumnIndex(DownloadsTable.COLUMN_STATE);
                do {
                    String url = c.getString(columnUrlIndex);
                    String filePath = c.getString(columnFilePathIndex);
                    long content = c.getLong(columnContentLengthIndex);
                    int id = c.getInt(columnIdIndex);
                    if (downloadTasks.getById(id) != null) continue;
                    Date createDate = DbHelper.parseDate(c.getString(columnCreateDateIndex));

                    DownloadTask topic = new DownloadTask(url, id, createDate, content);
                    topic.setOutputFile(filePath);
                    topic.setStateChangedDate(DbHelper.parseDate(c.getString(columnLastDateIndex)));
                    topic.setDownloadingFilePath(c.getString(columnDonwloadFilePathIndex));
                    topic.setDownloadedSize(c.getLong(columnDonwloadedContentLengthIndex));
                    topic.setJustState(c.getInt(columnStateIndex));
                    if (topic.isActive())
                        topic.setJustState(DownloadTask.STATE_ERROR);
                    topic.calcPercents();
                    downloadTasks.add(topic);
                } while (c.moveToNext());

            }
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }
        updateDataInfo();
        return downloadTasks;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.downloads_list_activity, container, false);


        m_ListView = (ListView) view.findViewById(R.id.lstTree);
        m_ListView.addFooterView(createListFooter(inflater));

        m_Adapter = new DownloadTasksAdapter(getContext(), R.layout.download_task_item, getDownloadTasks());

        getListView().setAdapter(m_Adapter);
        getListView().setOnItemClickListener(this);
        Client.getInstance().getDownloadTasks().setOnStateListener(new Client.OnProgressPositionChangedListener() {
            public void onProgressChanged(Context context, DownloadTask downloadTask, Exception ex) {
                mHandler.post(new Runnable() {
                    public void run() {
                        m_Adapter.notifyDataSetChanged();
                    }
                });

            }
        });
        return view;
    }


    private View createListFooter(LayoutInflater inflater) {
        m_ListFooter = inflater.inflate(R.layout.list_footer, null);

        m_ListFooter.setVisibility(View.GONE);
        m_ListFooter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Client.getInstance().getDownloadTasks().getFullLength() > Client.getInstance().getDownloadTasks().size())
                    loadMore();
            }
        });
        txtLoadMoreThemes = (TextView) m_ListFooter.findViewById(R.id.txtLoadMoreThemes1);
        txtPullToLoadMore = (TextView) m_ListFooter.findViewById(R.id.txtPullToLoadMore1);
        imgPullToLoadMore = (ImageView) m_ListFooter.findViewById(R.id.imgPullToLoadMore1);
        ProgressBar m_ProgressBar = (ProgressBar) m_ListFooter.findViewById(R.id.load_more_progress1);
        m_ProgressBar.setVisibility(View.GONE);
        return m_ListFooter;
    }

    private void updateDataInfo() {

        int loadMoreVisibility = (Client.getInstance().getDownloadTasks().getFullLength() > Client.getInstance().getDownloadTasks().size()) ? View.VISIBLE : View.GONE;
        txtPullToLoadMore.setVisibility(loadMoreVisibility);
        imgPullToLoadMore.setVisibility(loadMoreVisibility);
        txtLoadMoreThemes.setText("Всего: " + Client.getInstance().getDownloadTasks().getFullLength());

        m_ListFooter.setVisibility(Client.getInstance().getDownloadTasks().size() > 0 ? View.VISIBLE : View.GONE);
    }

    private void loadMore() {
        getDownloadTasks();
        m_Adapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {
            if (l < 0) return;
            CharSequence[] items;
            final DownloadTask downloadTask = m_Adapter.getItem((int) l);
            switch (downloadTask.getState()) {
                case DownloadTask.STATE_PENDING:
                case DownloadTask.STATE_CONNECTING:
                case DownloadTask.STATE_DOWNLOADING:
                    new MaterialDialog.Builder(getContext())
                            .title("Действие")
                            .content("Отменить загрузку?")
                            .cancelable(true)
                            .positiveText("Да")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    downloadTask.cancel();
                                }
                            })
                            .negativeText("Нет")
                            .show();
                    break;
                case DownloadTask.STATE_ERROR:
                case DownloadTask.STATE_CANCELED:
                    items = new CharSequence[]{"Повторить загрузку", "Докачать файл"};
                    new MaterialDialog.Builder(getContext())
                            .title("Выберите действие")
                            .items(items)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int i, CharSequence items) {
                                    switch (i) {
                                        case 0: // Повторить загрузку
                                            Client.getInstance().getDownloadTasks().remove(downloadTask);
                                            DownloadsService.download(getActivity(), downloadTask.getUrl(), false);

                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    m_Adapter.notifyDataSetChanged();
                                                }
                                            });
                                            break;
                                        case 1: // Докачать файл
                                            Client.getInstance().getDownloadTasks().remove(downloadTask);
                                            DownloadsService.download(getActivity(), downloadTask.getUrl(),
                                                    downloadTask.getDownloadingFilePath(), downloadTask.getId(),false);

                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    m_Adapter.notifyDataSetChanged();
                                                }
                                            });

                                            break;
                                    }
                                }
                            })
                            .cancelable(true)
                            .negativeText("Отмена")
                            .show();
                    break;
                case DownloadTask.STATE_SUCCESSFULL:
                    items = new CharSequence[]{"Запустить файл", "Повторить загрузку"};
                    new MaterialDialog.Builder(getContext())
                            .title("Выберите действие")
                            .items(items)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int i, CharSequence items) {
                                    switch (i) {
                                        case 0: // Запустить файл
                                            runFile(downloadTask.getOutputFile());
                                            break;
                                        case 1: // Повторить загрузку
                                            //Client.getInstance().getDownloadTasks().remove(downloadTask);
                                            DownloadsService.download(getActivity(), downloadTask.getUrl(),false);

                                            mHandler.post(new Runnable() {
                                                public void run() {
                                                    m_Adapter.notifyDataSetChanged();
                                                }
                                            });
                                            break;
                                    }
                                }
                            })
                            .cancelable(true)
                            .negativeText("Отмена")
                            .show();


                    break;
            }
        } catch (Exception ex) {
            AppLog.e(getContext(), ex);
        }
    }

    private void runFile(String filePath) {
        try {

            getContext().startActivity(getRunFileIntent(filePath));
        } catch (ActivityNotFoundException e) {
            AppLog.e(getContext(), new NotReportException("Не найдено сопоставление для типа файла!"));
        }
    }

    private static Intent getRunFileIntent(String filePath) {
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = myMime.getMimeTypeFromExtension(FileUtils.fileExt(filePath).substring(1));
        newIntent.setDataAndType(Uri.parse("file://" + filePath), mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return newIntent;
    }

    public Boolean onParentBackPressed() {
        return false;
    }
    

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add("Очистить").setIcon(R.drawable.ic_delete_white_24dp);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                new MaterialDialog.Builder(getContext())
                        .title("Подтвердите действие")
                        .content("Удалить все неактивные загрузки?")
                        .positiveText("Удалить")
                        .negativeText("Отмена")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                clearNotActiveDownloads();
                            }
                        })
                        .show();

                return true;
            }
        });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    public void refresh() {

    }

    public void clearNotActiveDownloads() {
        DownloadTasks downloadTasks = Client.getInstance().getDownloadTasks();

        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            DbHelper dbHelper = new DbHelper(App.getInstance());
            db = dbHelper.getWritableDatabase();

            assert db != null;
            c = db.query(DownloadsTable.TABLE_NAME, null, null, null, null, null, null);

            if (c.moveToFirst()) {
                int columnIdIndex = c.getColumnIndex(DownloadsTable.COLUMN_ID);

                do {
                    int id = c.getInt(columnIdIndex);
                    DownloadTask downloadTask = downloadTasks.getById(id);
                    if (downloadTask != null && downloadTask.isActive()) continue;

                    db.delete(DownloadsTable.TABLE_NAME, DownloadsTable.COLUMN_ID + "=?", new String[]{Integer.toString(id)});
                    downloadTasks.remove(downloadTask);
                } while (c.moveToNext());

            }
        } catch (Exception ex) {
            AppLog.e(App.getInstance(), ex);
        } finally {
            if (db != null) {
                if (c != null)
                    c.close();
                db.close();
            }
        }
        loadMore();
    }

    public Boolean refreshed() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ListView getListView() {
        return m_ListView;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {

    }

    private ArrayAdapter<DownloadTask> m_Adapter;
    

    public class DownloadTasksAdapter extends ArrayAdapter<DownloadTask> {
        private LayoutInflater m_Inflater;

        public DownloadTasksAdapter(Context context, int textViewResourceId, ArrayList<DownloadTask> objects) {
            super(context, textViewResourceId, objects);
            m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;


            if (convertView == null) {


                convertView = m_Inflater.inflate(R.layout.download_task_item, parent, false);


                holder = new ViewHolder();
                assert convertView != null;
                holder.txtFileName = (TextView) convertView
                        .findViewById(R.id.txtFileName);
                holder.txtDescription = (TextView) convertView
                        .findViewById(R.id.txtDescription);
                holder.txtResult = (TextView) convertView
                        .findViewById(R.id.txtResult);
                holder.progress = (ProgressBar) convertView
                        .findViewById(R.id.progress);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DownloadTask downloadTask = this.getItem(position);
            try {
                holder.txtFileName.setText(downloadTask.getFileName());
            } catch (Throwable ex) {
                holder.txtFileName.setText(ex.toString());
            }

            holder.txtDescription.setText("Загружено " + downloadTask.getPercents() + "%("
                    + Functions.getSizeText(downloadTask.getDownloadedSize()) + "/"
                    + Functions.getSizeText(downloadTask.getM_ContentLength()) + ")");
            int state = downloadTask.getState();

            Boolean processing = state == DownloadTask.STATE_CONNECTING || state == DownloadTask.STATE_DOWNLOADING
                    || state == DownloadTask.STATE_PENDING;
            holder.progress.setIndeterminate(state == DownloadTask.STATE_CONNECTING);
            holder.progress.setProgress(downloadTask.getPercents());

            holder.progress.setVisibility(processing ? View.VISIBLE : View.GONE);
            holder.txtResult.setVisibility(processing ? View.GONE : View.VISIBLE);
            holder.txtResult.setText(downloadTask.getStateMessage());

            return convertView;
        }

        public class ViewHolder {
            //  TextView txtPostsCount;
            TextView txtFileName;
            TextView txtDescription;
            TextView txtResult;
            ProgressBar progress;
        }

    }

}
