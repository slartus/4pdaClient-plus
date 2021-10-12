package org.softeg.slartus.forpdaplus.feature_notes.ui.list

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.softeg.slartus.forpdacommon.toForumDate
import org.softeg.slartus.forpdaplus.feature_notes.R

class NotesListAdapter(
    private val onClick: (NoteListItem) -> Unit
) :
    ListAdapter<NoteListItem, NotesListViewHolder>(NoteDiffCallback) {

    var lastLongClickItem: NoteListItem? = null
        private set

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).inProgress) TYPE_NORMAL else TYPE_PROGRESS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_note, parent, false)
        val holder = NotesListViewHolder(view, onClick)
        holder.itemView.setOnLongClickListener {
            lastLongClickItem = getItem(holder.adapterPosition)
            false
        }
        return holder
    }

    override fun onBindViewHolder(holder: NotesListViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        private const val TYPE_NORMAL = 0
        private const val TYPE_PROGRESS = 1
    }
}

object NoteDiffCallback : DiffUtil.ItemCallback<NoteListItem>() {
    override fun areItemsTheSame(oldItem: NoteListItem, newItem: NoteListItem): Boolean {
        return oldItem.note.id == newItem.note.id
    }

    override fun areContentsTheSame(oldItem: NoteListItem, newItem: NoteListItem): Boolean {
        return oldItem == newItem
    }
}

class NotesListViewHolder(
    itemView: View,
    private val onClick: (NoteListItem) -> Unit
) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {

    private var currentItem: NoteListItem? = null

    private val titleTextView: TextView = itemView.findViewById(R.id.title_textView)
    private val bodyTextView: TextView = itemView.findViewById(R.id.body_textView)
    private val userTextView: TextView = itemView.findViewById(R.id.user_nick_textView)
    private val dateTextView: TextView = itemView.findViewById(R.id.date_textView)
    private val progressView: View = itemView.findViewById(R.id.progress_view)

    init {
        itemView.setOnClickListener {
            currentItem?.let {
                onClick(it)
            }
        }
        itemView.setOnCreateContextMenuListener(this)
    }

    fun bind(item: NoteListItem) {
        currentItem = item

        titleTextView.text = item.note.title ?: ""
        bodyTextView.text = item.note.body ?: ""
        userTextView.text = item.note.userName ?: ""
        dateTextView.text = item.note.date.toForumDate()
        progressView.visibility = if (item.inProgress) View.VISIBLE else View.GONE
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        p2: ContextMenu.ContextMenuInfo?
    ) {

    }
}
