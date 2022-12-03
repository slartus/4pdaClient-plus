package org.softeg.slartus.forpdaplus.common.impl.screens.fragments

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding
import org.softeg.slartus.forpdaplus.common.impl.R
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.BaseFragment
import org.softeg.slartus.forpdaplus.core_lib.ui.fragments.Inflate

abstract class BaseMessageFragment<VB : ViewBinding, PV : ViewBinding>(
    inflate: Inflate<VB>,
    private val popupViewInflate: Inflate<PV>
) :
    BaseFragment<VB>(inflate) {

    private var popupWindow: PopupWindow? = null
    private var keyboardHeight: Float = 0f
    private var windowContentView: View? = null
    private var isKeyBoardVisible: Boolean = false
    private val onGlobalLayoutListener = OnGlobalLayoutListener { onGlobalLayout() }

    /**
     * Checking keyboard height and keyboard visibility
     */
    private var previousHeightDifference = 0
    private var k = -1

    private var _popupBinding: PV? = null
    protected val popupBinding get() = _popupBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _popupBinding = popupViewInflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _popupBinding = null
        popupWindow?.dismiss()
        popupWindow = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        windowContentView = requireActivity().window.decorView.findViewById(android.R.id.content)
        keyboardHeight = resources.getDimension(R.dimen.keyboard_height)

        addGlobalLayoutListener()
    }

    abstract fun getPopupFiller(): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enablePopUpView()
    }

    fun isPopupWindowVisible(): Boolean = popupWindow?.isShowing == true

    fun togglePopupWindowVisibility() {
        if (!isPopupWindowVisible()) {
            showPopupWindow()
        } else {
            hidePopupWindow()
        }
    }

    private fun showPopupWindow() {
        val windowContentView = windowContentView ?: return
        val popupWindow = popupWindow ?: return
        if (!popupWindow.isShowing) {
            popupWindow.height = keyboardHeight.toInt()
            getPopupFiller().visibility = if (isKeyBoardVisible) View.GONE else View.VISIBLE
            popupWindow.showAtLocation(windowContentView, Gravity.BOTTOM, 0, 0)
        }
    }

    protected fun hidePopupWindow() {
        val popupWindow = popupWindow ?: return
        if (popupWindow.isShowing)
            popupWindow.dismiss()
    }

    private fun changeKeyboardHeight(height: Float) {
        if (height > 100) {
            keyboardHeight = height
            setPopupFillerHeight()
        }
    }

    private fun setPopupFillerHeight() {
        getPopupFiller().layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight.toInt()
        )
    }

    private fun onGlobalLayout() {
        val windowContentView = windowContentView ?: return
        val visibleRect = Rect()
        windowContentView.getWindowVisibleDisplayFrame(visibleRect)
        val screenHeight = windowContentView.rootView.height
        if (k == -1) k = screenHeight - visibleRect.bottom
        val heightDifference = screenHeight - visibleRect.bottom - k
        if (previousHeightDifference - heightDifference > 50) {
            hidePopupWindow()
        }
        isKeyBoardVisible = heightDifference > 100
        if (previousHeightDifference != heightDifference) changeKeyboardHeight(heightDifference.toFloat())
        previousHeightDifference = heightDifference
    }

    private fun addGlobalLayoutListener() {
        removeGlobalLayoutListener()
        val windowContentView = windowContentView ?: return
        windowContentView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private fun removeGlobalLayoutListener() {
        val windowContentView = windowContentView ?: return
        windowContentView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    /**
     * Defining all components of emoticons keyboard
     */
    private fun enablePopUpView() {
        // Creating a pop window for emoticons keyboard
        popupWindow = PopupWindow(
            popupBinding.root, ViewGroup.LayoutParams.MATCH_PARENT,
            keyboardHeight.toInt(), false
        ).apply {
            setOnDismissListener {
                getPopupFiller().visibility = LinearLayout.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            removeGlobalLayoutListener()
            hidePopupWindow()
//            for (item in mQuickPostPagerAdapter.mItems) if (item.getBaseQuickView() != null) item.getBaseQuickView()
//                .onPause()
        } catch (ex: Throwable) {
            Log.e("PopupPanelView", ex.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            addGlobalLayoutListener()
//            for (item in mQuickPostPagerAdapter.mItems) if (item.getBaseQuickView() != null) item.getBaseQuickView()
//                .onResume()
        } catch (ex: Throwable) {
            Log.e("PopupPanelView", ex.toString())
        }
    }
}