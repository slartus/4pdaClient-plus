package org.softeg.slartus.forpdaplus.topicview;/*
 * Created by slinkin on 09.07.2014.
 */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
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

import com.afollestad.materialdialogs.MaterialDialog;

import net.londatiga.android3d.ActionItem;
import net.londatiga.android3d.QuickAction;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.ForumUser;
import org.softeg.slartus.forpdaplus.classes.TopicAttaches;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.listfragments.ListFragmentActivity;
import org.softeg.slartus.forpdaplus.listfragments.TopicReadersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.TopicWritersListFragment;
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment;
import org.softeg.slartus.forpdaplus.listtemplates.TopicReadersBrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.TopicWritersBrickInfo;


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
                new MaterialDialog.Builder(getContext())
                        .title("Вложения")
                        .items(topicAttaches.getList())
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog materialDialog, Integer[] which, CharSequence[] charSequence) {
                                for (int i = 0; i < which.length; i++) {
                                    selection[which[i]] = true;
                                }
                                return true;
                            }
                        })
                        .alwaysCallMultiChoiceCallback()
                        .positiveText("Скачать")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                if (!Client.getInstance().getLogined()) {
                                    new MaterialDialog.Builder(getContext())
                                            .title("Внимание!")
                                            .content("Для скачивания файлов с сайта необходимо залогиниться!")
                                            .positiveText("ОК")
                                            .show();
                                    return;
                                }
                                for (int j = 0; j < selection.length; j++) {
                                    if (!selection[j]) continue;
                                    DownloadsService.download(getContext(), topicAttaches.get(j).getUri(), false);
                                    selection[j]=false;
                                }
                            }
                        })
                        .negativeText("Отмена")
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
                new MaterialDialog.Builder(getContext())
                        .title("Подтвердите действие")
                        .content("Понизить рейтинг сообщения?")
                        .positiveText("Понизить")
                        .negativeText("Отмена")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                org.softeg.slartus.forpdaplus.classes.Post.minusOne(getContext(), new Handler(), postId);
                            }
                        }).show();
            }
        })
        ;
    }

    @JavascriptInterface
    public void postVoteGood(final String postId) {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialDialog.Builder(getContext())
                        .title("Подтвердите действие")
                        .content("Повысить рейтинг сообщения?")
                        .positiveText("Повысить")
                        .negativeText("Отмена")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                org.softeg.slartus.forpdaplus.classes.Post.plusOne(getContext(), new Handler(), postId);
                            }
                        }).show();
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
                    Bundle args=new Bundle();
                    args.putString(TopicReadersListFragment.TOPIC_ID_KEY, getContext().getTopic().getId());
                    ListFragmentActivity.showListFragment(context, TopicReadersBrickInfo.NAME, args);
                } catch (ActivityNotFoundException e) {
                    AppLog.e(getContext(), e);
                }
            }
        });

    }

    @JavascriptInterface
    public void showWriters() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getContext().getTopic() == null) {
                    Toast.makeText(getContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle args=new Bundle();
                    args.putString(TopicWritersListFragment.TOPIC_ID_KEY, getContext().getTopic().getId());
                    ListFragmentActivity.showListFragment(context, TopicWritersBrickInfo.NAME, args);

                }
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
                    ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(),
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
                                AppLog.e(getContext(), ex);
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
                                AppLog.e(getContext(), ex);
                            } finally {
                                txtNumberPage.setTag(true);
                            }
                        }
                    });

                    listView.setItemChecked(getContext().getTopic().getCurrentPage() - 1, true);
                    listView.setSelection(getContext().getTopic().getCurrentPage() - 1);

                    new MaterialDialog.Builder(getContext())
                            .title("Перейти к странице")
                            .customView(view,false)
                            .positiveText("Перейти")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    getContext().openFromSt(listView.getCheckedItemPosition() * postsPerPage);
                                }
                            })
                            .negativeText("Отмена")
                            .cancelable(true)
                            .show();
                } catch (Throwable ex) {
                    AppLog.e(getContext(), ex);
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

                    actionItem.setTitle("Поднять (+1)");
                    plusRepPosition = mQuickAction.addActionItem(actionItem);
                }

                int showRepPosition;

                actionItem = new ActionItem();
                actionItem.setTitle("Посмотреть");

                showRepPosition = mQuickAction.addActionItem(actionItem);

                int minusRepPosition = -1;
                if ("1".equals(canMinus)) {
                    actionItem = new ActionItem();

                    actionItem.setTitle("Опустить (-1)");
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

                String url = "http://4pda.ru/forum/index.php?&showtopic=" + getContext().getTopic().getId() + "&mode=show&poll_open=true&st=" +
                        getContext().getTopic().getCurrentPage() * getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());
                getContext().showTheme(url);
            }
        });

    }

    @JavascriptInterface
    public void go_gadget_vote() {
        getContext().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String url = "http://4pda.ru/forum/index.php?&showtopic=" + getContext().getTopic().getId() + "&poll_open=true&st=" +
                        getContext().getTopic().getCurrentPage() * getContext().getTopic().getPostsPerPageCount(getContext().getLastUrl());
                getContext().showTheme(url);
            }
        });

    }

}
