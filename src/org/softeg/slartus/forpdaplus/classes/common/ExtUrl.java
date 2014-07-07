package org.softeg.slartus.forpdaplus.classes.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.notes.NoteDialog;

/**
 * Created by IntelliJ IDEA.
 * User: slartus
 * Date: 27.10.12
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class ExtUrl {

    public static void showInBrowser(Context context, String url) {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(Intent.createChooser(marketIntent, "Выберите"));
    }

    public static void shareIt(Context context, String subject, String text, String url) {
        Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendMailIntent.setData(Uri.parse(url));
        sendMailIntent.setType("text/plain");

        context.startActivity(Intent.createChooser(sendMailIntent, "Поделиться через.."));
    }

    public static void copyLinkToClipboard(Context context, String link) {
        StringUtils.copyToClipboard(context, link);
        Toast.makeText(context, "Ссылка скопирована в буфер обмена", Toast.LENGTH_SHORT).show();
    }

    public static SubMenu addUrlSubMenu(final android.os.Handler handler, final Context context, Menu menu, final String url
            , final CharSequence id, final String title) {
        SubMenu subMenu = menu.addSubMenu("Ссылка..");
        addUrlMenu(handler, context, subMenu, url, id, title);
        return subMenu;
    }

    public static void addUrlMenu(final android.os.Handler handler, final Context context, Menu menu, final String url, final String title) {
        addTopicUrlMenu(handler, context, menu, title, url, "", "", "", "", "", "");
    }

    public static void addTopicUrlMenu(final android.os.Handler handler, final Context context, Menu menu,
                                       final String title, final String url, final String body, final CharSequence topicId, final String topic,
                                       final String postId, final String userId, final String user) {

        menu.add("Открыть в браузере")
                .setIcon(R.drawable.ic_menu_browser)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showInBrowser(context, url);
                        return true;
                    }
                });

        menu.add("Поделиться ссылкой").setIcon(R.drawable.ic_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                shareIt(context, title, url, url);
                return true;
            }
        });

        menu.add("Скопировать ссылку").setIcon(R.drawable.ic_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                copyLinkToClipboard(context, url);
                return true;
            }
        });

        if (!TextUtils.isEmpty(topicId)) {
            menu.add("Создать заметку").setIcon(R.drawable.ic_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    NoteDialog.showDialog(handler, context,
                            title, body, url, topicId, topic,
                            postId, userId, user);
                    return true;
                }
            });
        }
    }

    public static void addUrlMenu(final Context context, Menu menu,
                                  final String title,
                                  final String url) {
        menu.add("Открыть в браузере")
                .setIcon(R.drawable.ic_menu_browser)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        showInBrowser(context, url);
                        return true;
                    }
                });

        menu.add("Поделиться ссылкой").setIcon(R.drawable.ic_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                shareIt(context, title, url, url);
                return true;
            }
        });

        menu.add("Скопировать ссылку").setIcon(R.drawable.ic_menu_share).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                copyLinkToClipboard(context, url);
                return true;
            }
        });
    }

    public static void showSelectActionDialog(final Context context,
                                              final String title,
                                              final String url) {

        CharSequence[] titles = {"Открыть в браузере", "Поделиться ссылкой", "Скопировать ссылку"};
        new AlertDialogBuilder(context)
                .setTitle(title)
                .setSingleChoiceItems(titles, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        switch (i) {
                            case 0:
                                showInBrowser(context, url);
                                break;
                            case 1:
                                shareIt(context, title, url, url);
                                break;
                            case 2:
                                copyLinkToClipboard(context, url);
                                break;
                        }
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(true)
                .create().show();



    }

    public static void addUrlMenu(final android.os.Handler handler, final Context context, Menu menu, final String url,
                                  final CharSequence id, final String title) {
        addTopicUrlMenu(handler, context, menu, title, url, url, id, title, "", "", "");
    }

    public static void showSelectActionDialog(final android.os.Handler handler, final Context context,
                                              final String title, final String body, final String url, final String topicId, final String topic,
                                              final String postId, final String userId, final String user) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(org.softeg.slartus.forpdaplus.R.layout.link_dialog, null);

        final AlertDialog dialog = new AlertDialogBuilder(context)
                .setTitle("Ссылка...")
                .setView(layout)
                .setNegativeButton("Отмена", null)
                .setCancelable(true)
                .create();

        assert layout != null;
        ((TextView) layout.findViewById(org.softeg.slartus.forpdaplus.R.id.text)).setText(url);
        layout.findViewById(org.softeg.slartus.forpdaplus.R.id.rbShowInBrowser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showInBrowser(context, url);
            }
        });
        layout.findViewById(org.softeg.slartus.forpdaplus.R.id.rbShareIt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                shareIt(context, title, url, url);
            }
        });
        layout.findViewById(org.softeg.slartus.forpdaplus.R.id.rbCopyToClipboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                copyLinkToClipboard(context, url);
            }
        });
        layout.findViewById(org.softeg.slartus.forpdaplus.R.id.rbNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                NoteDialog.showDialog(handler, context,
                        title, body, url, topicId, topic,
                        postId, userId, user);
            }
        });


        dialog.show();

    }

    public static void showSelectActionDialog(final android.os.Handler handler, final Context context, final String url) {
        showSelectActionDialog(handler, context, "", "", url, "", "", "", "", "");
    }
}
