package org.softeg.slartus.forpdaplus.feature_notes.ui.note

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.databinding.ViewNoteRowBinding

class RowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewNoteRowBinding =
        ViewNoteRowBinding.inflate(LayoutInflater.from(context), this)

    var title: CharSequence?
        get() = binding.titleTextView.text
        set(value) {
            binding.titleTextView.text = value
        }

    var value: CharSequence?
        get() = binding.valueTextView.text
        set(value) {
            binding.valueTextView.text = value
        }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        orientation = VERTICAL

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RowView,
            0, 0
        ).apply {

            try {
                title = getString(R.styleable.RowView_title)
                value = getString(R.styleable.RowView_value)
            } finally {
                recycle()
            }
        }
    }

    fun setOnValueClickListener(listener: View.OnClickListener) {
        binding.valueTextView.setOnClickListener(listener)
    }
}