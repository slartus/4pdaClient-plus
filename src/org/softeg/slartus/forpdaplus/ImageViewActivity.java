package org.softeg.slartus.forpdaplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.softeg.slartus.forpdaplus.common.Log;
import org.softeg.slartus.forpdaplus.download.DownloadsService;

/**
 * User: slinkin
 * Date: 28.11.11
 * Time: 14:04
 */
public class ImageViewActivity extends BaseFragmentActivity {
    private static final String URL_KEY = "url";

    private String mUrl;
    private MenuFragment mFragment1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setHomeButtonEnabled(false);

        MyImageView myImageView = new MyImageView(this, getWindowManager());
        setContentView(myImageView);


        createMenu();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mUrl = extras.getString(URL_KEY);

        myImageView.setImageDrawable(mUrl);
    }

    private void createMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    private String getUrl() {
        return mUrl;
    }

    public static void showImageUrl(Context activity, String imgUrl) {
        try {
            Intent intent = null;

                    intent = new Intent(activity, ImageViewActivity.class);
                    intent.putExtra(ImageViewActivity.URL_KEY, imgUrl);

            activity.startActivity(intent);
        } catch (Exception ex) {
            Log.e(activity, ex);
        }
//        ActionSelectDialogFragment.execute(activity,
//                "Действие по умолчанию",
//                "image.viewer",
//                activity.getResources().getStringArray(R.array.ImageViwersTitles),
//                activity.getResources().getStringArray(R.array.ImageViwersValues),
//                new ActionSelectDialogFragment.OkListener() {
//                    @Override
//                    public void execute(CharSequence value) {
//                        try {
//                            Intent intent = null;
//                            switch (value.toString()) {
//                                case "system":
//                                    intent = new Intent(Intent.ACTION_VIEW);
//                                    intent.setDataAndType(Uri.parse(imgUrl), "image/*");
//                                    break;
//                                case "player":
//                                    intent = new Intent(activity, ImageViewActivity.class);
//                                    intent.putExtra(ImageViewActivity.URL_KEY, imgUrl);
//                                    break;
//                            }
//                            activity.startActivity(intent);
//                        } catch (Exception ex) {
//                            Log.e(activity, ex);
//                        }
//                    }
//                }, null
//        );

    }

    public static final class MenuFragment extends Fragment {
        public MenuFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            MenuItem item;

            item = menu.add("Скачать").setIcon(R.drawable.ic_menu_download);

            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    DownloadsService.download(getInterface(), getInterface().getUrl(),false);
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Закрыть").setIcon(R.drawable.ic_menu_close_clear_cancel);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem menuItem) {
                    getInterface().finish();
                    return true;
                }
            });
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        }

        public ImageViewActivity getInterface() {
            return (ImageViewActivity) getActivity();
        }
    }

}
