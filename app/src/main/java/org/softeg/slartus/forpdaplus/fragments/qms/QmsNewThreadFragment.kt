package org.softeg.slartus.forpdaplus.fragments.qms

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.BaseBrickContainerFragment
import org.softeg.slartus.forpdaplus.tabs.TabsManager
import ru.slartus.feature_qms_new_thread.QmsNewThreadFragment as FeatureFragment

class QmsNewThreadFragment : BaseBrickContainerFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitleByNick(null)
        childFragmentManager.setFragmentResultListener(
            FeatureFragment.ARG_CONTACT_NICK,
            this
        ) { _, bundle ->
            val nick = bundle.getString(FeatureFragment.ARG_CONTACT_NICK)
            setTitleByNick(nick)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setArrow()
        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.setHomeButtonEnabled(true)
    }

    private fun setTitleByNick(contactNick: String?) {
        val title = if (contactNick == null) {
            getString(R.string.qms_title_new_thread, "QMS")
        } else {
            getString(R.string.qms_title_new_thread, contactNick)
        }
        setTitle(title)
        TabsManager.instance.getTabByTag(tag)?.title = title
        mainActivity.notifyTabAdapter()
    }

    override fun closeTab(): Boolean {
        return false
    }

    override fun getFragmentInstance(): Fragment {
        val args = arguments
        return FeatureFragment().apply {
            this.arguments = args
        }
    }

    override fun onResume() {
        super.onResume()
        setArrow()
        //        if (mPopupPanelView != null)
//            mPopupPanelView.resume();
    }

    companion object {

        //    @Override
        //    public void hidePopupWindows() {
        //        super.hidePopupWindows();
        //        mPopupPanelView.hidePopupWindow();
        //    }
        fun showUserNewThread(userId: String?, userNick: String?) {
            val args = bundleOf(FeatureFragment.ARG_CONTACT_ID to userId)
            val fragment = QmsNewThreadFragment().apply {
                arguments = args
            }
            MainActivity.addTab(userNick, fragment)
        }

//    private val mPopupPanelView: PopupPanelView? = null
//    override fun onPause() {
//        super.onPause()
//        mPopupPanelView?.pause()
//    }
        //
        //
        //    @Nullable
        //    @Override
        //    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //        if (mPopupPanelView == null)
        //            mPopupPanelView = new PopupPanelView(PopupPanelView.VIEW_FLAG_EMOTICS | PopupPanelView.VIEW_FLAG_BBCODES);
        //        mPopupPanelView.createView(LayoutInflater.from(getContext()), (ImageButton) findViewById(R.id.advanced_button), message);
        //        mPopupPanelView.activityCreated(getMainActivity(), view);

        //        return view;
        //    }
        //
        //    @Override
        //    public void onDestroy() {
        //        if (mPopupPanelView != null) {
        //            mPopupPanelView.destroy();
        //            mPopupPanelView = null;
        //        }
        //        super.onDestroy();
        //    }
        //
        //    @Override
        //    public boolean onOptionsItemSelected(MenuItem item) {
        //        if (item.getItemId() == android.R.id.home) {
        //            onBackPressed();
        //            return true;
        //        }
        //
        //        return true;
        //    }
        //

    }
}