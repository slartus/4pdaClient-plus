package org.softeg.slartus.forpdaplus.feature_notes.ui.note

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.softeg.slartus.forpdaplus.feature_notes.databinding.ViewNoteRowBinding
import android.R

import android.content.res.TypedArray




class RowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: ViewNoteRowBinding =
        ViewNoteRowBinding.inflate(LayoutInflater.from(context), this)

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        orientation = VERTICAL

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PieChart,
            0, 0).apply {

            try {
                mShowText = getBoolean(R.styleable.PieChart_showText, false)
                textPos = getInteger(R.styleable.PieChart_labelPosition, 0)
            } finally {
                recycle()
            }
        }
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomBarItemCustomView)
    }
}