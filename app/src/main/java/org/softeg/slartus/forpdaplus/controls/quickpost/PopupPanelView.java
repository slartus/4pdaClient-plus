package org.softeg.slartus.forpdaplus.controls.quickpost;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Parcelable;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.jetbrains.annotations.NotNull;
import org.softeg.slartus.forpdaplus.App;
import org.softeg.slartus.forpdaplus.R;
import org.softeg.slartus.forpdaplus.controls.quickpost.items.BBCodesAndSmilesItem;
import org.softeg.slartus.forpdaplus.controls.quickpost.items.BaseQuickView;
import org.softeg.slartus.forpdaplus.controls.quickpost.items.BbCodesItem;
import org.softeg.slartus.forpdaplus.controls.quickpost.items.EmoticsItem;
import org.softeg.slartus.forpdaplus.controls.quickpost.items.SettingsItem;

import java.util.ArrayList;
import java.util.List;


public class PopupPanelView {
    private final int mViewsFlags;
    public static final int VIEW_FLAG_SETTINGS = 2;
    public static final int VIEW_FLAG_EMOTICS = 4;
    public static final int VIEW_FLAG_BBCODES = 8;
    public static final int VIEW_FLAG_ALL = VIEW_FLAG_SETTINGS | VIEW_FLAG_EMOTICS | VIEW_FLAG_BBCODES;

    private int keyboardHeight;
    private EditText mPostEditText;
    private ImageButton advanced_button;
    private View emoticonsCover;

    private ViewPager mViewPager;
    private QuickPostPagerAdapter mQuickPostPagerAdapter;
    private View popUpView;
    private PopupWindow popupWindow;

    private boolean isKeyBoardVisible;
    private View parentLayout;
    private String mForumId, mTopicId, mAuthKey;

    public PopupPanelView(int views) {
        mViewsFlags = views;
    }

    public void createView(LayoutInflater inflater, ImageButton advanced_button, EditText editText) {

        popUpView = inflater.inflate(R.layout.quick_post_tabs_fragment, null);

        assert popUpView != null;
        mViewPager = popUpView.findViewById(R.id.pager1);

        this.advanced_button = advanced_button;
        mPostEditText = editText;
        mPostEditText.setOnFocusChangeListener((view, b) -> {
            if (b)
                hidePopupWindow();
        });

        mPostEditText.setOnClickListener(view -> hidePopupWindow());

        advanced_button.setOnClickListener(view -> toggleAdvPanelVisibility());
    }

    public void activityCreated(Activity activity, View view) {
        parentLayout = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        if (view == null)
            this.emoticonsCover = parentLayout.findViewById(R.id.footer_for_emoticons);
        else
            this.emoticonsCover = view.findViewById(R.id.footer_for_emoticons);

        final float popUpheight = App.getContext().getResources().getDimension(
                R.dimen.keyboard_height);
        changeKeyboardHeight((int) popUpheight);
        enablePopUpView();
        addGlobalLayoutListener();

        ArrayList<QuickPostItem> items = new ArrayList<>();

        int select = 0;
        if ((mViewsFlags & VIEW_FLAG_SETTINGS) != 0) {
            items.add(new SettingsItem());
            select = 1;
        }
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            items.add(new BBCodesAndSmilesItem());
        } else {
            if ((mViewsFlags & VIEW_FLAG_EMOTICS) != 0) {
                items.add(new EmoticsItem());
            }
            if ((mViewsFlags & VIEW_FLAG_BBCODES) != 0) {
                items.add(new BbCodesItem());
            }
        }
        mQuickPostPagerAdapter =
                new QuickPostPagerAdapter(activity, items);

        mViewPager.setAdapter(mQuickPostPagerAdapter);

