package org.softeg.slartus.forpdaplus.listfragments;/*
 * Created by slinkin on 21.03.14.
 */


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo;
import org.softeg.slartus.forpdaplus.listtemplates.ListCore;

import java.util.ArrayList;

public class BricksListDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {
    private static final String BRICK_NAMES_KEY = "BRICK_NAMES_KEY";
    private static final String DIALOG_ID_KEY = "DIALOG_ID_KEY";
    private static final String TAG = "BricksListDialogFragment";

    public static final String QUICK_LIST_ID = "QUICK_LIST_ID";
    public static final String CREATE_POST_ID = "CREATE_POST_ID";

    public BricksListDialogFragment(){
        super();
    }
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Object o = m_ListView.getAdapter().getItem((int) l);
        if (o == null)
            return;
        BrickInfo brickInfo = (BrickInfo) o;


        ((IBricksListDialogCaller) getActivity()).onBricksListDialogResult(this.getDialog(),
                args.getString(DIALOG_ID_KEY), brickInfo, args);
    }


    public interface IBricksListDialogCaller {
        Context getContext();
        FragmentManager getSupportFragmentManager();

        void onBricksListDialogResult(android.content.DialogInterface dialog, String dialogId,
                                      BrickInfo brickInfo, Bundle args);
    }

    static BricksListDialogFragment newInstance(String dialogId, String[] brickNames, Bundle args) {
        BricksListDialogFragment f = new BricksListDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putStringArray(BRICK_NAMES_KEY, brickNames);
        arguments.putString(DIALOG_ID_KEY, dialogId);
        if (args != null)
            arguments.putAll(args);
        f.setArguments(arguments);
        return f;
    }

    public static void showDialog(IBricksListDialogCaller bricksListDialogCaller, String dialogId,
                                  String[] brickNames, Bundle args) {
        FragmentTransaction ft = bricksListDialogCaller.getSupportFragmentManager().beginTransaction();
        Fragment prev = bricksListDialogCaller.getSupportFragmentManager().findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = BricksListDialogFragment.newInstance(dialogId, brickNames, args);
        newFragment.show(ft, TAG);
    }

    private Bundle args;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            args = getArguments();
        }
        if (savedInstanceState != null) {

            args = savedInstanceState;
        }
    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        if (args != null)
            outState.putAll(args);


        super.onSaveInstanceState(outState);
    }

    private ListView m_ListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.select_list);
        View v = inflater.inflate(R.layout.bricks_list_dialog, container, false);
        assert v != null;
        m_ListView = v.findViewById(android.R.id.list);
        return v;
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        m_ListView.setAdapter(new ListAdapter(getActivity(),
                ListCore.createBricks(args.getStringArray(BRICK_NAMES_KEY))));
        m_ListView.setOnItemClickListener(this);
    }


    public class ListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private ArrayList<BrickInfo> mData;

        public ListAdapter(Context context, ArrayList<BrickInfo> data) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mData = data;
        }

        public void setData(ArrayList<BrickInfo> data) {
            mData = data;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int i) {
            return mData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public android.view.View getView(int position, android.view.View view, android.view.ViewGroup parent) {
            final ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder();
                holder.text1 = view.findViewById(android.R.id.text1);
                view.setTag(holder);

            } else {
                holder = (ViewHolder) view.getTag();
            }
            BrickInfo item = mData.get(position);
            holder.text1.setText(item.getTitle());
            return view;
        }

        class ViewHolder {
            TextView text1;

        }
    }
}
