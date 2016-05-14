package org.softeg.slartus.forpdaplus.fragments.search;/*
 * Created by slinkin on 24.04.2014.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.view.ViewPropertyAnimator;

import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaapi.Topic;
import org.softeg.slartus.forpdaapi.search.SearchApi;
import org.softeg.slartus.forpdaapi.search.SearchSettings;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.common.ArrayUtils;
import org.softeg.slartus.forpdaplus.classes.common.StringUtils;
import org.softeg.slartus.forpdaplus.db.ForumsTable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SearchSettingsDialogFragment extends DialogFragment {
    private static final String SEARCH_SETTINGS_KEY = "SEARCH_SETTINGS_KEY";


    public interface ISearchDialogListener {
        void doSearchDialogPositiveClick(SearchSettings searchSettings);

        void doSearchDialogNegativeClick();
    }

    private EditText query_edit, username_edit;
    private CheckBox subforums_check, topics_check;
    private Spinner source_spinner, sort_spinner, result_spinner, forumsSpinner;
    private View forumsProgress, topicsProgress;
    private View topics_group, forums_group, result_group, sort_group, source_group;
    private ImageButton forHideButton;
    private LinearLayout forHide;
    public static final int FORUMS_DIALOG_REQUEST = 1;

    protected Bundle args = new Bundle();

    public static void showSearchSettingsDialog(FragmentActivity activity, SearchSettings searchSettings) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = createSettingsFragment(searchSettings);

        newFragment.show(ft, "dialog");
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args.clear();
        if (getArguments() != null) {
            args.putAll(getArguments());
        }
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);
    }

    public static SearchSettings createDefaultSearchSettings() {
        SearchSettings searchSettings = new SearchSettings(SearchSettings.SEARCH_TYPE_FORUM);
        searchSettings.load(App.getInstance().getPreferences());
        searchSettings.setQuery("");
        searchSettings.setUserName("");
        searchSettings.getTopicIds().clear();
        searchSettings.setSource(SearchSettings.SOURCE_TOPICS);
        return searchSettings;
    }

    public static SearchSettings createForumSearchSettings() {
        SearchSettings searchSettings = new SearchSettings(SearchSettings.SEARCH_TYPE_FORUM);
        searchSettings.load(App.getInstance().getPreferences());
        searchSettings.setQuery("");
        searchSettings.setUserName("");
        searchSettings.getForumsIds().clear();
        searchSettings.getForumsIds().add("all");
        searchSettings.getTopicIds().clear();
        return searchSettings;
    }

    public static SearchSettings createTopicSearchSettings(CharSequence topicId) {
        SearchSettings searchSettings = new SearchSettings(SearchSettings.SEARCH_TYPE_TOPIC);
        searchSettings.setQuery("");
        searchSettings.setUserName("");
        searchSettings.getForumsIds().clear();
        searchSettings.getForumsIds().add("all");
        searchSettings.getTopicIds().clear();
        if (topicId != null)
            searchSettings.getTopicIds().add(topicId.toString());
        searchSettings.setSource(SearchSettings.SOURCE_POSTS);

        return searchSettings;
    }

    public static SearchSettings createUserTopicsSearchSettings(String userNick) {
        SharedPreferences prefs = App.getInstance().getPreferences();
        SearchSettings searchSettings = new SearchSettings(SearchSettings.SEARCH_TYPE_USER_TOPICS);
        searchSettings.load(prefs);
        searchSettings.setQuery("");
        searchSettings.setResultView(SearchSettings.RESULT_VIEW_TOPICS);
        searchSettings.setSource(SearchSettings.SOURCE_TOPICS);
        searchSettings.setUserName(userNick);
        searchSettings.getTopicIds().clear();
        searchSettings.getForumsIds().clear();
        searchSettings.getForumsIds().add("all");
        return searchSettings;
    }

    public static SearchSettings createUserPostsSearchSettings(String userNick) {
        SearchSettings searchSettings = new SearchSettings(SearchSettings.SEARCH_TYPE_USER_POSTS);
        searchSettings.load(App.getInstance().getPreferences());
        searchSettings.setSort(SearchSettings.RESULT_SORT_DATE_DESC);
        searchSettings.setQuery("");
        searchSettings.setResultView(SearchSettings.RESULT_VIEW_POSTS);
        searchSettings.setSource(SearchSettings.SOURCE_POSTS);
        searchSettings.setUserName(userNick);
        searchSettings.getTopicIds().clear();
        searchSettings.getForumsIds().clear();
        searchSettings.getForumsIds().add("all");

        return searchSettings;
    }
    public static SearchSettings createUserPostsInTopicSearchSettings(String userNick, String topic) {
        SearchSettings searchSettings = new SearchSettings(SearchSettings.SEARCH_TYPE_USER_POSTS);
        searchSettings.load(App.getInstance().getPreferences());
        searchSettings.setSort(SearchSettings.RESULT_SORT_DATE_DESC);
        searchSettings.setQuery("");
        searchSettings.setResultView(SearchSettings.RESULT_VIEW_POSTS);
        searchSettings.setSource(SearchSettings.SOURCE_POSTS);
        searchSettings.setUserName(userNick);
        searchSettings.getTopicIds().clear();
        searchSettings.getTopicIds().add(topic);
        searchSettings.getForumsIds().clear();
        searchSettings.getForumsIds().add("all");

        return searchSettings;
    }

    public static SearchSettingsDialogFragment createSettingsFragment(SearchSettings searchSettings) {
        Bundle args = new Bundle();
        args.putParcelable(SEARCH_SETTINGS_KEY, searchSettings);
        SearchSettingsDialogFragment fragment = new SearchSettingsDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static SearchSettingsDialogFragment restartSearch(SearchSettings searchSettings) {
        return createSettingsFragment(searchSettings);
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadPreferences();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
        }
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_settings_fragment, null);
        assert view != null;
        query_edit = (EditText) view.findViewById(R.id.query_edit);
        username_edit = (EditText) view.findViewById(R.id.username_edit);
        subforums_check = (CheckBox) view.findViewById(R.id.subforums_check);
        topics_check = (CheckBox) view.findViewById(R.id.topics_check);
        source_spinner = (Spinner) view.findViewById(R.id.source_spinner);
        sort_spinner = (Spinner) view.findViewById(R.id.sort_spinner);
        result_spinner = (Spinner) view.findViewById(R.id.result_spinner);
        topics_group = view.findViewById(R.id.topics_group);
        forumsProgress = view.findViewById(R.id.forums_progress);
        topicsProgress = view.findViewById(R.id.topics_progress);
        forums_group = view.findViewById(R.id.forums_group);
        source_group = view.findViewById(R.id.source_group);
        sort_group = view.findViewById(R.id.sort_group);
        result_group = view.findViewById(R.id.result_group);
        forHideButton = (ImageButton) view.findViewById(R.id.forHideButton);
        forHide = (LinearLayout) view.findViewById(R.id.forHide);

        final RotateAnimation rotate = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF,
                0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(250);
        final Runnable hideRunnable = new Runnable() {
            @Override
            public void run() {
                forHide.setVisibility(View.VISIBLE);
            }
        };

        forHideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(forHide.getVisibility() == View.VISIBLE){
                    forHideButton.startAnimation(rotate);
                    forHide.setVisibility(View.GONE);
                }else {
                    forHideButton.startAnimation(rotate);
                    new Handler().postDelayed(hideRunnable, 250);
                }
            }
        });
        if (view.findViewById(R.id.forums_button) != null) {
            view.findViewById(R.id.forums_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment newFragment = ForumsTreeDialogFragment.newInstance(true, getSearchSettings().getForumsIds());
                    newFragment.setTargetFragment(SearchSettingsDialogFragment.this, FORUMS_DIALOG_REQUEST);
                    newFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            });
        }
        if (view.findViewById(R.id.forums_spinner) != null) {
            initSpinner(view);
        }
        MaterialDialog adb = new MaterialDialog.Builder(getActivity())
                .customView(view, true)
                .cancelable(true)
                .title(R.string.search)
                .positiveText(R.string.find)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        MainActivity.startForumSearch(createSearchSettings());
                    }
                })
                .build();

        // в поиске по теме не показываем "запомнить настройки"
        if (SearchSettings.SEARCH_TYPE_FORUM.equals(getSearchSettings().getSearchType())) {
            adb.setActionButton(DialogAction.NEUTRAL, R.string.remember);
            View neutral = adb.getActionButton(DialogAction.NEUTRAL);
            neutral.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SearchSettings searchSettings = createSearchSettings();
                    searchSettings.setQuery("");
                    searchSettings.setUserName("");
                    searchSettings.save(App.getInstance().getPreferences().edit()).commit();
                }
            });
        }
        adb.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final MaterialDialog d = adb;
        return d;
    }

    private SearchSettings createSearchSettings() {
        SearchSettings searchSettings = getSearchSettings();
        searchSettings.setQuery(query_edit.getText().toString());
        searchSettings.setUserName(username_edit.getText().toString());

        searchSettings.setSearchInSubForums(subforums_check.isChecked());
        searchSettings.setSource(getResources().getStringArray(R.array.SearchSourceValues)[(int) source_spinner.getSelectedItemId()]);
        searchSettings.setSort(getResources().getStringArray(R.array.SearchSortValues)[(int) sort_spinner.getSelectedItemId()]);
        searchSettings.setResultView(getResources().getStringArray(R.array.SearchResultValues)[(int) result_spinner.getSelectedItemId()]);
        if (!topics_check.isChecked())
            searchSettings.getTopicIds().clear();
        return searchSettings;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        if (requestCode == FORUMS_DIALOG_REQUEST) {
            if (resultCode == ForumsTreeDialogFragment.OK_RESULT) {
                getSearchSettings().getForumsIds().clear();
                getSearchSettings().getForumsIds().addAll(Arrays.asList(data.getExtras().getStringArray(ForumsTreeDialogFragment.FORUM_IDS_KEY)));
                loadForums(getSearchSettings().getForumsIds());
            }
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    private SearchSettings getSearchSettings() {
        return args.getParcelable(SEARCH_SETTINGS_KEY);
    }

    private void loadPreferences() {
        SearchSettings searchSettings = getSearchSettings();
        assert searchSettings != null;
        query_edit.setText(searchSettings.getQuery());
        username_edit.setText(searchSettings.getUserName());
        subforums_check.setChecked(searchSettings.getIsSubForums());

        source_spinner.setSelection(ArrayUtils.indexOf(searchSettings.getSource(), getResources().getStringArray(R.array.SearchSourceValues)));
        sort_spinner.setSelection(ArrayUtils.indexOf(searchSettings.getSort(), getResources().getStringArray(R.array.SearchSortValues)));
        result_spinner.setSelection(ArrayUtils.indexOf(searchSettings.getResultView(), getResources().getStringArray(R.array.SearchResultValues)));
        loadForums(searchSettings.getForumsIds());
        loadTopics(searchSettings.getTopicIds());

        switch (searchSettings.getSearchType()) {
            case SearchSettings.SEARCH_TYPE_TOPIC:
                forums_group.setVisibility(View.GONE);
                source_group.setVisibility(View.GONE);
                result_group.setVisibility(View.GONE);
                break;
            case SearchSettings.SEARCH_TYPE_USER_POSTS:
                source_group.setVisibility(View.GONE);
                result_group.setVisibility(View.GONE);
                break;
            case SearchSettings.SEARCH_TYPE_USER_TOPICS:
                source_group.setVisibility(View.GONE);
                result_group.setVisibility(View.GONE);
                break;
            default:
                forums_group.setVisibility(View.VISIBLE);
                source_group.setVisibility(View.VISIBLE);
                result_group.setVisibility(View.VISIBLE);
        }

    }

    private void initSpinner(View parentView) {
        forumsSpinner = (Spinner) parentView.findViewById(R.id.forums_spinner);
        forumsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) return;
                forumsSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void loadForums(final Collection<String> checkedForumIds) {

        final Handler handler = new Handler();
        forumsProgress.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<String> forums = ForumsTable.loadForumTitlesList(checkedForumIds);
                    if (checkedForumIds.contains("all"))
                        forums.add(0, getString(R.string.all_forums));
                    forums.add(0, getString(R.string.total)+": " + forums.size());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (getActivity() == null)
                                return;
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, forums);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            forumsSpinner.setAdapter(adapter);
                            forumsProgress.setVisibility(View.GONE);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadTopics(final Collection<String> topicIds) {
        topics_group.setVisibility(topicIds.size() == 0 ? View.GONE : View.VISIBLE);
        if (topicIds.size() != 0) {

            topics_check.setText(StringUtils.join(new ArrayList(topicIds), ","));
            topicsProgress.setVisibility(View.VISIBLE);

            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SearchSettings settings = new SearchSettings(SearchSettings.SEARCH_TYPE_FORUM);
                    settings.getTopicIds().addAll(topicIds);
                    settings.setSource(SearchSettings.SOURCE_TOPICS);
                    settings.setResultView(SearchSettings.RESULT_VIEW_TOPICS);

                    ArrayList<Topic> topics = new ArrayList<>();
                    try {
                        topics = SearchApi.getSearchTopicsResult(Client.getInstance(), settings.getSearchQuery(), new ListInfo());
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                    final StringBuilder sb = new StringBuilder();
                    for (Topic topic : topics) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        sb.append(topic.getTitle());
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            topics_check.setText(sb.toString());
                            topicsProgress.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        }
    }

}
