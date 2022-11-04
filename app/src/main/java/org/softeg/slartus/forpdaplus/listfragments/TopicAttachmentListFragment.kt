package org.softeg.slartus.forpdaplus.listfragments

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.MainActivity
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.fragments.BaseBrickContainerFragment
import org.softeg.slartus.forpdaplus.listtemplates.TopicAttachmentBrickInfo
import org.softeg.slartus.forpdaplus.topic.impl.screens.attachments.TopicAttachmentsFragment


class TopicAttachmentListFragment : BaseBrickContainerFragment() {
    private var topicId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicId =
            savedInstanceState?.getString(TopicAttachmentsFragment.ARG_TOPIC_ID)
                ?: arguments?.getString(TopicAttachmentsFragment.ARG_TOPIC_ID) ?: topicId
        setBrickInfo(TopicAttachmentBrickInfo())
    }

    override fun onResume() {
        super.onResume()
        setArrow()
    }

    override fun getListName(): String {
        return "TopicAttachmentListFragment_$topicId"
    }

    override fun getFragmentInstance(): Fragment {
        val args = arguments
        return TopicAttachmentsFragment().apply {
            this.arguments = args
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TopicAttachmentsFragment.ARG_TOPIC_ID, topicId)
    }

    companion object {
        @JvmStatic
        fun showActivity(topicId: String) {
            val bundle = bundleOf(
                TopicAttachmentsFragment.ARG_TOPIC_ID to topicId
            )
            val fragment = newInstance(bundle)
            MainActivity.addTab(
                App.getContext().getString(R.string.attachments),
                fragment.listName,
                newInstance(bundle)
            )
        }

        @JvmStatic
        fun newInstance(args: Bundle?): TopicAttachmentListFragment =
            TopicAttachmentListFragment().apply {
                arguments = args
            }
    }
}