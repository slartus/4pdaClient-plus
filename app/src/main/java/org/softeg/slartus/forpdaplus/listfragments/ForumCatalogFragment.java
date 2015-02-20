package org.softeg.slartus.forpdaplus.listfragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.Forum;
import org.softeg.slartus.forpdaapi.Forums;
import org.softeg.slartus.forpdaapi.ICatalogItem;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdaapi.classes.ForumsData;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.ForumsTable;
import org.softeg.slartus.forpdaplus.listfragments.adapters.CatalogAdapter;
import org.softeg.slartus.forpdaplus.listtemplates.ForumBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

/*
 * Created by slinkin on 21.02.14.
 */
public class ForumCatalogFragment extends BaseCatalogFragment {
    public static final String FORUM_ID_KEY = "KEY_FORUM_ID";
    public static final String FORUM_TITLE_KEY = "FORUM_TITLE_KEY";

    protected Forum m_StartForum = null;
    protected ArrayList<Forum> mData = new ArrayList<>();
    protected ArrayList<Forum> mLoadResultList;


    public static void showActivity(Context context, String forumId, String topicId) {
        Bundle args = new Bundle();
        if (!TextUtils.isEmpty(forumId))
            args.putString(ForumCatalogFragment.FORUM_ID_KEY, forumId);
        if (!TextUtils.isEmpty(topicId))
            args.putString(TopicsListFragment.KEY_TOPIC_ID, topicId);
        ListFragmentActivity.showListFragment(context, new ForumBrickInfo().getName(), args);
    }

    public ForumCatalogFragment() {
        super();
        m_CurrentCatalogItem = new Forum("-1", "4pda");
        m_LoadingCatalogItem = m_CurrentCatalogItem.clone();
    }

    private Boolean m_SavedInstance = false;

    /*
   Поддерживает ли загрузку состояния
    */
    protected Boolean isSavedInstanceStateEnabled() {
        return m_CurrentCatalogItem != null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            if (!getArguments().containsKey(FORUM_ID_KEY) && !getArguments().containsKey(TopicsListFragment.KEY_TOPIC_ID))
                return;

            String forumId = null;
            String forumTitle = null;
            if (getArguments().containsKey(FORUM_ID_KEY)) {
                forumId = getArguments().getString(FORUM_ID_KEY);
            }
            if (getArguments().containsKey(FORUM_TITLE_KEY))
                forumTitle = getArguments().getString(FORUM_TITLE_KEY);

            if (getArguments().containsKey(TopicsListFragment.KEY_TOPIC_ID) && forumId == null)
                m_StartForum = new Forum(null, getArguments().getString(TopicsListFragment.KEY_TOPIC_ID));
            else
                m_StartForum = new Forum(forumId, forumTitle);
        } else if (savedInstanceState != null) {
            //  m_SavedInstance = true;
            m_StartForum = savedInstanceState.getParcelable("StartForum");
            m_CurrentCatalogItem = (Forum) savedInstanceState.getParcelable("CurrentCatalogItem");
            m_LoadingCatalogItem = (Forum) savedInstanceState.getParcelable("LoadingCatalogItem");
            mData = savedInstanceState.getParcelableArrayList("Data");
        } else {
            m_StartForum = Preferences.List.getStartForum();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (m_CurrentCatalogItem != null)
            rebuildCrumbs(m_CurrentCatalogItem);
        super.onActivityCreated(savedInstanceState);

        afterDeliveryResult();


    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putParcelable("StartForum", m_StartForum);
        outState.putParcelable("CurrentCatalogItem", (Forum) m_CurrentCatalogItem);
        outState.putParcelable("LoadingCatalogItem", (Forum) m_LoadingCatalogItem);
        outState.putParcelableArrayList("Data", mData);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected boolean inBackground(boolean isRefresh, ICatalogItem catalogItem) throws IOException, ParseException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (m_SavedInstance) {
            mLoadResultList = mData;
            m_SavedInstance = false;
            return true;
        }
        if (m_CurrentCatalogItem != null && !isRefresh
                && catalogItem.getId().equals(m_CurrentCatalogItem.getId()))
            return false;
        if (m_StartForum != null && m_StartForum.getId() == null) {
            m_StartForum.setId(Client.getInstance().getThemeForumId(m_StartForum.getTitle()));
        }

        if (m_StartForum != null && m_StartForum.getId() != null) {
            catalogItem = ForumsTable.loadCrumbs(m_StartForum.getId(), true);
            m_StartForum = null;
            if (catalogItem != null) {
                m_LoadingCatalogItem = catalogItem.clone();
            }
        }

        mLoadResultList = ForumsTable.loadForums((Forum) catalogItem, true);
//        if (mLoadResultList.size() == 1 && mLoadResultList.get(0).getId().equals(catalogItem.getId())) {
//            if (mData.size() == 0) {
//                catalogItem = catalogItem.getParent().clone();
//
//                mLoadResultList = ForumsTable.loadForums((Forum) catalogItem, true);
//            }
//
//        }

        return mLoadResultList.size() != 0;
    }

