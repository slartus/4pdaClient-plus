package org.softeg.slartus.forpdaplus.core_ui.ui.fragments

import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import org.softeg.slartus.forpdaplus.core_ui.utils.dip2px
import org.softeg.slartus.forpdaplus.core_ui.utils.getDisplaySize

abstract class BaseDialogFragment : DialogFragment() {

    protected open val widthPercentsOfScreen: Float? = 90f

    override fun onStart() {
        super.onStart()
        setDialogSize()
    }

    private fun setDialogSize() {
        val widthPercentsOfScreen = widthPercentsOfScreen ?: return

        dialog?.window?.let {
            val context = requireContext()
            val size = context.getDisplaySize()
            val width = (size.x * widthPercentsOfScreen / 100.0f)
                .coerceAtMost((480f.dip2px(context)).toFloat())

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(it.attributes)
            lp.width = width.toInt()

            it.attributes = lp
        }
    }
}