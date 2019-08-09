package org.softeg.slartus.forpdaplus.listfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.FavTopic;
import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ActionSelectDialogFragment;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.TopicListItemTask;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.controls.ListViewLoadMoreFooter;
import org.softeg.slartus.forpdaplus.fragments.topic.editpost.EditPostFragment;
import org.softeg.slartus.forpdaplus.listfragments.adapters.SortedListAdapter;
import org.softeg.slartus.forpdaplus.listfragments.next.forum.ForumFragment;
import org.softeg.slartus.forpdaplus.listtemplates.FavoritesBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.prefs.TopicsListPreferencesActivity;
import org.softeg.slartus.forpdaplus.prefs.TopicsPreferenceFragment;
import org.softeg.slartus.forpdaplus.repositories.UserInfoRepository;

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
    public void loadCache() {
        clearNotification(2);
        super.loadCache();
        sort();
    }

    @Override
    public boolean inBackground(boolean isRefresh) throws IOException, ParseException, URISyntaxException {
        mListInfo = new ListInfo();
        mListInfo.setFrom(isRefresh ? 0 : getMData().size());
        mLoadResultList = loadTopics(Client.getInstance(), mListInfo);
        return true;
    }


    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh)
            getMData().clear();
        List<CharSequence> ids = new ArrayList<>();
        for (IListItem item : getMData()) {
            ids.add(item.getId());
        }
        for (IListItem item : mLoadResultList) {
            if (ids.contains(item.getId()))
                continue;
            getMData().add(item);
        }

        mLoadResultList.clear();

        sort();
    }

    protected void sort() {
        Collections.sort(getMData(), getComparator());
    }

    @Override
    public void setCount() {
        int count = Math.max(mListInfo.getOutCount(), getMData().size());
        getMListViewLoadMoreFooter().setCount(getMData().size(), count);
        getMListViewLoadMoreFooter().setState(
                getMData().size() == count ? ListViewLoadMoreFooter.STATE_FULL_DOWNLOADED :
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

            final List<MenuListDialog> list = new ArrayList<>();

            list.add(new MenuListDialog(getContext().getString(R.string.navigate_getfirstpost), () -> showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_FIRST_POST, "")));
            list.add(new MenuListDialog(getContext().getString(R.string.navigate_getlastpost), () -> showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_LAST_POST, "view=getlastpost")));
            list.add(new MenuListDialog(getContext().getString(R.string.navigate_getnewpost), () -> showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_NEW_POST, "view=getnewpost")));
            list.add(new MenuListDialog(getContext().getString(R.string.navigate_last_url), () -> showSaveNavigateActionDialog(topic, Topic.NAVIGATE_VIEW_LAST_URL,
                    TopicUtils.getUrlArgs(topic.getId(), Topic.NAVIGATE_VIEW_LAST_URL.toString(), ""))));
            list.add(new MenuListDialog(getContext().getString(R.string.NotesByTopic), () -> {
                Bundle args = new Bundle();
                args.putString(NotesListFragment.TOPIC_ID_KEY, topic.getId().toString());
                MainActivity.showListFragment(topic.getId().toString(), new NotesBrickInfo().getName(), args);
            }));
            list.add(new MenuListDialog(getString(R.string.link), () -> showLinkMenu(getContext(), topic)));
            list.add(new MenuListDialog(getString(R.string.options), () -> showOptionsMenu(getContext(), getMHandler(), topic, null)));

            ExtUrl.showContextDialog(getContext(), null, list);
        } catch (Exception ex) {
            AppLog.e(this.getContext(), ex);
        }
    }

    public void showLinkMenu(final Context context, final IListItem topic) {
        final List<MenuListDialog> list = new ArrayList<>();
        ExtUrl.addUrlSubMenu(getMHandler(), context, list,
                TopicUtils.getTopicUrl(topic.getId().toString(), TopicUtils.getOpenTopicArgs(topic.getId(), getListName())), topic.getId().toString(),
                topic.getMain().toString());
        ExtUrl.showContextDialog(context, getString(R.string.link), list);
    }

    public void showOptionsMenu(final Context context, final Handler mHandler, final IListItem topic, final String shareItUrl) {
        final List<MenuListDialog> list = new ArrayList<>();
        configureOptionsMenu(context, mHandler, list, topic, shareItUrl);
        ExtUrl.showContextDialog(context, getString(R.string.options), list);
    }

    public void configureOptionsMenu(final Context context, final Handler mHandler, List<MenuListDialog> optionsMenu, final IListItem listItem,
                                     final String shareItUrl) {
        final Topic topic;
        if (listItem instanceof Topic) {
            topic = (Topic) listItem;
        } else {
            topic = new Topic(listItem.getId().toString(), listItem.getMain().toString());
        }
        if (Client.getInstance().getLogined() && !topic.isInProgress()) {

            Boolean isFavotitesList = FavoritesBrickInfo.NAME.equals(getListName());
            String title = isFavotitesList ? context.getString(R.string.change_subscription) : context.getString(R.string.add_to_favorite);

            optionsMenu.add(new MenuListDialog(title, () -> TopicUtils.showSubscribeSelectTypeDialog(context, mHandler, topic,
                    new TopicListItemTask(context, (Topic) topic, getAdapter()) {
                        @Override
                        public String doInBackground(Topic topic1, String... pars) throws Throwable {
                            return TopicApi.changeFavorite(Client.getInstance(), topic1.getId(), pars[0]);
                        }

                        @Override
                        public void onPostExecute(Topic topic1) {

                        }
                    }
            )));

            if (isFavotitesList) {
                optionsMenu.add(new MenuListDialog(context.getString(R.string.DeleteFromFavorites), () -> {
                    Toast.makeText(context, R.string.request_sent, Toast.LENGTH_SHORT).show();
                    new TopicListItemTask(context, (Topic) topic, getAdapter()) {
                        @Override
                        public String doInBackground(Topic topic12, String... pars) throws ParseException, IOException, URISyntaxException {
                            return TopicApi.deleteFromFavorites(Client.getInstance(), topic12.getId());
                        }

                        @Override
                        public void onPostExecute(Topic topic12) {
                            getMData().remove(topic12);
                        }
                    }.execute();
                }));

                final FavTopic favTopic = (FavTopic) topic;
                optionsMenu.add(new MenuListDialog((favTopic.isPinned() ? context.getString(R.string.unpin) : context.getString(R.string.pin)) + context.getString(R.string.in_favorites_combined), () -> {
                    Toast.makeText(context, R.string.request_sent, Toast.LENGTH_SHORT).show();
                    new TopicListItemTask(context, topic, getAdapter()) {
                        @Override
                        public String doInBackground(Topic topic13, String... pars)
                                throws ParseException, IOException, URISyntaxException {
                            return TopicApi.pinFavorite(Client.getInstance(), topic13.getId(), favTopic.isPinned() ? TopicApi.TRACK_TYPE_UNPIN : TopicApi.TRACK_TYPE_PIN);
                        }

                        @Override
                        public void onPostExecute(Topic topic13) {
                            ((FavTopic) topic13).setPinned(!favTopic.isPinned());
                        }
                    }.execute();
                }));
            }
            optionsMenu.add(new MenuListDialog(context.getString(R.string.OpenTopicForum), () -> ForumFragment.Companion.showActivity(topic.getForumId(), topic.getId())));
        }
        optionsMenu.add(new MenuListDialog(getString(R.string.attachments), () -> TopicAttachmentListFragment.showActivity(context, listItem.getId())));
    }

    private void showTopicActivity(IListItem topic, String args) {
        ExtTopic.showActivity(topic.getMain(), topic.getId(), args);
        //topicAfterClick(topic);

    }

    private void showSaveNavigateActionDialog(final IListItem topic, final CharSequence selectedAction, final String params) {
        ActionSelectDialogFragment.INSTANCE.showSaveNavigateActionDialog(getContext(),
                String.format("%s.navigate_action", getListName()),
                selectedAction.toString(),
                () -> showTopicActivity(topic, params)
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

        EditPostFragment.Companion.newPostWithAttach(getContext(),
                null, topic.getId().toString(), Client.getInstance().getAuthKey(), extras);
        getActivity().finish();
        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        if (!v.hasWindowFocus()) return;
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
            if (!UserInfoRepository.Companion.getInstance().getLogined()) {
                Toast.makeText(getContext(), "Залогиньтесь для просмотра тем форума!", Toast.LENGTH_LONG).show();
            }
            ActionSelectDialogFragment.INSTANCE.execute(getActivity(),
                    getString(R.string.default_action),
                    String.format("%s.navigate_action", getListName()),
                    new CharSequence[]{getString(R.string.navigate_getfirstpost), getString(R.string.navigate_getlastpost), getString(R.string.navigate_getnewpost), getString(R.string.navigate_last_url)},
                    new CharSequence[]{Topic.NAVIGATE_VIEW_FIRST_POST, Topic.NAVIGATE_VIEW_LAST_POST, Topic.NAVIGATE_VIEW_NEW_POST, Topic.NAVIGATE_VIEW_LAST_URL},
                    value -> showTopicActivity(topic, TopicUtils.getUrlArgs(topic.getId(), value.toString(), Topic.NAVIGATE_VIEW_FIRST_POST.toString())), getString(R.string.default_action_notify)
            );


        } catch (Throwable ex) {
            AppLog.e(getActivity(), ex);
        }
    }

    public void topicAfterClick(String id) {
        for (IListItem item : getMData()) {
            if (item.getId().equals(id)) {
                item.setState(IListItem.STATE_NORMAL);
                getAdapter().notifyDataSetChanged();
                updateItem(item);
                return;
            }
        }
    }

    protected void updateItem(IListItem topic) {
        saveCache();
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new SortedListAdapter(getActivity(), getMData(), getPreferences().getBoolean("showSubMain", false));
    }

    private Comparator<? super IListItem> getComparator() {
        return (Comparator<IListItem>) (listItem1, listItem2) -> {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.list_settings_item) {
            showSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (menu.findItem(R.id.list_settings_item) != null)
            menu.findItem(R.id.list_settings_item).setVisible(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (inflater != null) {
            inflater.inflate(R.menu.topics, menu);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeArrow();
    }

    @Override
    public void onResume() {
        super.onResume();
        removeArrow();
    }
}
