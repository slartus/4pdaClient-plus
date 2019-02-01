package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 05.05.2014.
 */

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;

import org.softeg.slartus.forpdaapi.IListItem;
import org.softeg.slartus.forpdaapi.TopicApi;
import org.softeg.slartus.forpdaapi.post.PostAttach;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.IntentActivity;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.MenuListDialog;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.download.DownloadsService;
import org.softeg.slartus.forpdaplus.listtemplates.TopicAttachmentBrickInfo;

import java.util.ArrayList;
import java.util.List;

public class TopicAttachmentListFragment extends BaseTaskListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArrow();
    }

    @Override
    public void onResume() {
        super.onResume();
        setArrow();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static void showActivity(Context context, CharSequence topicId){
        Bundle args=new Bundle();
        args.putString(TOPIC_ID_KEY,topicId.toString());
        MainActivity.showListFragment(topicId.toString(), TopicAttachmentBrickInfo.NAME, args);
    }
    public TopicAttachmentListFragment() {

        super();
    }
    public static final String TOPIC_ID_KEY="TOPIC_ID_KEY";
    @Override
    protected boolean inBackground(boolean isRefresh) throws Throwable {
        mLoadResultList = TopicApi.getTopicAttachment(Client.getInstance(), args.getString(TOPIC_ID_KEY));
        return true;
    }

    @Override
    protected void deliveryResult(boolean isRefresh) {
        if (isRefresh)
            getMData().clear();
        List<CharSequence> ids=new ArrayList<>();
        for (IListItem item : getMData()) {
            ids.add(item.getId());
        }
        for (IListItem item : mLoadResultList) {
            if(ids.contains(item.getId()))
                continue;
            getMData().add(item);
        }

        mLoadResultList.clear();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {

            getActivity().openContextMenu(v);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.id == -1) return;
        Object o = getAdapter().getItem((int) info.id );
        if (o == null)
            return;
        final IListItem item = (IListItem) o;
        if (TextUtils.isEmpty(item.getId())) return;
        final PostAttach attach=(PostAttach)item;

        final List<MenuListDialog> list = new ArrayList<>();
        list.add(new MenuListDialog(getString(R.string.do_download), new Runnable() {
            @Override
            public void run() {
                DownloadsService.download(getActivity(), attach.getUrl().toString().replaceFirst("//4pda.ru", "").trim(), false);
            }
        }));
        list.add(new MenuListDialog(getString(R.string.jump_to_page), new Runnable() {
            @Override
            public void run() {
                IntentActivity.showTopic(attach.getPostUrl());
            }
        }));
        ExtUrl.showContextDialog(getContext(), null, list);
    }

}
