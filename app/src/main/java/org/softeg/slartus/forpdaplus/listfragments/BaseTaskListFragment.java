package org.softeg.slartus.forpdaplus.listfragments;


import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Filterable;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.CacheDbHelper;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.sqliteannotations.BaseDao;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by slartus on 19.02.14.
 */
public abstract class BaseTaskListFragment extends BaseListFragment {

    protected ArrayList<? extends IListItem> mLoadResultList;
    protected ArrayList<IListItem> mCacheList = new ArrayList<>();

    public BaseTaskListFragment() {
        super();
    }

    public void filter(CharSequence text) {
        if (getAdapter() instanceof Filterable) {
            ((Filterable) getAdapter()).getFilter().filter(text);
        }
    }

    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {

    }

    public void saveCache() throws Exception {

    }

    protected <T> void updateItemCache(final T item, final Class<T> tClass, Boolean newThread) {
        if (newThread) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateItemCache(item, tClass);
                }
            }).start();
        } else {
            updateItemCache(item, tClass);
        }
    }

    private <T> void updateItemCache(T item, Class<T> tClass) {
        try {
            CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
            SQLiteDatabase db = null;
            try {
                db = cacheDbHelper.getWritableDatabase();
                BaseDao<T> baseDao = new BaseDao<>(App.getContext(), db, getListName(), tClass);

                baseDao.update(item, ((IListItem) item).getId().toString());

            } finally {
                if (db != null)
                    db.close();
            }
        } catch (Throwable ex) {
            // Log.e(getContext(), ex);
        }
    }

    public void trySaveCache() {
        try {
            saveCache();
        } catch (Throwable e) {
            AppLog.toastE(getContext(), e);
        }
    }

    /*
    Поддерживает ли загрузку состояния
     */
    protected Boolean isSavedInstanceStateEnabled() {
        return false;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // если не поддерживает загрузку состояния, то грузим по новой
        if (!isSavedInstanceStateEnabled() || savedInstanceState == null) {
            mCacheTask = new LoadCacheTask();
            mCacheTask.execute();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCacheTask != null)
            mCacheTask.cancel(false);
        if (mTask != null)
            mTask.cancel(null);
    }

    private Task mTask = null;
    private LoadCacheTask mCacheTask = null;


    public void startLoad() {

        if (mTask != null)
            return;
        loadData(true);
    }

    protected Task createTask(Boolean isRefresh) {
        return new Task(isRefresh);
    }

    public void loadData(final boolean isRefresh) {

        saveListViewScrollPosition();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (needLogin()) {
                    Client.getInstance().checkLoginByCookies();
                    if (!Client.getInstance().getLogined())
                        Client.getInstance().showLoginForm(getContext(), new Client.OnUserChangedListener() {
                            public void onUserChanged(String user, Boolean success) {
                                loadData(isRefresh);
                            }
                        });
                }

                mTask = createTask(isRefresh);
                mTask.execute();
            }
        };
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED)
            mTask.cancel(runnable);
        else {
            runnable.run();
        }
    }


    protected abstract boolean inBackground(boolean isRefresh) throws Throwable;

    protected abstract void deliveryResult(boolean isRefresh);

    private void beforeDeliveryResult() {
        saveListViewScrollPosition();
    }

    protected void afterDeliveryResult() {
        setCount();
        setListShown(true);
        mAdapter.notifyDataSetChanged();
        setEmptyText("Нет данных");
        new Thread(new Runnable() {
            @Override
            public void run() {
                trySaveCache();
            }
        }).start();

        restoreListViewScrollPosition();
    }

    protected void onFailureResult() {

    }

    protected Boolean isCancelled() {
        return mTask.isCancelled();
    }


    public class Task extends AsyncTask<Boolean, Void, Boolean> {
        protected Boolean mRefresh;
        private Runnable onCancelAction;
        protected Throwable mEx;

        public Task(Boolean refresh) {
            mRefresh = refresh;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setLoading(true);
        }

        public void cancel(Runnable runnable) {
            onCancelAction = runnable;

            cancel(false);
        }

        @Override
        protected Boolean doInBackground(Boolean[] p1) {
            try {
                return inBackground(mRefresh);
            } catch (Throwable e) {
                mEx = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result && !isCancelled()) {
                beforeDeliveryResult();
                deliveryResult(mRefresh);
                afterDeliveryResult();
            }
            if (!isCancelled())
                setLoading(false);
            if (mEx != null)
                AppLog.e(getActivity(), mEx, new Runnable() {
                    @Override
                    public void run() {
                        loadData(mRefresh);
                    }
                });
            else if (!result) {
                onFailureResult();
            }
        }

        @Override
        protected void onCancelled(Boolean result) {
            if (onCancelAction != null)
                onCancelAction.run();
        }

        @Override
        protected void onCancelled() {
            if (onCancelAction != null)
                onCancelAction.run();
        }
    }

    public class LoadCacheTask extends AsyncTask<Boolean, Void, Boolean> {
        private Throwable mEx;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setLoading(true);
        }

        public void cancel() {
            cancel(false);
        }

        @Override
        protected Boolean doInBackground(Boolean[] p1) {
            try {
                loadCache();
                return true;
            } catch (Throwable e) {
                mEx = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (mEx != null)
                Toast.makeText(getContext(), AppLog.getLocalizedMessage(mEx, "Ошибка загрузки кешированного списка"), Toast.LENGTH_SHORT).show();
            if (!isCancelled()) {
                deliveryCache();
                restoreListViewScrollPosition();
                if (mData.size() == 0 || Preferences.Lists.isRefresh())
                    startLoad();
                else {
                    setLoading(false);
                }
            }
        }

        @Override
        protected void onCancelled(Boolean result) {
            setLoading(false);
        }

        @Override
        protected void onCancelled() {
            setLoading(false);
        }

    }

    protected void deliveryCache() {
        mData.clear();
        if (mCacheList != null) {
            mData.addAll(mCacheList);
            mCacheList.clear();
        }
        setCount();
        mAdapter.notifyDataSetChanged();
    }


}
