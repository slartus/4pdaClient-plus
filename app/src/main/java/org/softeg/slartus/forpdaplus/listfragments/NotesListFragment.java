package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 21.03.14.
 */

import android.app.Activity;
import android.text.TextUtils;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.ListInfo;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.db.NotesTable;
import org.softeg.slartus.forpdaplus.fragments.NoteFragment;
import org.softeg.slartus.forpdaplus.notes.Note;
import org.softeg.slartus.forpdaplus.tabs.ListViewMethodsBridge;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
    public void saveCache() {

    }

    @Override
    public void loadCache(){

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        if(!v.hasWindowFocus()) return;
        id = ListViewMethodsBridge.getItemId(getActivity(), position, id);
        if (id < 0 || getAdapter().getCount() <= id) return;

        Object o = getAdapter().getItem((int) id);
        if (o == null)
            return;
        final IListItem topic = (IListItem) o;

        if (TextUtils.isEmpty(topic.getId())) return;

        try {
            Note note = NotesTable.getNote(topic.getId().toString());
            if (note != null) {
                if (note.Url!=null) {
                    IntentActivity.tryShowUrl((Activity) getContext(), getMHandler(), note.Url, true, false, null);
                }else {
                    NoteFragment.showNote(topic.getId().toString());
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

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

        final List<MenuListDialog> list = new ArrayList<>();
        AddLinksSubMenu(list, topic);
        list.add(new MenuListDialog(App.getContext().getString(R.string.delete), () -> new MaterialDialog.Builder(getContext())
                .title(R.string.confirm_action)
                .content(R.string.ask_delete_note)
                .cancelable(true)
                .negativeText(R.string.cancel)
                .positiveText(R.string.delete)
                .onPositive((dialog, which) -> {
                    try {
                        NotesTable.delete(topic.getId().toString());
                        getMData().remove(topic);
                        getAdapter().notifyDataSetChanged();
                    } catch (Throwable ex) {
                        AppLog.e(getContext(), ex);
                    }
                })
                .show()));
        ExtUrl.showContextDialog(getContext(), null, list);

    }

    private void AddLinksSubMenu(List<MenuListDialog> list, final IListItem topic) {
        try {
            final Note note = NotesTable.getNote(topic.getId().toString());
            if (note != null) {
                final ArrayList<Pair> links = note.getLinks();
                if (links.size() != 0) {
                    list.add(new MenuListDialog(App.getContext().getString(R.string.links), () -> {
                        final List<MenuListDialog> list1 = new ArrayList<>();
                        for (final Pair pair : links) {
                            list1.add(new MenuListDialog(pair.first.toString(), () -> IntentActivity.tryShowUrl((Activity) getContext(), getMHandler(), pair.second.toString(), true, false, null)));
                        }
                        ExtUrl.showContextDialog(getContext(), null, list1);
                    }));
                }
            }
        } catch (Throwable e) {
            AppLog.e(getContext(), e);
        }
    }

}
