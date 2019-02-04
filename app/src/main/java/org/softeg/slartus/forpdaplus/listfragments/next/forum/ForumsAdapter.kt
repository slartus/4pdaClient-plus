package org.softeg.slartus.forpdaplus.listfragments.next.forum

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import org.softeg.slartus.forpdaapi.Forum
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.prefs.Preferences

/*
 * Created by slinkin on 04.02.2019.
 */
internal class ForumsAdapter// Provide a suitable constructor (depends on the kind of dataset)
internal constructor(private val mHeaderset: List<Forum>, private val mDataset: List<Forum>,
                     private val mOnClickListener: OnClickListener, private val mOnLongClickListener: OnLongClickListener) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER_VIEW_TYPE = 0
        private const val HEADER_CURRENT_VIEW_TYPE = 1
        private const val HEADER_CURRENT_NOTOPICS_VIEW_TYPE = 2
        private const val DATA_VIEW_TYPE = 3
    }

    private val mIsShowImages: Boolean? = Preferences.Forums.isShowImages()
    interface OnClickListener {
        fun onItemClick(v: View)

        fun onHeaderClick(v: View)

        fun onHeaderTopicsClick(v: View)

    }

    interface OnLongClickListener {
        fun onItemClick(v: View)

        fun onHeaderClick(v: View)

        fun onHeaderTopicsClick(v: View)

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        internal var mText1: TextView = v.findViewById(android.R.id.text1)
        internal var mText2: TextView = v.findViewById(android.R.id.text2)
        internal var mImageView: ImageView = v.findViewById(R.id.imageView3)

    }

    internal class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var mText: TextView = v.findViewById(R.id.textView3)
    }


    private fun getItem(position: Int): Forum? {
        when (getItemViewType(position)) {
            HEADER_CURRENT_NOTOPICS_VIEW_TYPE, HEADER_CURRENT_VIEW_TYPE, HEADER_VIEW_TYPE -> return mHeaderset[position]
            DATA_VIEW_TYPE -> return mDataset[position - mHeaderset.size]
        }
        return null
    }

    override fun getItemViewType(position: Int): Int {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return if (position < mHeaderset.size) {
            if (position == mHeaderset.size - 1) {
                if (!mHeaderset[position].isHasTopics) HEADER_CURRENT_NOTOPICS_VIEW_TYPE else HEADER_CURRENT_VIEW_TYPE
            } else HEADER_VIEW_TYPE
        } else DATA_VIEW_TYPE
    }


    internal fun notifyDataSetChangedWithLayout() {
        // mIsShowImages = Preferences.Forums.isShowImages();
        notifyDataSetChanged()
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            DATA_VIEW_TYPE -> {
                val v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.forum_item, parent, false)

                val viewHolder = ViewHolder(v)
                if (mIsShowImages != true)
                    viewHolder.mImageView.visibility = View.GONE
                v.setOnClickListener { v1 -> mOnClickListener.onItemClick(v1) }
                v.setOnLongClickListener { v12 ->
                    mOnLongClickListener.onItemClick(v12)
                    true
                }
                viewHolder
            }
            HEADER_VIEW_TYPE -> {
                val headerV = LayoutInflater.from(parent.context)
                        .inflate(R.layout.forum_header_item, parent, false)


                val headerViewHolder = HeaderViewHolder(headerV)
                headerV.setOnClickListener { v13 -> mOnClickListener.onHeaderClick(v13) }
                headerV.setOnLongClickListener { v14 ->
                    mOnLongClickListener.onHeaderClick(v14)
                    true
                }
                headerViewHolder
            }
            HEADER_CURRENT_VIEW_TYPE -> {
                val headerCV = LayoutInflater.from(parent.context)
                        .inflate(R.layout.forum_header_current_item, parent, false)


                val headerCViewHolder = HeaderViewHolder(headerCV)
                headerCV.setOnClickListener { v15 -> mOnClickListener.onHeaderTopicsClick(v15) }
                headerCV.setOnLongClickListener { v16 ->
                    mOnLongClickListener.onHeaderTopicsClick(v16)
                    true
                }

                headerCViewHolder
            }
            HEADER_CURRENT_NOTOPICS_VIEW_TYPE -> {
                val headerCNV = LayoutInflater.from(parent.context)
                        .inflate(R.layout.forum_header_notopics_item, parent, false)


                val headerCNViewHolder = HeaderViewHolder(headerCNV)
                headerCNV.setOnClickListener { v17 -> mOnClickListener.onHeaderClick(v17) }
                headerCNV.setOnLongClickListener { v18 ->
                    mOnLongClickListener.onHeaderClick(v18)
                    false
                }

                headerCNViewHolder
            }
            else -> throw IllegalArgumentException()
        }
//                viewHolders[viewType] = item
//            }
//            return viewHolders[viewType]
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        val forum = getItem(position)!!
        when (viewType) {
            DATA_VIEW_TYPE -> {
                val viewHolder = holder as ViewHolder

                viewHolder.mText1.text = forum.title
                viewHolder.mText2.text = forum.description

                if (forum.iconUrl != null && mIsShowImages==true) {
                    ImageLoader.getInstance().displayImage(forum.iconUrl,
                            holder.mImageView,
                            object : ImageLoadingListener {

                                override fun onLoadingStarted(p1: String, p2: View) {
                                    p2.visibility = View.INVISIBLE
                                    //holder.mProgressBar.setVisibility(View.VISIBLE);
                                }

                                override fun onLoadingFailed(p1: String, p2: View, p3: FailReason) {
                                    // holder.mProgressBar.setVisibility(View.INVISIBLE);
                                }

                                override fun onLoadingComplete(p1: String, p2: View, p3: Bitmap) {
                                    p2.visibility = View.VISIBLE
                                    // holder.mProgressBar.setVisibility(View.INVISIBLE);
                                }

                                override fun onLoadingCancelled(p1: String, p2: View) {

                                }
                            })
                }
            }
            HEADER_VIEW_TYPE -> {
                val headerViewHolder = holder as HeaderViewHolder
                headerViewHolder.mText.text = forum.title
            }
            HEADER_CURRENT_VIEW_TYPE -> {
                val headerCViewHolder = holder as HeaderViewHolder
                headerCViewHolder.mText.text = forum.title
            }
            HEADER_CURRENT_NOTOPICS_VIEW_TYPE -> {
                val headerCNViewHolder = holder as HeaderViewHolder
                headerCNViewHolder.mText.text = forum.title
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mHeaderset.size + mDataset.size
    }
}