    @Override
    protected void onFailureResult() {

        ForumTopicsListFragment.showForumTopicsList(getActivity(), m_LoadingCatalogItem.getId(), m_LoadingCatalogItem.getTitle());
        if (getAdapter().getCount() == 0) {
            deliveryResult(true);

            getListView().setSelectionFromTop(getCatalogIndexById(mData, m_LoadingCatalogItem), 0);
        }
        rebuildCrumbs(m_LoadingCatalogItem.getParent()==null?m_LoadingCatalogItem:m_LoadingCatalogItem.getParent());
    }

    protected BaseAdapter createAdapter() {
        return new CatalogAdapter(getContext(), mData);
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        super.deliveryResult(isRefresh);
        mData.clear();
        for (Forum item : mLoadResultList) {
            mData.add(item);
        }

        mLoadResultList.clear();
        getAdapter().notifyDataSetChanged();
    }

    private MenuItem m_SetFavorite;

    @Override
    public void onPrepareOptionsMenu(android.view.Menu menu) {
        if (m_SetFavorite != null) {
            m_SetFavorite.setChecked(false);

            if (m_CurrentCatalogItem != null && m_CurrentCatalogItem.getId() != null
                    && m_CurrentCatalogItem.getId().equals(Preferences.List.getStartForumId())) {
                m_SetFavorite.setChecked(true);
            } else {
                m_SetFavorite.setChecked(false);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        m_SetFavorite = menu.add("Задать этот форум стартовым")
                .setCheckable(true)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.isChecked()) {
                            menuItem.setChecked(false);
                            Preferences.List.setStartForum(null,
                                    null);
                            Toast.makeText(getContext(), "Стартовый форум не задан", Toast.LENGTH_SHORT).show();
                        } else {
                            menuItem.setChecked(true);
                            Preferences.List.setStartForum(m_CurrentCatalogItem.getId().toString(),
                                    m_CurrentCatalogItem.getTitle().toString());
                            Toast.makeText(getContext(), "Форум задан стартовым", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });


        menu.add("Отметить этот форум прочитанным")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (!Client.getInstance().getLogined()) {
                            Toast.makeText(getActivity(), "Необходимо залогиниться!", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        new AlertDialogBuilder(getActivity())
                                .setTitle("Подтвердите действие")
                                .setMessage("Отметить этот форум прочитанным?")
                                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        Toast.makeText(getActivity(), "Запрос отправлен", Toast.LENGTH_SHORT).show();
                                        new Thread(new Runnable() {
                                            public void run() {
                                                Throwable ex = null;
                                                try {
                                                    Forums.markForumAsRead(Client.getInstance(), m_CurrentCatalogItem.getId());

                                                } catch (Throwable e) {
                                                    ex = e;
                                                }

                                                final Throwable finalEx = ex;

                                                mHandler.post(new Runnable() {
                                                    public void run() {
                                                        try {
                                                            if (finalEx != null) {
                                                                Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
                                                                AppLog.e(getActivity(), finalEx);
                                                            } else {
                                                                Toast.makeText(getActivity(), "Форум отмечен прочитанным", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } catch (Exception ex) {
                                                            AppLog.e(getActivity(), ex);
                                                        }

                                                    }
                                                });
                                            }
                                        }).start();
                                    }
                                })
                                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .create()
                                .show();
                        return true;
                    }
                });
        menu.add("Обновить структуру форума")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        new UpdateForumStructTask(getActivity()).execute();
                        return true;
                    }
                });
        m_SetFavorite.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

    }

    private class UpdateForumStructTask extends AsyncTask<String, String, ForumsData> {

        private final ProgressDialog dialog;

        public UpdateForumStructTask(Context context) {
            dialog = new AppProgressDialog(context);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }

        protected void onCancelled() {
            Toast.makeText(getActivity(), "Обновление структуры форума отменено", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected ForumsData doInBackground(String... forums) {

            try {

                if (isCancelled()) return null;

                ForumsData res = Forums.loadForums(Client.getInstance(), new ProgressState() {
                    @Override
                    public void update(String message, int percents) {
                        publishProgress(String.format("%s %d", message, percents));
                    }
                });
                publishProgress("Обновление базы");
                ForumsTable.updateForums(res.getItems());
                return res;
            } catch (Throwable e) {
                ForumsData res = new ForumsData();
                res.setError(e);

                return res;
            }
        }

        @Override
        protected void onProgressUpdate(final String... progress) {
            mHandler.post(new Runnable() {
                public void run() {
                    dialog.setMessage(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Обновление структуры форума...");
                this.dialog.show();
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
        }


        protected void onPostExecute(final ForumsData data) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                AppLog.e(null, ex);
            }
            loadData(true);
            if (data != null) {
                if (data.getError() != null) {
                    AppLog.e(getActivity(), data.getError());
                }
            }
        }
    }
}
