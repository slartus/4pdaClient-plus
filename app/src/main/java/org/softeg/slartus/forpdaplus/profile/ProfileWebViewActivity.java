package org.softeg.slartus.forpdaplus.profile;/*
 * Created by slinkin on 17.04.2014.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.BaseFragmentActivity;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.qms.QmsNewThreadActivity;
import org.softeg.slartus.forpdaplus.search.ui.SearchActivity;
import org.softeg.slartus.forpdaplus.search.ui.SearchSettingsDialogFragment;


public class ProfileWebViewActivity extends BaseFragmentActivity {
    public static final String USER_ID_KEY = "UserIdKey";
    public static final String USER_NAME_KEY = "UserNameKey";
    public static void startActivity(Context context, String userId) {
        startActivity(context, userId, null);
    }

    public static void startActivity(Context context, String userId, String userName) {
        Intent intent = new Intent(context, ProfileWebViewActivity.class);

        intent.putExtra(USER_ID_KEY, userId);
        intent.putExtra(USER_NAME_KEY, userName);
        intent.putExtra("activity", context.getClass().toString());
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setContentView(R.layout.simple_fragment_activity);

        try {
            Bundle extras = getIntent().getExtras();
            ProfileWebViewFragment details = new ProfileWebViewFragment();
            details.setArguments(extras);

            MenuFragment menuFragment = new MenuFragment();
            menuFragment.setArguments(extras);


            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, details).add(menuFragment, "menufragment").commit();

        } catch (Throwable e) {
            AppLog.e(this, e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public static final class MenuFragment extends ProfileMenuFragment {
        private String userId;
        private String userNick;

        @Override
        public void onCreate(android.os.Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(USER_ID_KEY))
                    userId = savedInstanceState.getString(USER_ID_KEY);
                if (savedInstanceState.containsKey(USER_NAME_KEY))
                    userNick = savedInstanceState.getString(USER_NAME_KEY);
            }
            if (getArguments() != null) {
                if (getArguments().containsKey(USER_ID_KEY))
                    userId = getArguments().getString(USER_ID_KEY);
                if (getArguments().containsKey(USER_NAME_KEY))
                    userNick = getArguments().getString(USER_NAME_KEY);
            }
            setHasOptionsMenu(true);// важно после получения аргументов это сделать!!!
        }

        @Override
        public void onSaveInstanceState(android.os.Bundle outState) {
            outState.putString(USER_ID_KEY, userId);
            outState.putString(USER_NAME_KEY, userNick);

            super.onSaveInstanceState(outState);
        }


        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu,inflater);
            MenuItem item;

            if (Client.getInstance().getLogined() && userId != null && !userId.equals(Client.getInstance().UserId)) {
                item = menu.add(getString(R.string.MessagesQms)).setIcon(R.drawable.ic_menu_send);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        QmsNewThreadActivity.showUserNewThread(getActivity(), userId, userNick);

                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }


            item = menu.add(getString(R.string.Reputation));
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    CharSequence[] items = {"Поднять", "Опустить", "Посмотреть", "Кому изменял репутацию"};
                    new MaterialDialog.Builder(getActivity())
                            .title("Репутация")
                            .items(items)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int i, CharSequence items) {
                                    switch (i) {
                                        case 0:
                                            UserReputationFragment.plusRep(getActivity(), new Handler(), userId, userNick);
                                            break;
                                        case 1:
                                            UserReputationFragment.minusRep(getActivity(), new Handler(), userId, userNick);
                                            break;
                                        case 2:
                                            UserReputationFragment.showActivity(getActivity(), userId, false);
                                            break;
                                        case 3:
                                            UserReputationFragment.showActivity(getActivity(), userId, true);
                                            break;
                                    }
                                }
                            })
                            .show();

                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(getString(R.string.FindUserTopics));
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    SearchActivity.startForumSearch(getActivity(),
                            SearchSettingsDialogFragment.
                                    createUserTopicsSearchSettings(userNick)
                    );
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(getString(R.string.FindUserPosts));
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    SearchActivity.startForumSearch(getActivity(),
                            SearchSettingsDialogFragment.
                                    createUserPostsSearchSettings(userNick)
                    );
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add("Ссылка на профиль");
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ExtUrl.showSelectActionDialog(getActivity(),"Ссылка на профиль","http://4pda.ru/forum/index.php?showuser=" + userId);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        }
    }


}
