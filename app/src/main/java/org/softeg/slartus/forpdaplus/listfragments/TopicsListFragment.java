package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdacommon.ActionSelectDialogFragment;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.TopicListItemTask;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter;
import org.softeg.slartus.forpdaplus.db.CacheDbHelper;
import org.softeg.slartus.forpdaplus.fragments.topic.EditPostFragment;
import org.softeg.slartus.forpdaplus.listfragments.adapters.SortedListAdapter;
import org.softeg.slartus.forpdaplus.listfragments.next.ForumFragment;
import org.softeg.slartus.forpdaplus.listtemplates.FavoritesBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.prefs.TopicsListPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.TopicsPreferenceFragment;
import org.softeg.sqliteannotations.BaseDao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Created by slartus on 20.02.14.
 */
public abstract class TopicsListFragment extends BaseTaskListFragment {
    public static final String KEY_TOPIC_ID = "KEY_TOPIC_ID";

    public TopicsListFragment() {
        super();
    }

    protected ListInfo mListInfo = new ListInfo();

    protected abstract ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException, URISyntaxException;

    @Override
    public void saveCache() throws Exception {
        CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getWritableDatabase();
            BaseDao<Topic> baseDao = new BaseDao<>(App.getContext(), db, getListName(), Topic.class);
            baseDao.createTable(db);
            for (IListItem item : mData) {
                Topic topic = (Topic) item;
                baseDao.insert(topic);
            }

        } finally {
            if (db != null)
                db.close();
        }
    }

    @Override
    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {
        mCacheList.clear();
        CacheDbHelper cacheDbHelper = new CacheDbHelper(App.getContext());
        SQLiteDatabase db = null;
        try {
            db = cacheDbHelper.getReadableDatabase();
            BaseDao<Topic> baseDao = new BaseDao<>(App.getContext(), db, getListName(), Topic.class);
            if (baseDao.isTableExists())
                mCacheList.addAll(baseDao.getAll());
        } finally {
            if (db != null)
                db.close();
        }
        sort();
    }

    @Override
    public boolean inBackground(boolean isRefresh) throws IOException, ParseException, URISyntaxException {
        mListInfo = new ListInfo();
        mListInfo.setFrom(isRefresh ? 0 : mData.size());
        mLoadResultList = loadTopics(Client.getInstance(), mListInfo);
        return true;
    }


    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh)
            mData.clear();
        List<CharSequence> ids = new ArrayList<>();
        for (IListItem item : mData) {
            ids.add(item.getId());
        }
        for (IListItem item : mLoadResultList) {
            if (ids.contains(item.getId()))
                continue;
            mData.add(item);
        }

        mLoadResultList.clear();

        sort();
    }

    protected void sort() {
        Collections.sort(mData, getComparator());
    }

    @Override
    public void setCount() {
        int count = Math.max(mListInfo.getOutCount(), mData.size());
        mListViewLoadMoreFooter.setCount(mData.size(), count);
        mListViewLoadMoreFooter.setState(
                mData.size() == count ? ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED :
                        ListViewLoadMoreFooter.STATE_LOAD_MORE
        );
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        try {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            if (info.id == -1) return;
            Object o = getAdapter().getItem((int) info.id);
            if (o == null)
                return;
            final IListItem topic = (IListItem) o;
            if (TextUtils.isEmpty(topic.getId())) return;

            menu.add(getContext().getString(R.string.navigate_getfirstpost)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_FIRST_POST, "");
                    return true;
                }
            });
            menu.add(getContext().getString(R.string.navigate_getlastpost)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_LAST_POST, "view=getlastpost");
                    return true;
                }
            });
            menu.add(getContext().getString(R.string.navigate_getnewpost)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_NEW_POST, "view=getnewpost");
                    return true;
                }
            });
            menu.add(getContext().getString(R.string.navigate_last_url)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_LAST_URL,
                            TopicUtils.getUrlArgs(topic.getId(), Topic.NAVIGATE_VIEW_LAST_URL.toString(), ""));
                    return true;
                }
            });
            menu.add(getContext().getString(R.string.NotesByTopic)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Bundle args = new Bundle();
                    args.putString(NotesListFragment.TOPIC_ID_KEY, topic.getId().toString());
                    MainActivity.showListFragment(topic.getId().toString(), new NotesBrickInfo().getName(), args);
                    return true;
                }
            });
            ExtUrl.addUrlSubMenu(mHandler, getContext(), menu,
                    TopicUtils.getTopicUrl(topic.getId().toString(), TopicUtils.getOpenTopicArgs(topic.getId(), getListName())), topic.getId().toString(),
                    topic.getMain().toString());
            addOptionsMenu(getContext(), mHandler, menu, topic, null);
        } catch (Exception ex) {
            AppLog.e(this.getContext(), ex);
        }
    }

    public SubMenu addOptionsMenu(final Context context, final Handler mHandler, Menu menu, final IListItem topic,
                                  final String shareItUrl) {
        SubMenu optionsMenu = menu.addSubMenu("Опции").setIcon(R.drawable.ic_menu_more);

        configureOptionsMenu(context, mHandler, optionsMenu, topic, shareItUrl);
        return optionsMenu;
    }

    public void configureOptionsMenu(final Context context, final Handler mHandler, SubMenu optionsMenu, final IListItem listItem,
                                     final String shareItUrl) {
        optionsMenu.clear();
        Topic topic = null;
        if (listItem instanceof Topic) {
            topic = (Topic) listItem;
        } else {
            topic = new Topic(listItem.getId().toString(), listItem.getMain().toString());
        }
        if (Client.getInstance().getLogined() && !topic.isInProgress()) {

            Boolean isFavotitesList = FavoritesBrickInfo.NAME.equals(getListName());
            String title = isFavotitesList ? "Изменить подписку" : "Добавить в избранное";
            final Topic finalTopic = topic;
            optionsMenu.add(title).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {

                    TopicUtils.showSubscribeSelectTypeDialog(context, mHandler, finalTopic,
                            new TopicListItemTask(context, (Topic) finalTopic, mAdapter) {
                                @Override
                                public String doInBackground(Topic topic, String... pars) throws Throwable {
                                    return TopicApi.changeFavorite(Client.getInstance(), topic.getId(), pars[0]);
                                }

                                @Override
                                public void onPostExecute(Topic topic) {

                                }
                            }
                    );
                    return true;
                }
            });

            if (isFavotitesList) {
                final Topic finalTopic1 = topic;
                optionsMenu.add(context.getString(R.string.DeleteFromFavorites)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Toast.makeText(context, "Запрос на удаление отправлен", Toast.LENGTH_SHORT).show();
                        new TopicListItemTask(context, (Topic) finalTopic1, mAdapter) {
                            @Override
                            public String doInBackground(Topic topic, String... pars) throws ParseException, IOException, URISyntaxException {
                                return TopicApi.deleteFromFavorites(Client.getInstance(), topic.getId());
                            }

                            @Override
                            public void onPostExecute(Topic topic) {
                                mData.remove(topic);
                            }
                        }.execute();

                        return true;
                    }
                });

                FavTopic favTopic = (FavTopic) topic;
                if (!favTopic.isPinned()) {
                    final Topic finalTopic2 = topic;
                    optionsMenu.add("\"Закрепить\" в избранном")
                            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    Toast.makeText(context, "Запрос на \"закрепить\" отправлен", Toast.LENGTH_SHORT).show();
                                    new TopicListItemTask(context, (Topic) finalTopic2, mAdapter) {
                                        @Override
                                        public String doInBackground(Topic topic, String... pars)
                                                throws ParseException, IOException, URISyntaxException {
                                            return TopicApi.pinFavorite(Client.getInstance(), topic.getId(), TopicApi.TRACK_TYPE_PIN);
                                        }

                                        @Override
                                        public void onPostExecute(Topic topic) {
                                            ((FavTopic) topic).setPinned(true);
                                        }
                                    }.execute();

                                    return true;
                                }
                            });
                } else {
                    final Topic finalTopic3 = topic;
                    optionsMenu.add("\"Открепить\" в избранном")
                            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem menuItem) {

                                    Toast.makeText(context, "Запрос на \"открепить\" отправлен", Toast.LENGTH_SHORT)
                                            .show();
                                    new TopicListItemTask(context, (Topic) finalTopic3, mAdapter) {
                                        @Override
                                        public String doInBackground(Topic topic, String... pars)
                                                throws ParseException, IOException, URISyntaxException {
                                            return TopicApi.pinFavorite(Client.getInstance(), topic.getId(), TopicApi.TRACK_TYPE_UNPIN);
                                        }

                                        @Override
                                        public void onPostExecute(Topic topic) {
                                            ((FavTopic) topic).setPinned(false);
                                        }
                                    }.execute();

                                    return true;
                                }
                            });
                }
            }
            optionsMenu.add(context.getString(R.string.OpenTopicForum)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ForumFragment.showActivity(context, null, listItem.getId().toString());
                    return true;
                }
            });

        }

        optionsMenu.add("Вложения").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                TopicAttachmentListFragment.showActivity(context, listItem.getId());
                return true;
            }
        });

        optionsMenu.add(context.getString(R.string.Share)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {

                try {
                    Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
                    sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, listItem.getMain());
                    sendMailIntent.putExtra(Intent.EXTRA_TEXT, TextUtils.isEmpty(shareItUrl) ?
                            ("http://4pda.ru/forum/index.php?showtopic=" + listItem.getId()) : shareItUrl);
                    sendMailIntent.setType("text/plain");

                    context.startActivity(Intent.createChooser(sendMailIntent, context.getString(R.string.Share)));
                } catch (Exception ex) {
                    return false;
                }
                return true;
            }
        });
    }

    private void showTopicActivity(IListItem topic, String args) {
        ExtTopic.showActivity(getActivity(), topic.getId(), args);
        topicAfterClick(topic);

    }

    private void showSaveNavigateActionDialog(final IListItem topic, final CharSequence selectedAction, final String params) {
        ActionSelectDialogFragment.showSaveNavigateActionDialog(getContext(),
                String.format("%s.navigate_action", getListName()),
                selectedAction.toString(),
                new Runnable() {
                    @Override
                    public void run() {
                        showTopicActivity(topic, params);
                    }
                }
        );
    }

    /**
     * Извне создание поста
     */
    public Boolean tryCreatePost(IListItem topic) {
        Bundle extras = getArguments();
        if (extras == null)
            return false;
        if (!extras.containsKey(Intent.EXTRA_STREAM) &&
                !extras.containsKey(Intent.EXTRA_TEXT) &&
                !extras.containsKey(Intent.EXTRA_HTML_TEXT)) return false;

        EditPostFragment.newPostWithAttach(getContext(),
                null, topic.getId().toString(), Client.getInstance().getAuthKey(), extras);
        getActivity().finish();
        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        try {
            id = org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge.getItemId(getActivity(), position, id);
            if (id < 0 || getAdapter().getCount() <= id) return;

            Object o = getAdapter().getItem((int) id);
            if (o == null)
                return;
            final IListItem topic = (IListItem) o;
            if (TextUtils.isEmpty(topic.getId())) return;
            if (tryCreatePost(topic))
                return;
            ActionSelectDialogFragment.execute(getActivity(),
                    "Действие по умолчанию",
                    String.format("%s.navigate_action", getListName()),
                    new CharSequence[]{getString(R.string.navigate_getfirstpost), getString(R.string.navigate_getlastpost), getString(R.string.navigate_getnewpost), getString(R.string.navigate_last_url)},
                    new CharSequence[]{Topic.NAVIGATE_VIEW_FIRST_POST, Topic.NAVIGATE_VIEW_LAST_POST, Topic.NAVIGATE_VIEW_NEW_POST, Topic.NAVIGATE_VIEW_LAST_URL},
                    new ActionSelectDialogFragment.OkListener() {
                        @Override
                        public void execute(CharSequence value) {
                            showTopicActivity(topic, TopicUtils.getUrlArgs(topic.getId(), value.toString(), Topic.NAVIGATE_VIEW_FIRST_POST.toString()));
                        }
                    }, "Вы можете изменить действие по умолчанию долгим тапом по теме"
            );


        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    private void topicAfterClick(IListItem topic) {
        topic.setState(IListItem.STATE_NORMAL);
        getAdapter().notifyDataSetChanged();

        updateItem(topic);

    }

    protected void updateItem(IListItem topic) {
        if (topic != null)
            updateItemCache((Topic) topic, Topic.class, true);
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new SortedListAdapter(getActivity(), mData, getPreferences().getBoolean("showSubMain", false));
    }

    private Comparator<? super IListItem> getComparator() {
        return new Comparator<IListItem>() {
            @Override
            public int compare(IListItem listItem1, IListItem listItem2) {
                if (!(listItem1 instanceof Topic))
                    return 0;
                int i;
                switch (Preferences.List.getListSort(getListName(), Preferences.List.defaultListSort())) {
                    case "sortorder.desc":
                        return compareBySortOrder((Topic) listItem1, (Topic) listItem2, -1);
                    case "sortorder.asc":
                        return compareBySortOrder((Topic) listItem1, (Topic) listItem2, 1);
                    case "date.desc":
                        return compareByDate((Topic) listItem1, (Topic) listItem2, -1);
                    case "date_and_new.desc":
                        i = compareByNew((Topic) listItem1, (Topic) listItem2, -1);
                        return i == 0 ? compareByDate((Topic) listItem1, (Topic) listItem2, -1) : i;
                    case "title.desc":
                        return compareByTitle((Topic) listItem1, (Topic) listItem2, -1);
                    case "date.asc":
                        return compareByDate((Topic) listItem1, (Topic) listItem2, 1);
                    case "date_and_new.asc":
                        i = compareByNew((Topic) listItem1, (Topic) listItem2, 1);
                        return i == 0 ? compareByDate((Topic) listItem1, (Topic) listItem2, 1) : i;
                    case "title.asc":
                        return compareByTitle((Topic) listItem1, (Topic) listItem2, 1);

                    default:
                        return compareByDate((Topic) listItem1, (Topic) listItem2, -1);
                }
            }
        };
    }


    private int compareBySortOrder(Topic listItem1, Topic listItem2, int k) {
        if (TextUtils.isEmpty(listItem1.getSortOrder()) && TextUtils.isEmpty(listItem2.getSortOrder()))
            return 0;
        if (TextUtils.isEmpty(listItem1.getSortOrder()))
            return k;
        if (TextUtils.isEmpty(listItem2.getSortOrder()))
            return -k;
        return k * listItem1.getSortOrder().toString().compareTo(listItem2.getSortOrder().toString());
    }

    private int compareByDate(Topic listItem1, Topic listItem2, int k) {
        if (listItem1.getLastMessageDate() == null && listItem2.getLastMessageDate() == null)
            return 0;
        if (listItem1.getLastMessageDate() == null)
            return k;
        return k * (listItem1.getLastMessageDate().after(listItem2.getLastMessageDate()) ? 1 : -1);
    }

    private int compareByNew(Topic listItem1, Topic listItem2, int k) {
        if (listItem1.getState() == listItem2.getState())
            return 0;
        if (listItem1.getState() == Topic.FLAG_NEW)
            return k;
        return -k;
    }

    private int compareByTitle(Topic listItem1, Topic listItem2, int k) {
        if (listItem1.getTitle() == null && listItem2.getTitle() == null)
            return 0;
        if (listItem1.getTitle() == null)
            return k;
        return k * (listItem1.getTitle().compareTo(listItem2.getTitle()));
    }


    protected void showSettings() {
        Intent settingsActivity = new Intent(
                getContext(), TopicsListPreferencesActivity.class);
        //settingsActivity.putExtra("LIST_NAME",getListName());
        TopicsPreferenceFragment.ListName = getListName();
        getContext().startActivity(settingsActivity);
    }

    final static int settingItemId = 537;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0,settingItemId,0,"Настройки списка")
                .setIcon(R.drawable.ic_settings_white_24dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showSettings();
                        return true;
                    }
                }).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
}
