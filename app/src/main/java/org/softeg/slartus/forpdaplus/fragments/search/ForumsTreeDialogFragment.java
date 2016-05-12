package org.softeg.slartus.forpdaplus.fragments.search;/*
 * Created by slinkin on 24.04.2014.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.classes.Forum;
import org.softeg.slartus.forpdaplus.classes.ForumsAdapter;
import org.softeg.slartus.forpdaplus.common.AppLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ForumsTreeDialogFragment extends DialogFragment {

    public static final String IS_DIALOG_KEY="IS_DIALOG_KEY";
    public static final String FORUM_IDS_KEY="FORUM_IDS_KEY";
    public static final int OK_RESULT = 0;
    public static final int CANCEL_RESULT = 1;
    private ListView m_ListView;
    private Spinner m_Spinner;
    private ForumsAdapter m_ListViewAdapter;
    private SpinnerAdapter m_SpinnerAdapter;
    private View m_Progress;
    private ArrayList<CheckableForumItem> m_Forums = new ArrayList<>();

    public static ForumsTreeDialogFragment newInstance(Boolean dialog, Collection<String> checkedForumIds) {
        Bundle args = new Bundle();
        args.putBoolean(IS_DIALOG_KEY, dialog);
        String[] ar=new String[checkedForumIds.size()];
        args.putStringArray(FORUM_IDS_KEY, checkedForumIds.toArray(ar));
        ForumsTreeDialogFragment fragment = new ForumsTreeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public android.view.View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
//        if (getArguments().getBoolean(IS_DIALOG_KEY))
//            return super.onCreateView(inflater, container, savedInstanceState);
//        View view = inflater.inflate(R.layout.forums_tree_dialog_fragment, null);
//        assert view != null;
//        m_ListView = (ListView) view.findViewById(android.R.id.list);
//        initListView();
//        m_Spinner = (Spinner) view.findViewById(R.id.selected_spinner);
//        initSpinner();
//        m_Progress = view.findViewById(R.id.progress);
//        return view;
//
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.forums_tree_dialog_fragment, null);
        assert view != null;
        m_ListView = (ListView) view.findViewById(android.R.id.list);
        initListView();
        m_Spinner = (Spinner) view.findViewById(R.id.selected_spinner);
        initSpinner();

        m_Progress = view.findViewById(R.id.progress);
        MaterialDialog dialog =new MaterialDialog.Builder(getActivity())
                .customView(view,false)
                .title(R.string.forum)
                .positiveText(R.string.accept)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent=new Intent();
                        String[] ar=new String[m_ListViewAdapter.getCheckedIds().size()];
                        intent.putExtra(FORUM_IDS_KEY, m_ListViewAdapter.getCheckedIds().toArray(ar));

                        getTargetFragment().onActivityResult(SearchSettingsDialogFragment.FORUMS_DIALOG_REQUEST, OK_RESULT, intent);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        getTargetFragment().onActivityResult(SearchSettingsDialogFragment.FORUMS_DIALOG_REQUEST, CANCEL_RESULT, null);
                    }
                })
                .build();
        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }


    private void initListView() {
        m_ListView.setFastScrollEnabled(true);

        m_ListViewAdapter = new ForumsAdapter(getActivity(),
                m_Forums);
        m_ListView.setAdapter(m_ListViewAdapter);

        m_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                m_ListViewAdapter.toggleChecked(position);

                m_SpinnerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initSpinner() {
        m_SpinnerAdapter = new SpinnerAdapter(getActivity(), m_Forums);
        m_Spinner.setAdapter(m_SpinnerAdapter);

        m_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (l == 0)
                    return;
                CheckableForumItem item = m_SpinnerAdapter.getItem((int) l);
                m_ListView.setSelection(m_ListViewAdapter.getPosition(item));
                m_Spinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startLoadData();
    }

    private void startLoadData() {
        new Task().execute();
    }

    private void deliveryResult(org.softeg.slartus.forpdaplus.classes.Forum result) {
        m_Forums.clear();
        addForumCaptions(m_Forums, result, null, 0, Arrays.asList(getArguments().getStringArray(FORUM_IDS_KEY)));

        m_ListViewAdapter.notifyDataSetChanged();
        m_SpinnerAdapter.notifyDataSetChanged();
    }

    private void setLoading(boolean b) {
        m_Progress.setVisibility(b ? View.VISIBLE : View.GONE);
    }


    private void addForumCaptions(ArrayList<CheckableForumItem> forums, Forum forum,
                                  Forum parentForum,
                                  int level, Collection<String> checkIds) {
        CheckableForumItem checkableForumItem = null;
        if (parentForum == null) {
            checkableForumItem = new CheckableForumItem("all", ">> Все форумы");
        } else if (!parentForum.getId().equals(forum.getId())) {
            checkableForumItem = new CheckableForumItem(forum.getId(), forum.getTitle());
        }
        if (checkableForumItem != null) {
            checkableForumItem.level = level;
            checkableForumItem.IsChecked = checkIds.contains(checkableForumItem.Id);
            forums.add(checkableForumItem);
        }

        int childSize = forum.getForums().size();

        for (int i = 0; i < childSize; i++) {
            addForumCaptions(forums, forum.getForums().get(i), forum, level + 1, checkIds);
        }
    }

    public class Task extends AsyncTask<Boolean, Void, org.softeg.slartus.forpdaplus.classes.Forum> {
        protected Throwable mEx;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setLoading(true);
        }

        @Override
        protected org.softeg.slartus.forpdaplus.classes.Forum doInBackground(Boolean[] p1) {
            try {
                return Client.getInstance().loadForums();
            } catch (Throwable e) {
                mEx = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(org.softeg.slartus.forpdaplus.classes.Forum result) {
            super.onPostExecute(result);
            if (result != null && !isCancelled()) {
                deliveryResult(result);
            }
            setLoading(false);

            if (mEx != null)
                AppLog.e(getActivity(), mEx, new Runnable() {
                    @Override
                    public void run() {
                        startLoadData();
                    }
                });
        }

    }

    public class SpinnerAdapter extends BaseAdapter {

        private ArrayList<CheckableForumItem> mForums;
        private LayoutInflater m_Inflater;

        public SpinnerAdapter(Context context, ArrayList<CheckableForumItem> forums) {
            super();
            mForums = forums;
            m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            int c = 1;
            for (CheckableForumItem f : mForums) {
                if (f.IsChecked)
                    c++;
            }
            return c;
        }

        @Override
        public CheckableForumItem getItem(int i) {
            if (i == 0) {
                return new CheckableForumItem("", "Всего: " + (getCount() - 1));
            }
            int c = 1;
            for (CheckableForumItem f : mForums) {
                if (f.IsChecked && c == i)
                    return f;
                if (f.IsChecked)
                    c++;
            }
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            View rowView = convertView;
            if (rowView == null) {
                rowView = m_Inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
                holder = new ViewHolder();
                assert rowView != null;
                holder.text = (TextView) rowView
                        .findViewById(android.R.id.text1);

                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            CheckableForumItem item = this.getItem(position);

            holder.text.setText(item.Title);

            return rowView;
        }

        public class ViewHolder {
            TextView text;
        }
    }

}
