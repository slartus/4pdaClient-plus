package org.softeg.slartus.forpdaplus.fragments.qms

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.QmsContactsFragment
import org.softeg.slartus.forpdaplus.fragments.BaseBrickContainerFragment

class QmsContactsList : BaseBrickContainerFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setArrow()
    }

    override fun onResume() {
        super.onResume()
        setArrow()
        //        if (Preferences.Notifications.Qms.isReadDone())
//            reloadData();
    } //

    override fun getFragmentInstance(): Fragment {
        val args = arguments
        return QmsContactsFragment().apply {
            this.arguments = args
        }
    }
    //
    //    @Override
    //    protected ListData loadData(int loaderId, Bundle args) {
// TODO:
    //        Client.getInstance().setQmsCount(QmsUsers.unreadMessageUsersCount(users));
    //    }

    //
    //    @Override
    //    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
    //        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    //        if (info.id == -1) return;
    //        Object o = getAdapter().getItem((int) info.id);
    //        if (o == null)
    //            return;
    //        final QmsUser qmsUser = (QmsUser) o;
    //        if (TextUtils.isEmpty(qmsUser.getId())) return;
    //
    //        final List<MenuListDialog> list = new ArrayList<>();
    //        list.add(new MenuListDialog(getString(R.string.delete), () -> {
    //            Handler handler = new Handler();
    //            new Thread(() -> {
    //                try {
    //                    Map<String, String> additionalHeaders = new HashMap<>();
    //                    additionalHeaders.put("act", "qms-xhr");
    //                    additionalHeaders.put("action", "del-member");
    //                    additionalHeaders.put("del-mid", qmsUser.getId());
    //                    Client.getInstance().performPost("https://" + App.Host + "/forum/index.php", additionalHeaders);
    //
    //                    handler.post(this::reloadData);
    //
    //                } catch (IOException e) {
    //                    e.printStackTrace();
    //                }
    //            }).start();
    //        }));
    //// TODO:
    //        ExtUrl.showContextDialog(getContext(), null, list);
    //    }

}