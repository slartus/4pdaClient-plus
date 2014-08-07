package org.softeg.slartus.forpdaplus.topicview;/*
 * Created by slinkin on 09.07.2014.
 */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import net.londatiga.android3d.ActionItem;
import net.londatiga.android3d.QuickAction;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.TopicAttaches;
import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.listfragments.UserReputationFragment;
import org.softeg.slartus.forpdaplus.tabs.TopicReadingUsersTab;
import org.softeg.slartus.forpdaplus.tabs.TopicWritersTab;

public class HtmloutWebInterface {
    public static final String NAME = "HTMLOUT";
    private ThemeActivity context;

    public HtmloutWebInterface(ThemeActivity context) {
        this.context = context;
    }

    private ThemeActivity getContext() {
        return context;
    }

    @JavascriptInterface
    public void showImgPreview(final String title, final String previewUrl, final String fullUrl) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ThemeActivity.showImgPreview(getContext(), title, previewUrl, fullUrl);
            }
        });
    }

    @JavascriptInterface
    public void quote(final String forumId, final String topicId, final String postId, final String postDate, final String userId, final String userNick) {
        getContext().runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           getContext().quote(forumId, topicId, postId, postDate, userId, userNick);

                                       }
                                   }
        );
    }

    @JavascriptInterface
    public void checkBodyAndReload(final String postBody) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().checkBodyAndReload(postBody);
            }
        });

    }

    @JavascriptInterface
    public void showTopicAttaches(final String postBody) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TopicAttaches topicAttaches = new TopicAttaches();
                topicAttaches.parseAttaches(postBody);
                if (topicAttaches.size() == 0) {
                    Toast.makeText(getContext(), "Страница не имеет вложений", Toast.LENGTH_SHORT).show();
                    return;
                }
                final boolean[] selection = new boolean[topicAttaches.size()];
                new AlertDialogBuilder(getContext())
                        .setTitle("Вложения")
                        .setMultiChoiceItems(topicAttaches.getList(), selection, new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                selection[i] = b;
                            }
                        })
                        .setPositiveButton("Скачать", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();

                                if (!Client.getInstance().getLogined()) {
                                    new AlertDialogBuilder(getContext())
                                            .setTitle("Внимание!")
                                            .setMessage("Для скачивания файлов с сайта необходимо залогиниться!")
                                            .setPositiveButton("ОК", null)
                                            .create().show();
                                    return;
                                }
                                for (int j = 0; j < selection.length; j++) {
                                    if (!selection[j]) continue;
                                    DownloadsService.download(getContext(), topicAttaches.get(j).getUri(), false);
                                }
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    @JavascriptInterface
    public void showPostLinkMenu(final String postId) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().showLinkMenu(org.softeg.slartus.forpdaplus.classes.Post.getLink(getContext().getTopic().getId(), postId), postId);
            }
        })
        ;
    }

    @JavascriptInterface
    public void postVoteBad(final String postId) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogBuilder(getContext())
                        .setTitle("Подтвердите действие")
                        .setMessage("Понизить рейтинг сообщения?")
                        .setPositiveButton("Понизить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                org.softeg.slartus.forpdaplus.classes.Post.minusOne(getContext(), new Handler(), postId);
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        })
        ;
    }

    @JavascriptInterface
    public void postVoteGood(final String postId) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialogBuilder(getContext())
                        .setTitle("Подтвердите действие")
                        .setMessage("Повысить рейтинг сообщения?")
                        .setPositiveButton("Повысить", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                org.softeg.slartus.forpdaplus.classes.Post.plusOne(getContext(), new Handler(), postId);
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            }
        })
        ;
    }

    @JavascriptInterface
    public void showReadingUsers() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    TopicReadingUsersTab.show(getContext(), getContext().getTopic().getId());
                } catch (ActivityNotFoundException e) {
                    Log.e(getContext(), e);
                }
            }
        });

    }

    @JavascriptInterface
    public void showWriters() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TopicWritersTab.show(getContext(), getContext().getTopic().getId());
            }
        });
    }

    @JavascriptInterface
    public void showUserMenu(final String postId, final String userId, final String userNick, final String avatar) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ForumUser.showUserQuickAction(getContext(), getContext().getWebView(), postId, userId, userNick, avatar,
                        new ForumUser.InsertNickInterface() {
                            @Override
                            public void insert(String text) {
                                insertTextToPost(text);
                            }
                        }
                );
            }
        });
    }

    @JavascriptInterface
    public void insertTextToPost(final String text) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().post(new Runnable() {
                    public void run() {
                        getContext().insertTextToPost(text);
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public void showPostMenu(final String postId, final String postDate,
                             final String userId, final String userNick,
                             final String canEdit, final String canDelete) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().openActionMenu(postId, postDate, userId, userNick, "1".equals(canEdit), "1".equals(canDelete));
            }
        });
    }

    @JavascriptInterface
    public void post() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().post();
            }
        });
    }

    @JavascriptInterface
    public void nextPage() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().nextPage();
            }
        });
    }

    @JavascriptInterface
    public void prevPage() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().prevPage();
            }
        });

    }

    @JavascriptInterface
    public void firstPage() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().firstPage();
            }
        });
    }

    @JavascriptInterface
    public void lastPage() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().lastPage();
            }
        });
    }

    @JavascriptInterface
    public void jumpToPage() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    final CharSequence[] pages = new CharSequence[getContext().getTopic().getPagesCount()];

                    final int postsPerPage = getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());

                    for (int p = 0; p < getContext().getTopic().getPagesCount(); p++) {
                        pages[p] = "Стр. " + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
                    }

                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.select_page_layout, null);

                    assert view != null;
                    final ListView listView = (ListView) view.findViewById(R.id.lstview);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_list_item_single_choice, pages);
                    // присваиваем адаптер списку
                    listView.setAdapter(adapter);

                    final EditText txtNumberPage = (EditText) view.findViewById(R.id.txtNumberPage);
                    txtNumberPage.setText(Integer.toString(getContext().getTopic().getCurrentPage()));
                    txtNumberPage.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                            if (txtNumberPage.getTag() != null && !((Boolean) txtNumberPage.getTag()))
                                return;
                            if (TextUtils.isEmpty(charSequence)) return;
                            try {
                                int value = Integer.parseInt(charSequence.toString());
                                value = Math.min(pages.length - 1, value - 1);
                                listView.setTag(false);
                                listView.setItemChecked(value, true);
                                listView.setSelection(value);
                            } catch (Throwable ex) {
                                Log.e(getContext(), ex);
                            } finally {
                                listView.setTag(true);
                            }

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (listView.getTag() != null && !((Boolean) listView.getTag()))
                                return;
                            txtNumberPage.setTag(false);
                            try {
                                txtNumberPage.setText(Integer.toString((int) l + 1));
                            } catch (Throwable ex) {
                                Log.e(getContext(), ex);
                            } finally {
                                txtNumberPage.setTag(true);
                            }
                        }
                    });

                    listView.setItemChecked(getContext().getTopic().getCurrentPage() - 1, true);
                    listView.setSelection(getContext().getTopic().getCurrentPage() - 1);

                    new AlertDialogBuilder(getContext())
                            .setTitle("Перейти к странице")
                            .setView(view)
                            .setPositiveButton("Перейти", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    getContext().openFromSt(listView.getCheckedItemPosition() * postsPerPage);
                                }
                            })
                            .setNegativeButton("Отмена", null)
                            .setCancelable(true)
                            .create()
                            .show();
                } catch (Throwable ex) {
                    Log.e(getContext(), ex);
                }
            }
        });

    }

    @JavascriptInterface
    public void plusRep(final String postId, final String userId, final String userNick) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().showChangeRep(postId, userId, userNick, "add", "Поднять репутацию");
            }
        });
    }

    @JavascriptInterface
    public void minusRep(final String postId, final String userId, final String userNick) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContext().showChangeRep(postId, userId, userNick, "minus", "Опустить репутацию");
            }
        });
    }

    @JavascriptInterface
    public void claim(final String postId) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                org.softeg.slartus.forpdaplus.classes.Post.claim(getContext(), new Handler(), getContext().getTopic().getId(), postId);
            }
        });

    }

    @JavascriptInterface
    public void showRepMenu(final String postId, final String userId, final String userNick, final String canPlus, final String canMinus) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final QuickAction mQuickAction = new QuickAction(getContext());
                ActionItem actionItem;


                int plusRepPosition = -1;
                if ("1".equals(canPlus)) {
                    actionItem = new ActionItem();

                    actionItem.setTitle("+1");
                    actionItem.setIcon(getContext().getResources().getDrawable(R.drawable.ic_menu_rating_good
                            //MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark)
                    ));
                    plusRepPosition = mQuickAction.addActionItem(actionItem);
                }

                int showRepPosition;

                actionItem = new ActionItem();
                actionItem.setIcon(getContext().getResources().getDrawable(R.drawable.ic_menu_view));
                actionItem.setTitle("Посмотреть");

                showRepPosition = mQuickAction.addActionItem(actionItem);

                int minusRepPosition = -1;
                if ("1".equals(canMinus)) {
                    actionItem = new ActionItem();

                    actionItem.setTitle("-1");
                    actionItem.setIcon(getContext().getResources().getDrawable(R.drawable.ic_menu_rating_bad
                            //MyApp.getInstance().isWhiteTheme() ?R.drawable.rating_good_white : R.drawable.rating_good_dark)
                    ));
                    minusRepPosition = mQuickAction.addActionItem(actionItem);
                }


                if (mQuickAction.getItemsCount() == 0) return;


                final int finalMinusRepPosition = minusRepPosition;
                final int finalShowRepPosition = showRepPosition;
                final int finalPlusRepPosition = plusRepPosition;
                mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
                    @Override
                    public void onItemClick(QuickAction source, int pos, int actionId) {
                        if (pos == finalMinusRepPosition) {
                            UserReputationFragment.minusRep(getContext(), new Handler(), postId, userId, userNick);
                        } else if (pos == finalShowRepPosition) {
                            getContext().showRep(userId);
                        } else if (pos == finalPlusRepPosition) {
                            UserReputationFragment.plusRep(getContext(), new Handler(), postId, userId, userNick);
                        }
                    }
                });

                mQuickAction.show(getContext().getWebView(), getContext().getWebView().getLastMotionEvent());
            }
        });

    }

    @JavascriptInterface
    public void go_gadget_show() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
               String url="http://4pda.ru/forum/index.php?&showtopic=" + getContext().getTopic().getId() + "&mode=show&poll_open=true&st="+
                       getContext().getTopic().getCurrentPage()* getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());
               getContext().showTheme(url);
            }
        });

    }

    @JavascriptInterface
    public void go_gadget_vote() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String url="http://4pda.ru/forum/index.php?&showtopic=" + getContext().getTopic().getId() + "&poll_open=true&st="+
                        getContext().getTopic().getCurrentPage()* getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());
                getContext().showTheme(url);
            }
        });

    }
}
