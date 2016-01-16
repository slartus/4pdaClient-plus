package org.softeg.slartus.forpdaplus.devdb;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MainActivity;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.TabDrawerMenu;
import org.softeg.slartus.forpdaplus.classes.common.ExtUrl;
import org.softeg.slartus.forpdaplus.common.AppLog;
import org.softeg.slartus.forpdaplus.devdb.helpers.DevDbUtils;
import org.softeg.slartus.forpdaplus.devdb.helpers.FLifecycleUtil;
import org.softeg.slartus.forpdaplus.devdb.helpers.ParseHelper;
import org.softeg.slartus.forpdaplus.devdb.helpers.ParsedModel;
import org.softeg.slartus.forpdaplus.fragments.GeneralFragment;

public class ParentFragment extends GeneralFragment {

    private static final String DEVICE_ID_KEY = "DeviceId";
    private static final String POSITION_ID = "position";
    private static final String TOOLBAR_TITLE = "title";
    private String m_DeviceId;
    private int m_Position;
    private String m_Title;

    private View rootView;
    private int LAYOUT = R.layout.dev_db_parent_fragment;
    private ViewPager viewPager;
    private MaterialDialog dialog;
    private DevDbViewPagerAdapter adapter;

    public ParentFragment() {
    }

    public static ParentFragment newInstance(Bundle args) {
        ParentFragment fragment = new ParentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void showDevice(String deviceId, String title, int position) {
        Bundle args = new Bundle();
        args.putString(DEVICE_ID_KEY, deviceId);
        args.putInt(POSITION_ID, position);
        args.putString(TOOLBAR_TITLE, title);
        MainActivity.addTab(title, deviceId + "more", newInstance(args));
    }
    public static void showDevice(String deviceId) {
        Bundle args = new Bundle();
        args.putString(DEVICE_ID_KEY, deviceId);
        args.putInt(POSITION_ID, 0);
        args.putString(TOOLBAR_TITLE, "ForPDA");
        MainActivity.addTab(deviceId + "more", newInstance(args));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onCreate(savedInstanceState);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);

        setHasOptionsMenu(true);
        Bundle extras = getArguments();
        assert extras != null;
        m_DeviceId = extras.getString(DEVICE_ID_KEY);
        m_Position = extras.getInt(POSITION_ID);
        m_Title = extras.getString(TOOLBAR_TITLE);
        setTitle(m_Title);
        loading();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add("Ссылка")
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        ExtUrl.showSelectActionDialog(getMainActivity(), "Ссылка", m_DeviceId);
                        return true;
                    }
                });
        this.menu = menu;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!DevDbUtils.isAndroid5())
            getMainActivity().getToolbarShadow().setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!DevDbUtils.isAndroid5())
            getMainActivity().getToolbarShadow().setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        rootView = inflater.inflate(LAYOUT, container, false);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
        getMainActivity().getToolbarShadow().setVisibility(View.GONE);

        if (DevDbUtils.isAndroid5()) {
            getMainActivity().getAppBarLayout().setElevation(0);
            rootView.findViewById(R.id.dev_toolbar_shadow).setVisibility(View.GONE);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onViewCreated(view, savedInstanceState);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);

    }

    @Override
    public void onSaveInstanceState(android.os.Bundle outState) {
        outState.putString(DEVICE_ID_KEY, m_DeviceId);
        outState.putInt(POSITION_ID, m_Position);
        outState.putString(TOOLBAR_TITLE, m_Title);
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onSaveInstanceState(outState);
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
    }

    @Override
    public void onDestroy() {
//        recLifeCycle(getClass(), CALL_TO_SUPER);
        super.onDestroy();
//        recLifeCycle(getClass(), RETURN_FROM_SUPER);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private Menu menu;
    @Override
    public Menu getMenu() {
        return menu;
    }


    @Override
    public boolean closeTab() {
        return false;
    }

    private void showTabs(int position) {
        viewPager.setCurrentItem(position);
    }

    private void initUI(ParsedModel parsed) {
        m_Title = parsed.getTitle();
        setTitle(m_Title);
        App.getInstance().getTabByTag(getTag()).setTitle(m_Title);
        TabDrawerMenu.notifyDataSetChanged();
        viewPager = (ViewPager) rootView.findViewById(R.id.devDbViewPager);
        adapter = new DevDbViewPagerAdapter(getMainActivity(), getChildFragmentManager(), parsed);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(mChangeListener);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.devDbTabLayout);
        tabLayout.setupWithViewPager(viewPager);
        showTabs(m_Position);
    }

    private ViewPager.OnPageChangeListener mChangeListener = new ViewPager.OnPageChangeListener() {
        int currentPosition = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            FLifecycleUtil showFragment = (FLifecycleUtil) adapter.getItem(position);
            showFragment.onResumeFragment();
            FLifecycleUtil hideFragment = (FLifecycleUtil) adapter.getItem(currentPosition);
            hideFragment.onPauseFragment();
            currentPosition = position;

        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    private void loading() {
        HelperTask task = new HelperTask();
        task.execute(m_DeviceId);
    }

    public class HelperTask extends AsyncTask<String, Void, Boolean> {

        private Throwable ex;
        private ParseHelper mParseHelper;
        private ParsedModel parsed;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                parsed = new ParseHelper().parseHelper(Client.getInstance().performGet(params[0]));
                return true;
            } catch (Throwable e) {
                ex = e;
                return false;
            }
        }

        protected void onPreExecute() {
            dialog = new MaterialDialog.Builder(getActivity())
                    .progress(true, 0)
                    .cancelable(false)
                    .content("Загрузка")
                    .build();
            dialog.show();
        }

        protected void onPostExecute(final Boolean success) {
            if (success) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                initUI(parsed);
            } else {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (ex != null)
                    AppLog.e(App.getContext(), ex);
            }
        }
    }
}
