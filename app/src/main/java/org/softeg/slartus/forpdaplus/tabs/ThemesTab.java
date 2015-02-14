package org.softeg.slartus.forpdaplus.tabs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.post.EditPostActivity;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.AppProgressDialog;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.classes.ForumItem;
import org.softeg.slartus.forpdaplus.classes.ThemeOpenParams;
import org.softeg.slartus.forpdaplus.classes.Themes;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdaapi.Topic;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 05.10.11
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public abstract class ThemesTab extends BaseTab {


    private View m_Header;
    protected View m_Footer;
    protected PullToRefreshListView lstTree;
    private TextView txtFroum, txtLoadMoreThemes;
    private ImageButton btnStar;
    private ImageButton btnSettings;
    private TextView txtPullToLoadMore;
    private ImageView imgPullToLoadMore;
    protected Boolean m_UseVolumesScroll = false;


    public ThemesTab(Context context, String tabTag, ITabParent tabParent) {
        super(context, tabParent);

        m_TabId = tabTag;

        addView(inflate(context, R.layout.forum_tree, null),
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        loadPreferences();

        lstTree = (PullToRefreshListView) findViewById(R.id.lstTree);
        lstTree.getRefreshableView().setCacheColorHint(0);

        lstTree.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listItemClick(adapterView, view, i, l);
            }
        });
        lstTree.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        m_Header = inflate(getContext(), R.layout.themes_list_header, null);
        txtFroum = (TextView) m_Header.findViewById(R.id.txtFroum);
        btnSettings = (ImageButton) m_Header.findViewById(R.id.btnSettings);
        btnSettings.setVisibility(View.GONE);

        btnStar = (ImageButton) m_Header.findViewById(R.id.btnStar);
        btnStar.setVisibility(needShowStarButton() ? View.VISIBLE : View.INVISIBLE);
        btnStar.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                starButtonClick();
            }
        });
        lstTree.getRefreshableView().addHeaderView(m_Header);

        m_Footer = inflate(getContext(), R.layout.themes_list_footer, null);
        m_Footer.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (m_ShowLatestTask != null && m_ShowLatestTask.getStatus() == AsyncTask.Status.RUNNING)
                    return;
                if (m_Themes.getThemesCount() > m_Themes.size())
                    loadLatest();
            }
        });
        txtLoadMoreThemes = (TextView) m_Footer.findViewById(R.id.txtLoadMoreThemes);
        txtPullToLoadMore = (TextView) m_Footer.findViewById(R.id.txtPullToLoadMore);
        imgPullToLoadMore = (ImageView) m_Footer.findViewById(R.id.imgPullToLoadMore);
        lstTree.getRefreshableView().addFooterView(m_Footer);


        m_ThemeAdapter = new ThemeAdapter(getContext(), getTabId(), getTemplate(), R.layout.theme_item, m_Themes);
        m_ThemeAdapter.showForumTitle(isShowForumTitle());
        setHeaderText(getTitle());
        beforeSetAdapterOnInit();
        lstTree.getRefreshableView().setAdapter(m_ThemeAdapter);
    }

    private boolean isUseChache() {
        if (!cachable()) return false;
        return true;
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
//        //if (!prefs.getBoolean(getTemplate() + ".themeslist.usechache", false)) return false;
//        return prefs.getBoolean("themeslist.usechache", true);
    }

    public void loadCache() {
        //if (!isUseChache()) return;

        Client.INSTANCE.checkLoginByCookies();
        m_Themes = new Themes();

    }

    protected void beforeSetAdapterOnInit() {

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!m_UseVolumesScroll)
            return super.dispatchKeyEvent(event);

        int action = event.getAction();

        ListView scrollView = getListView();
        int visibleItemsCount = scrollView.getLastVisiblePosition() - scrollView.getFirstVisiblePosition();

        String scrollUpKeys = "," + PreferenceManager.getDefaultSharedPreferences(MyApp.getContext())
                .getString("keys.scrollUp", "24").replace(" ", "") + ",";
        String scrollDownKeys = "," + PreferenceManager.getDefaultSharedPreferences(MyApp.getContext())
                .getString("keys.scrollDown", "25").replace(" ", "") + ",";

        int keyCode = event.getKeyCode();

        if (scrollUpKeys.contains("," + Integer.toString(keyCode) + ",")) {
            if (action == KeyEvent.ACTION_DOWN)
                scrollView.setSelection(Math.max(scrollView.getFirstVisiblePosition() - visibleItemsCount, 0));
            return true;// true надо обязательно возвращать даже если не ACTION_DOWN иначе звук нажатия
        } else if (scrollDownKeys.contains("," + Integer.toString(keyCode) + ",")) {
            if (action == KeyEvent.ACTION_DOWN)
                scrollView.setSelection(Math.min(scrollView.getLastVisiblePosition(), scrollView.getCount() - 1));
            return true;// true надо обязательно возвращать даже если не ACTION_DOWN иначе звук нажатия
        }


        return super.dispatchKeyEvent(event);

    }

    public String getTitle() {
        return "Пусто";
    }

    protected String m_TabId;


    public String getTabId() {
        return m_TabId;
    }

    public Bundle getExtras() {
        if (!(getContext() instanceof Activity))
            return null;
        Intent intent = ((Activity) getContext()).getIntent();
        if (intent == null) return null;
        return intent.getExtras();
    }

    public Boolean tryNewPost(ExtTopic topic) {
        Bundle extras = getExtras();
        if (extras == null)
            return false;
        if (!extras.containsKey(Intent.EXTRA_STREAM) &&
                !extras.containsKey(Intent.EXTRA_TEXT) &&
                !extras.containsKey(Intent.EXTRA_HTML_TEXT)) return false;

        EditPostActivity.newPostWithAttach(getContext(), topic.getForumId(), topic.getId(), topic.getAuthKey(),
                extras);
        assert ((Activity) getContext()) != null;
        ((Activity) getContext()).finish();
        return true;
    }

    /**
     * Проверка, не вызван ли список тем для выбора
     *
     * @param topic
     * @return
     */
    public Boolean checkSelectTopicAction(ExtTopic topic) {
        Bundle extras = getExtras();
        if (extras == null)
            return false;
        if (!extras.containsKey(IntentActivity.ACTION_SELECT_TOPIC)) return false;

        Intent intent = new Intent();
        intent.putExtra(IntentActivity.RESULT_TOPIC_ID, topic.getId());
        ((Activity) getContext()).setResult(Activity.RESULT_OK, intent);

        ((Activity) getContext()).finish();
        return true;
    }

    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        try {

            l = ListViewMethodsBridge.getItemId(getContext(), i, l);
            if (l < 0 || m_ThemeAdapter.getCount() <= l) return;

            if (m_ThemeAdapter == null) return;
            final ExtTopic topic = m_ThemeAdapter.getItem((int) l);
            if (TextUtils.isEmpty(topic.getId())) return;


            if (tryNewPost(topic))
                return;

            if (checkSelectTopicAction(topic))
                return;

            if (getOpenThemeParams() == null) {
                showNavigateDialog(getActivity(), getTabId(), getTemplate(),
                        topic.getId(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        topicAfterClick(topic);
                    }
                });
                return;
            }

            topic.showActivity(getContext(), getOpenThemeParams());
            topicAfterClick(topic);
        } catch (Throwable ex) {
            Log.e(getContext(), ex);
        }

    }

    private void topicAfterClick(ExtTopic topic) {
        Boolean needSaveCache = topic.getIsNew();
        topic.setIsNew(false);

        m_ThemeAdapter.notifyDataSetChanged();


        //TabTable.saveTab(m_Themes, getTemplate(), m_Themes.size());
    }


    protected void starButtonClick() {

    }

    protected Boolean addFavoritesMenu() {
        return true;
    }

    protected void setStarButtonState(Boolean on) {
        btnStar.setImageResource(on ? R.drawable.btn_star_big_on : R.drawable.btn_star_big_off);
    }

    protected Boolean needShowStarButton() {
        return false;
    }

    protected void showStarButton(Boolean show) {
        btnStar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_UseVolumesScroll = prefs.getBoolean("themeslist.UseVolumesScroll", false);
    }

    ShowLatestTask m_ShowLatestTask = new ShowLatestTask(getContext());


    protected void loadLatest() {
        if (m_ShowLatestTask != null && m_ShowLatestTask.getStatus() == AsyncTask.Status.RUNNING)
            m_ShowLatestTask.cancel(true);
        m_ShowLatestTask = new ShowLatestTask(getContext());

        m_ShowLatestTask.execute();
    }

    protected void modifyThemesListAfterLoad() {
    }

    public Boolean onParentBackPressed() {
        return false;
    }

    public Boolean refreshed() {
        return m_Refreshed;
    }

    protected Boolean m_Refreshed = false;

    public void refresh() {
        refresh(null);
    }

    public void refresh(Bundle savedInstanceState) {
        if (m_ShowLatestTask != null && m_ShowLatestTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        m_Refreshed = true;
        m_ThemeAdapter = null;
        m_Themes.clear();

        loadLatest();
    }

    public ListView getListView() {
        return lstTree.getRefreshableView();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        if (m_ThemeAdapter != null)
            m_ThemeAdapter.onCreateContextMenu(menu, v, menuInfo, addFavoritesMenu(), handler);
    }

    protected void getThemes(OnProgressChangedListener progressChangedListener) throws Throwable {

    }

    protected ThemeAdapter m_ThemeAdapter;
    Themes m_Themes = new Themes();

    protected String m_CurrentAdapter = "ThemeAdapter";

    private Boolean m_FirstLoading = true;

    private class ShowLatestTask extends AsyncTask<ForumItem, String, Boolean> {

        private final ProgressDialog dialog;

        public ShowLatestTask(Context context) {

            dialog = new AppProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cancel(true);
                }
            });
        }

        int m_SelectedIndex = 0;

        @Override
        protected Boolean doInBackground(ForumItem... forums) {
            try {
                if (this.isCancelled()) return false;
                m_SelectedIndex = Math.max(Math.min(m_Themes.size(), lstTree.getRefreshableView().getFirstVisiblePosition()), 0);

                if (m_FirstLoading && isUseChache()) {
                    loadCache();
                    if (m_Themes.size() == 0)
                        getThemes(new OnProgressChangedListener() {
                            public void onProgressChanged(String state) {
                                publishProgress(state);
                            }
                        });
                } else
                    getThemes(new OnProgressChangedListener() {
                        public void onProgressChanged(String state) {
                            publishProgress(state);
                        }
                    });
                return true;
            } catch (Throwable e) {
                //Log.e(getContext(), e);
                ex = e;
                return false;
            } finally {
                m_FirstLoading = false;
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setMessage(progress[0]);
        }

        protected void onCancelled() {
            Toast.makeText(getContext(), getContext().getString(R.string.Canceled), Toast.LENGTH_SHORT).show();
        }

        // can use UI thread here
        protected void onPreExecute() {
            try {
                this.dialog.setMessage(getContext().getResources().getString(R.string.loading));
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        private Throwable ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e(null, ex);
            }

            if (success) {

                listLoaded(m_SelectedIndex);

            } else {
                if (ex != null)
                    Log.e(ThemesTab.this.getContext(), ex, new Runnable() {
                        @Override
                        public void run() {
                            loadLatest();
                        }
                    });
            }

            lstTree.onRefreshComplete();
            super.onPostExecute(success);
        }

    }

    private void listLoaded(int selectedIndex) {

        int allThemesCount = m_Themes.getThemesCount();

        txtLoadMoreThemes.setText(getContext().getString(R.string.All) + ": " + allThemesCount);
        modifyThemesListAfterLoad();

        if (!m_CurrentAdapter.equals("ThemeAdapter") || m_ThemeAdapter == null) {
            setAdapter();
        }

        m_ThemeAdapter.showForumTitle(isShowForumTitle());

        m_ThemeAdapter.setParams(getOpenThemeParams());
        m_ThemeAdapter.notifyDataSetChanged();


        int loadMoreVisibility = allThemesCount > m_Themes.size() ? View.VISIBLE : View.GONE;
        txtPullToLoadMore.setVisibility(loadMoreVisibility);
        imgPullToLoadMore.setVisibility(loadMoreVisibility);

        setHeaderText(allThemesCount + " тем @ " + getTitle());
        if (selectedIndex == 0) {
            lstTree.getRefreshableView().setSelection(0);
        }

        afterOnPostSuccessExecute();
    }

    private void setAdapter() {
        m_CurrentAdapter = "ThemeAdapter";
        m_ThemeAdapter = new ThemeAdapter(getContext(), getTabId(), getTemplate(),
                R.layout.theme_item, m_Themes);
//            Comparator<Topic> topicComparator=getSortComparator();
//            if(topicComparator!=null)
//                m_ThemeAdapter.sort(topicComparator);

        m_ThemeAdapter.showForumTitle(isShowForumTitle());
        lstTree.getRefreshableView().addFooterView(m_Footer);

        lstTree.getRefreshableView().setAdapter(m_ThemeAdapter);

    }

    protected Comparator<ExtTopic> getSortComparator() {
        return null;
    }

    protected void afterOnPostSuccessExecute() {

    }

    protected void setHeaderText(String text) {
        txtFroum.setText(text);
    }

    protected Boolean isShowForumTitle() {
        return false;
    }

    public String getOpenThemeParams() {
        return getOpenThemeParams(getTabId(), getTemplate());
    }

    public static String getOpenThemeParams(CharSequence tabId, CharSequence template) {
        String themeActionPref = getTopicNavigateAction(tabId, template);
        return ThemeOpenParams.getUrlParams(themeActionPref, null);
    }

    public static String getTopicNavigateAction(CharSequence tabId, CharSequence template) {
        return PreferenceManager.getDefaultSharedPreferences(MyApp.getContext()).getString(String.format("%s.%s.navigate_action", tabId, template), null);
    }

    public void saveOpenThemeParams(CharSequence navigateAction) {
        saveOpenThemeParams(getTabId(), getTemplate(), navigateAction);
    }

    public static void saveOpenThemeParams(CharSequence tabId, CharSequence template, CharSequence navigateAction) {
        PreferenceManager.getDefaultSharedPreferences(MyApp.getContext())
                .edit()
                .putString(String.format("%s.%s.navigate_action", tabId, template), navigateAction.toString())
                .commit();
    }


    public static void showNavigateDialog(final Activity activity, final CharSequence tabId, final CharSequence templateId,
                                          final CharSequence topicId,
                                          final DialogInterface.OnClickListener onClickListener) {
        if (activity == null)
            return;


        CharSequence[] titles = new CharSequence[]{activity.getString(R.string.navigate_getfirstpost),
                activity.getString(R.string.navigate_getlastpost), activity.getString(R.string.navigate_getnewpost)};
        final CharSequence[] values = new CharSequence[]{Topic.NAVIGATE_VIEW_FIRST_POST,
                Topic.NAVIGATE_VIEW_LAST_POST, Topic.NAVIGATE_VIEW_NEW_POST};
        final int[] selected = {2};
        new AlertDialogBuilder(activity)
                .setSingleChoiceItems(titles, selected[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selected[0] = i;
                    }
                })
                .setTitle("Действие по умолчанию")
                .setPositiveButton("Всегда",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();

                                new AlertDialogBuilder(activity)
                                        .setTitle("Подсказка")
                                        .setMessage("Вы можете изменить действие по умолчанию долгим тапом по теме")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();

                                                String navigateAction = values[selected[0]].toString();
                                                saveOpenThemeParams(tabId, templateId, navigateAction);
                                                ExtTopic.showActivity(activity, topicId,
                                                        ThemeOpenParams.getUrlParams(navigateAction, null));

                                                onClickListener.onClick(null, -1);
                                            }
                                        })
                                        .create().show();


                            }
                        }
                )
                .setNeutralButton("Только сейчас",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();


                                String navigateAction = values[selected[0]].toString();
                                ExtTopic.showActivity(activity, topicId,
                                        ThemeOpenParams.getUrlParams(navigateAction, null));

                                onClickListener.onClick(null, -1);
                            }
                        }
                )
                .create()
                .show();

    }


}
