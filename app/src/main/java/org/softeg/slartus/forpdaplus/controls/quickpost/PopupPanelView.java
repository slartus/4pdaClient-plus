package org.softeg.slartus.forpdaplus.controls.quickpost;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

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
    private int mViewsFlags = VIEW_FLAG_ALL;
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
        mViewPager = (ViewPager) popUpView.findViewById(R.id.pager1);

        this.advanced_button = advanced_button;
        mPostEditText = editText;
        mPostEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    hidePopupWindow();
            }
        });

        mPostEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidePopupWindow();
            }
        });

        advanced_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAdvPanelVisibility();
            }
        });
    }
    public void activityCreated(Activity activity){
        activityCreated(activity, null);
    }
    public void activityCreated(Activity activity, View view) {
        parentLayout = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        if(view==null)
            this.emoticonsCover = parentLayout.findViewById(R.id.footer_for_emoticons);
        else
            this.emoticonsCover = view.findViewById(R.id.footer_for_emoticons);

        final float popUpheight = App.getContext().getResources().getDimension(
                R.dimen.keyboard_height);
        changeKeyboardHeight((int) popUpheight);
        enablePopUpView();
        checkKeyboardHeight(parentLayout);


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
        advanced_button.setImageResource(R.drawable.ic_close_grey600_24dp);
    }

    public void hidePopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        advanced_button.setImageResource(R.drawable.ic_plus_grey600_24dp);
    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height minimum height by which we can make sure actual keyboard is
     *               open or not
     */
    private void changeKeyboardHeight(int height) {
        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
            emoticonsCover.setLayoutParams(params);
        }
    }

    /**
     * Checking keyboard height and keyboard visibility
     */
    private int previousHeightDiffrence = 0;
    private int heightDifference;
    private int k = -1;
    private Rect r;
    private String toast="";
    @SuppressWarnings("ConstantConditions")
    private void checkKeyboardHeight(final View parentLayout) {

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {


                        r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight = parentLayout.getRootView()
                                .getHeight();
                        if (k == -1)
                            k = screenHeight - r.bottom;
                        heightDifference = screenHeight - (r.bottom) - k;

                        if (previousHeightDiffrence - heightDifference > 50) {
                            hidePopupWindow();
                        }

                        previousHeightDiffrence = heightDifference;
                        if (heightDifference > 100) {
                            isKeyBoardVisible = true;
                            changeKeyboardHeight(heightDifference);
                        } else {
                            isKeyBoardVisible = false;
                        }
                        /*if(!toast.equals(screenHeight+" : "+k+" : "+heightDifference+" : "+r.bottom+" : "+isKeyBoardVisible)){
                            toast = screenHeight+" : "+k+" : "+heightDifference+" : "+r.bottom+" : "+isKeyBoardVisible;
                            Toast.makeText(App.getContext(), toast, Toast.LENGTH_SHORT).show();
                            Log.e("kek", toast);
                        }*/
                    }
                }
        );
    }

    /**
     * Defining all components of emoticons keyboard
     */
    private void enablePopUpView() {
        // Creating a pop window for emoticons keyboard

        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT,
                keyboardHeight, false);


        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                emoticonsCover.setVisibility(LinearLayout.GONE);
            }
        });
    }

    public void destroy() {
        try {
            if (popupWindow != null)
                popupWindow.dismiss();
            popupWindow = null;
            for(QuickPostItem item:mQuickPostPagerAdapter.mItems)
                    item.getBaseQuickView().onDestroy();
        } catch (Throwable ex) {
            Log.e("PopupPanelView", ex.toString());
        }
    }
    public void pause(){
        try {
            hidePopupWindow();
            for(QuickPostItem item:mQuickPostPagerAdapter.mItems)
                item.getBaseQuickView().onPause();
        } catch (Throwable ex) {
            Log.e("PopupPanelView", ex.toString());
        }
    }
    public void resume(){
        try {
            for(QuickPostItem item:mQuickPostPagerAdapter.mItems)
                item.getBaseQuickView().onPause();
        } catch (Throwable ex) {
            Log.e("PopupPanelView", ex.toString());
        }
    }

    public class QuickPostPagerAdapter extends PagerAdapter {
        private List<QuickPostItem> mItems;
        private BaseQuickView[] mViews;
        private Activity mActivity;


        public QuickPostPagerAdapter(Activity activity, List<QuickPostItem> items) {
            mActivity = activity;
            mItems = items;
            mViews = new BaseQuickView[mItems.size()];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mViews[position] == null) {
                mViews[position] = mItems.get(position).createView(mActivity, mPostEditText);
                mViews[position].setTopic(mForumId, mTopicId, mAuthKey);
            }
            container.addView(mViews[position], 0);
            return mViews[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
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
            return mItems.get(position).getTitle();
        }

        public BaseQuickView[] getViews() {
            return mViews;
        }

    }
}
