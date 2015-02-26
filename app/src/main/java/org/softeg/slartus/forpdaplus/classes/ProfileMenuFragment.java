package org.softeg.slartus.forpdaplus.classes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.LoginDialog;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo;
import org.softeg.slartus.forpdaplus.profile.ProfileWebViewActivity;


/**
 * User: slinkin
 * Date: 04.04.12
 * Time: 9:29
 */
public class ProfileMenuFragment extends Fragment {
    public static final String TAG="org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment";
private Handler mHandler=new Handler();
    private SubMenu mUserMenuItem;

    public ProfileMenuFragment() {

    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        setHasOptionsMenu(true);
        Client.INSTANCE.checkLoginByCookies();
        Client.getInstance().addOnUserChangedListener(new Client.OnUserChangedListener() {
            @Override
            public void onUserChanged(String user, Boolean success) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setUserMenu();
                    }
                });
            }
        });
        Client.getInstance().addOnMailListener(new Client.OnMailListener() {
            @Override
            public void onMail(int count) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setUserMenu();
                    }
                });
            }
        });
    }


    private int getUserIconRes() {
        Boolean logged = Client.getInstance().getLogined();
        if (logged) {

            if (Client.getInstance().getQmsCount() > 0) {
                return R.drawable.ic_menu_user_qms;
            }
            return R.drawable.ic_menu_user_online;
        } else {
            return R.drawable.ic_menu_user_offline;
        }
    }

    public void setUserMenu() {
        if (mUserMenuItem == null) return;
        Boolean logged = Client.getInstance().getLogined();

        mUserMenuItem.getItem().setIcon(getUserIconRes());
        mUserMenuItem.getItem().setTitle(Client.getInstance().getUser());
        mUserMenuItem.clear();
        if (logged) {
            String text = Client.getInstance().getQmsCount() > 0 ? ("QMS (" + Client.getInstance().getQmsCount() + ")") : "QMS";
            mUserMenuItem.add(text)
                    .setIcon(R.drawable.ic_action_qms)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            ListFragmentActivity.showListFragment(getActivity(), QmsContactsBrickInfo.NAME, null);
                            return true;
                        }
                    });

            mUserMenuItem.add(R.string.Profile)
                    .setIcon(R.drawable.ic_action_user_online)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            ProfileWebViewActivity.startActivity(getActivity(), Client.getInstance().UserId, Client.getInstance().getUser());
                            return true;
                        }
                    });


            mUserMenuItem.add(R.string.Reputation)
                    .setIcon(R.drawable.ic_action_user_online)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            UserReputationFragment.showActivity(getActivity(), Client.getInstance().UserId,false);
                            return true;
                        }
                    });

            mUserMenuItem.add(R.string.Logout)
                    .setIcon(R.drawable.ic_menu_user_offline)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    LoginDialog.logout(getActivity());
                    return true;
                }
            });
        } else {
            mUserMenuItem.add(R.string.Login).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    LoginDialog.showDialog(getActivity(), null);
                    return true;
                }
            });

            mUserMenuItem.add(R.string.Registration).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    Intent marketIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://4pda.ru/forum/index.php?act=Reg&CODE=00"));
                    getActivity().startActivity(marketIntent);
                    //
                    return true;
                }
            });
        }
    }

    private void createUserMenu(Menu menu) {
        mUserMenuItem = menu.addSubMenu(Client.getInstance().getUser());

        mUserMenuItem.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setUserMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        createUserMenu(menu);

    }
}
