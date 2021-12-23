package org.softeg.slartus.forpdaplus.fragments.qms

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.fragments.BaseBrickContainerFragment
import org.softeg.slartus.forpdaplus.listtemplates.BrickInfo
import ru.slartus.feature_qms_contact_threads.QmsContactThreadsFragment

class QmsContactThemes : BaseBrickContainerFragment() {
    private var contactId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId =
            savedInstanceState?.getString(QmsContactThreadsFragment.ARG_CONTACT_ID)
                ?: arguments?.getString(QmsContactThreadsFragment.ARG_CONTACT_ID) ?: contactId

        childFragmentManager.setFragmentResultListener(
            QmsContactThreadsFragment.ARG_CONTACT_NICK,
            this
        ) { _, bundle ->
            setTitle(bundle.getString(QmsContactThreadsFragment.ARG_CONTACT_NICK))
        }
    }

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    override fun setBrickInfo(listTemplate: BrickInfo): Fragment {
        return super.setBrickInfo(listTemplate)
    }

    override fun getListName(): String {
        return "QmsContactThemes_$contactId"
    }

    override fun getFragmentInstance(): Fragment {
        val args = arguments
        return QmsContactThreadsFragment().apply {
            this.arguments = args
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(QmsContactThreadsFragment.ARG_CONTACT_ID, contactId)
    }

    companion object {

        @JvmStatic
        fun showThemes(userId: String, userNick: String?) {
            val bundle = bundleOf(
                QmsContactThreadsFragment.ARG_CONTACT_ID to userId,
                QmsContactThreadsFragment.ARG_CONTACT_NICK to userNick
            )
            MainActivity.addTab(userNick, userId, newInstance(bundle))
        }

        @JvmStatic
        fun newInstance(args: Bundle?): QmsContactThemes = QmsContactThemes().apply {
            arguments = args
        }
    }
}