        mViewPager.setCurrentItem(select);


    }

    public void setTopic(String forumId, String topicId, String authKey) {
        mForumId = forumId;
        mTopicId = topicId;
        mAuthKey = authKey;

        for (BaseQuickView view : mQuickPostPagerAdapter.getViews()) {
            if (view != null)
                view.setTopic(forumId, topicId, authKey);
        }
    }

    private void toggleAdvPanelVisibility() {
        if (!popupWindow.isShowing()) {
            showPopupWindow();
        } else {
            hidePopupWindow();
        }
    }


    private void showPopupWindow() {
        if (!popupWindow.isShowing()) {
            popupWindow.setHeight(keyboardHeight);
            if (isKeyBoardVisible) {
                emoticonsCover.setVisibility(LinearLayout.GONE);
            } else {
                emoticonsCover.setVisibility(LinearLayout.VISIBLE);
            }
            popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);
        }
        advanced_button.setImageResource(R.drawable.close_grey);
    }

    public void hidePopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        advanced_button.setImageResource(R.drawable.plus);
    }

    private void changeKeyboardHeight(int height) {
        if (height > 100) {
            keyboardHeight = height;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);
        }
    }

    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            parentLayout.getWindowVisibleDisplayFrame(r);

            int screenHeight = parentLayout.getRootView().getHeight();
            if (k == -1)
                k = screenHeight - r.bottom;
            int heightDifference = screenHeight - r.bottom - k;

            if (previousHeightDiffrence - heightDifference > 50) {
                hidePopupWindow();
            }

            isKeyBoardVisible = heightDifference > 100;
            if (previousHeightDiffrence != heightDifference)
                changeKeyboardHeight(heightDifference);
            previousHeightDiffrence = heightDifference;
        }
    };

    /**
     * Checking keyboard height and keyboard visibility
     */
    private int previousHeightDiffrence = 0;
    private int k = -1;

    private void addGlobalLayoutListener() {
        removeGlobalLayoutListener();
        if (parentLayout != null)
            parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    private void removeGlobalLayoutListener() {
        if (parentLayout != null)
            parentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    /**
     * Defining all components of emoticons keyboard
     */
    private void enablePopUpView() {
        // Creating a pop window for emoticons keyboard

        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                keyboardHeight, false);


        popupWindow.setOnDismissListener(() -> emoticonsCover.setVisibility(LinearLayout.GONE));
    }

    public void destroy() {
        try {
            if (popupWindow != null)
                popupWindow.dismiss();
            popupWindow = null;
            for (QuickPostItem item : mQuickPostPagerAdapter.mItems)
                if (item.getBaseQuickView() != null)
                    item.getBaseQuickView().onDestroy();
        } catch (Throwable ex) {
            Log.e("PopupPanelView", ex.toString());
        }
    }

    public void pause() {
        try {
            removeGlobalLayoutListener();
            hidePopupWindow();
            for (QuickPostItem item : mQuickPostPagerAdapter.mItems)
                if (item.getBaseQuickView() != null)
                    item.getBaseQuickView().onPause();
        } catch (Throwable ex) {
            Log.e("PopupPanelView", ex.toString());
        }
    }

    public void resume() {
        try {
            addGlobalLayoutListener();
            for (QuickPostItem item : mQuickPostPagerAdapter.mItems)
                if (item.getBaseQuickView() != null)
                    item.getBaseQuickView().onResume();
        } catch (Throwable ex) {
            Log.e("PopupPanelView", ex.toString());
        }
    }

    public class QuickPostPagerAdapter extends PagerAdapter {
        private final List<QuickPostItem> mItems;
        private final BaseQuickView[] mViews;
        private final Activity mActivity;


        public QuickPostPagerAdapter(Activity activity, List<QuickPostItem> items) {
            mActivity = activity;
            mItems = items;
            mViews = new BaseQuickView[mItems.size()];
        }

        @NotNull
        @Override
        public Object instantiateItem(@NotNull ViewGroup container, int position) {
            if (mViews[position] == null) {
                mViews[position] = mItems.get(position).createView(mActivity, mPostEditText);
                mViews[position].setTopic(mForumId, mTopicId, mAuthKey);
            }
            container.addView(mViews[position], 0);
            return mViews[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NotNull Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, @NotNull Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return mActivity.getString(mItems.get(position).getTitle());
        }

        public BaseQuickView[] getViews() {
            return mViews;
        }

    }
}
