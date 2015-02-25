package org.softeg.slartus.forpdaplus.topicview;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.ProfileMenuFragment;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.classes.forum.ExtTopic;
import org.softeg.slartus.forpdaplus.common.HelpTask;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.listfragments.BricksListDialogFragment;
import org.softeg.slartus.forpdaplus.listfragments.ForumCatalogFragment;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listfragments.NotesListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicUtils;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;
import org.softeg.slartus.forpdaplus.listtemplates.NotesBrickInfo;
import org.softeg.slartus.forpdaplus.prefs.Preferences;
import org.softeg.slartus.forpdaplus.prefs.PreferencesActivity;
import org.softeg.slartus.forpdaplus.search.ui.SearchSettingsDialogFragment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 01.11.12
 * Time: 7:18
 * To change this template use File | Settings | File Templates.
 */
public final class TopicViewMenuFragment extends ProfileMenuFragment {

    private ThemeActivity getInterface() {
        if (getActivity() == null) return null;
        return (ThemeActivity) getActivity();
    }

    public TopicViewMenuFragment() {
        super();

    }


    private Boolean m_FirstTime = true;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!m_FirstTime)
            getInterface().onPrepareOptionsMenu();
        m_FirstTime = false;
        if (mTopicOptionsMenu != null)
            configureOptionsMenu(getActivity(), getInterface().getHandler(), mTopicOptionsMenu, getInterface(),
                    true, getInterface().getLastUrl());
        else if (getInterface() != null && getInterface().getTopic() != null)
            mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface(),
                    true, getInterface().getLastUrl());
    }

    private SubMenu mTopicOptionsMenu;

    private static SubMenu addOptionsMenu(final Context context, final Handler mHandler,
                                          Menu menu, final ThemeActivity themeActivity,
                                          Boolean addFavorites, final String shareItUrl) {
        SubMenu optionsMenu = menu.addSubMenu("Опции темы");

        optionsMenu.getItem().setIcon(R.drawable.ic_menu_more);
        configureOptionsMenu(context, mHandler, optionsMenu, themeActivity, addFavorites, shareItUrl);
        return optionsMenu;
    }

    private static Boolean checkTopicMenuItemEnabled(ExtTopic topic) {
        return Client.getInstance().getLogined() && topic != null;
    }

    private static void configureOptionsMenu(final Context context, final Handler mHandler, SubMenu optionsMenu, final ThemeActivity themeActivity,
                                             Boolean addFavorites, final String shareItUrl) {

        optionsMenu.clear();


        if (addFavorites) {
            optionsMenu.add(R.string.AddToFavorites).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        TopicUtils.showSubscribeSelectTypeDialog(context, mHandler, themeActivity.getTopic().getId());
                    } catch (Exception ex) {
                        AppLog.e(context, ex);
                    }

                    return true;
                }
            });

            optionsMenu.add(R.string.DeleteFromFavorites).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        final HelpTask helpTask = new HelpTask(context, context.getString(R.string.DeletingFromFavorites));
                        helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                            public Object onMethod(Object param) {
                                if (helpTask.Success)
                                    Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                else
                                    AppLog.e(context, helpTask.ex);
                                return null;
                            }
                        });
                        helpTask.execute(new HelpTask.OnMethodListener() {
                                             public Object onMethod(Object param) throws IOException, ParseException, URISyntaxException {
                                                 return TopicApi.deleteFromFavorites(Client.getInstance(),
                                                         themeActivity.getTopic().getId());
                                             }
                                         }
                        );
                    } catch (Exception ex) {
                        AppLog.e(context, ex);
                    }
                    return true;
                }
            });


            optionsMenu.add(R.string.OpenTopicForum).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    try {
                        ForumCatalogFragment.showActivity(context, themeActivity.getTopic().getForumId(),
                                themeActivity.getTopic().getId());
                    } catch (Exception ex) {
                        AppLog.e(context, ex);
                    }
                    return true;
                }
            });
        }


        optionsMenu.add(R.string.NotesByTopic).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                Bundle args = new Bundle();
                args.putString(NotesListFragment.TOPIC_ID_KEY, themeActivity.getTopic().getId());
                ListFragmentActivity.showListFragment(context, new NotesBrickInfo().getName(), args);

                return true;
            }
        });

        optionsMenu.add(R.string.Share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    String url = TextUtils.isEmpty(shareItUrl) ? ("http://4pda.ru/forum/index.php?showtopic=" + themeActivity.getTopic().getId()) : shareItUrl;
                    ExtUrl.shareIt(context, shareItUrl, url, url);
                } catch (Exception ex) {
                    return false;
                }
                return true;
            }
        });


    }

    //private MenuItem m_EditPost;

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        try {

            SubMenu subMenu = menu.addSubMenu(R.string.Attaches)
                    .setIcon(R.drawable.ic_menu_download);
            subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            subMenu.add("Вложения текущей страницы")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            getInterface().showPageAttaches();
                            return true;
                        }
                    });
            subMenu.add("Все вложения топика")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            getInterface().showTopicAttaches();
                            return true;
                        }
                    });


            subMenu = menu.addSubMenu(R.string.FindOnPage).setIcon(R.drawable.ic_menu_search);
            subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            subMenu.add(R.string.FindOnPage)
                    .setIcon(R.drawable.ic_action_forum_search)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            getInterface().onSearchRequested();

                            return true;
                        }
                    });
            subMenu.add(R.string.FindInTopic)
                    .setIcon(R.drawable.ic_action_post_search)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            SearchSettingsDialogFragment.showSearchSettingsDialog(getActivity(),
                                    SearchSettingsDialogFragment.createTopicSearchSettings(getInterface().getTopic().getId()));
                            return true;
                        }
                    });

            menu.add(R.string.Refresh)
                    .setIcon(R.drawable.ic_menu_refresh)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            getInterface().reloadTopic();
                            return true;
                        }
                    });
            menu.add(R.string.Browser)
                    .setIcon(R.drawable.ic_menu_goto)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(MenuItem item) {
                            try {
                                Intent marketIntent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(getInterface().getLastUrl()));
                                startActivity(Intent.createChooser(marketIntent, "Выберите"));


                            } catch (ActivityNotFoundException e) {
                                AppLog.e(getActivity(), e);
                            }


                            return true;
                        }
                    });

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            if (getInterface() != null)
                mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface(),
                        true, getInterface().getLastUrl());


            SubMenu optionsMenu = menu.addSubMenu("Вид");
            optionsMenu.getItem().setIcon(R.drawable.ic_menu_preferences);
            optionsMenu.getItem().setTitle("Вид");


            optionsMenu.add(String.format("Аватары (%s)",
                    App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles)[Preferences.Topic.getShowAvatarsOpt()]))
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(final MenuItem menuItem) {

                   new AlertDialogBuilder(getActivity())
                           .setTitle("Показывать аватары")
                           .setCancelable(true)
                           .setSingleChoiceItems(App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles),
                                   Preferences.Topic.getShowAvatarsOpt(), new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialogInterface, int i) {
                                           dialogInterface.dismiss();
                                           if(i==-1)
                                               return;

                                           Preferences.Topic.setShowAvatarsOpt(i);
                                           menuItem.setTitle(String.format("Показывать аватары (%s)",
                                                   App.getContext().getResources().getStringArray(R.array.AvatarsShowTitles)[Preferences.Topic.getShowAvatarsOpt()]));
                                       }
                                   })
                           .create().show();
                    return true;
                }
            });

            optionsMenu.add("Скрывать верхнюю панель")
                    .setIcon(R.drawable.ic_menu_images).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Preferences.setHideActionBar(!Preferences.isHideActionBar());
                    getInterface().setHideActionBar();
                    menuItem.setChecked(Preferences.isHideActionBar());
                    return true;
                }
            }).setCheckable(true).setChecked(Preferences.isHideActionBar());

            optionsMenu.add("Загр-ть изобр-я (для сессии)")
                    .setIcon(R.drawable.ic_menu_images).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Boolean loadImagesAutomatically1 = getInterface().getLoadsImagesAutomatically();
                    getInterface().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                    menuItem.setChecked(!loadImagesAutomatically1);
                    return true;
                }
            }).setCheckable(true).setChecked(getInterface().getLoadsImagesAutomatically());
            optionsMenu.add("Размер шрифта")
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            getInterface().showFontSizeDialog();
                            return true;
                        }
                    });

            optionsMenu.add("Стиль").setIcon(R.drawable.ic_menu_styles).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    showStylesDialog(prefs);
                    return true;
                }
            });

            menu.add("Быстрый доступ..").setIcon(R.drawable.ic_menu_quickrun).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    BricksListDialogFragment.showDialog((BricksListDialogFragment.IBricksListDialogCaller) getActivity(),
                            BricksListDialogFragment.QUICK_LIST_ID,
                            ListCore.getBricksNames(ListCore.getQuickBricks()), null);

                    return true;
                }
            });


            if (Preferences.System.isDeveloper()) {
                menu.add("Сохранить страницу").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            getInterface().saveHtml();
                        } catch (Exception ex) {
                            return false;
                        }
                        return true;
                    }
                });
            }

            if (Preferences.System.isCurator()) {
                menu.add("Мультимодерация").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        try {
                            getInterface().getCurator().showMmodDialog();
                        } catch (Exception ex) {
                            return false;
                        }
                        return true;
                    }
                });
            }
            addCloseMenuItem(menu);

        } catch (Exception ex) {
            AppLog.e(getActivity(), ex);
        }


    }

    private void addCloseMenuItem(Menu menu) {
        MenuItem item;
        item = menu.add("Закрыть")
                .setIcon(R.drawable.ic_menu_close_clear_cancel)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        getInterface().getPostBody();
                        if (!TextUtils.isEmpty(getInterface().getPostBody())) {
                            new AlertDialogBuilder(getActivity())
                                    .setTitle("Подтвердите действие")
                                    .setMessage("Имеется введенный текст сообщения! Закрыть тему?")
                                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            getInterface().clear();
                                            getInterface().finish();
                                        }
                                    })
                                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .create()
                                    .show();
                        } else {
                            getInterface().clear(true);
                            getInterface().finish();
                        }

                        return true;
                    }
                });
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    private void showStylesDialog(final SharedPreferences prefs) {
        try {
            final String currentValue = App.getInstance().getCurrentTheme();

            ArrayList<CharSequence> newStyleNames = new ArrayList<CharSequence>();
            final ArrayList<CharSequence> newstyleValues = new ArrayList<CharSequence>();

            PreferencesActivity.getStylesList(getInterface(), newStyleNames, newstyleValues);
            final int selected = newstyleValues.indexOf(currentValue);


            LayoutInflater inflater = (LayoutInflater) getInterface()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_select_style, null);
            final ListView listView = (ListView) view.findViewById(R.id.listView);

            listView.setAdapter(new ArrayAdapter<CharSequence>(getInterface(),
                    android.R.layout.simple_list_item_single_choice, newStyleNames));
            listView.setItemChecked(selected, true);


            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            checkBox.setChecked(prefs.getBoolean("theme.BrowserStyle", false));

            AlertDialog alertDialog = new AlertDialogBuilder(getActivity())
                    .setTitle("Стиль")
                    .setCancelable(true)
                    .setView(view)
                    .setPositiveButton("Применить и перезагрузить страницу", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int selected = listView.getCheckedItemPosition();
                            if (selected == -1) {
                                Toast.makeText(getActivity(), "Выберите стиль", Toast.LENGTH_LONG).show();
                                return;
                            }
                            dialogInterface.dismiss();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("appstyle", newstyleValues.get(selected).toString());
                            editor.putBoolean("theme.BrowserStyle", checkBox.isChecked());
                            editor.commit();

                            getInterface().showTheme(getInterface().getLastUrl());
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();

            alertDialog.show();
        } catch (Exception ex) {
            AppLog.e(getInterface(), ex);
        }
    }
}
