package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 21.03.14.
 */

import android.app.Activity;
import android.text.TextUtils;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.NotesTable;
import org.softeg.slartus.forpdaplus.fragments.NoteFragment;
import org.softeg.slartus.forpdaplus.notes.Note;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class NotesListFragment extends TopicsListFragment {
    public static final String TOPIC_ID_KEY="TOPIC_ID_KEY";
    public NotesListFragment() {

        super();
    }
    @Override
    protected ArrayList<? extends IListItem> loadTopics(Client client, ListInfo listInfo) throws IOException, ParseException {
        return NotesTable.getNotes(args!=null?args.getString(TOPIC_ID_KEY):null);
    }



    @Override
    public void saveCache() throws Exception {

    }

    @Override
    public void loadCache() throws IOException, IllegalAccessException, NoSuchFieldException, java.lang.InstantiationException {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        id = ListViewMethodsBridge.getItemId(getActivity(), position, id);
        if (id < 0 || getAdapter().getCount() <= id) return;

        Object o = getAdapter().getItem((int) id);
        if (o == null)
            return;
        final IListItem topic = (IListItem) o;

        if (TextUtils.isEmpty(topic.getId())) return;
        NoteFragment.showNote(topic.getId().toString());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;

        if (info.id  < 0 || getAdapter().getCount() <= info.id ) return;

        Object o = getAdapter().getItem((int) info.id );
        if (o == null)
            return;
        final IListItem topic = (IListItem) o;

        AddLinksSubMenu(menu, topic);

        menu.add("Удалить").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                new MaterialDialog.Builder(getContext())
                        .title("Подтвердите действие")
                        .content("Удалить заметку?")
                        .cancelable(true)
                        .negativeText("Отмена")
                        .positiveText("Удалить")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                try {
                                    NotesTable.delete(topic.getId().toString());
                                    mData.remove(topic);
                                    getAdapter().notifyDataSetChanged();
                                } catch (Throwable ex) {
                                    AppLog.e(getContext(), ex);
                                }
                            }
                        })
                        .show();
                return true;
            }
        });

    }

    private void AddLinksSubMenu(ContextMenu menu, IListItem topic) {
        try {
            Note note = NotesTable.getNote(topic.getId().toString());
            if (note != null) {
                ArrayList<Pair> links = note.getLinks();
                if (links.size() != 0) {
                    android.view.SubMenu linksMenu = menu.addSubMenu("Ссылки");
                    for (final Pair pair : links) {
                        linksMenu.add(pair.first.toString())
                                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        IntentActivity.tryShowUrl((Activity) getContext(), mHandler, pair.second.toString(), true, false, null);
                                        return true;
                                    }
                                });
                    }
                }
            }
        } catch (Throwable e) {
            AppLog.e(getContext(), e);
        }
    }
}
