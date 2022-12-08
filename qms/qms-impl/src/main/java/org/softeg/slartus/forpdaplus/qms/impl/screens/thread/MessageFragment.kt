package org.softeg.slartus.forpdaplus.qms.impl.screens.thread

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdacommon.getListener
import org.softeg.slartus.forpdacommon.showKeyboard
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.BaseMessageFragment
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.BbCodesFragment
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.BbCodesListener
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.TextInfo
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.emotics.EmoticsFragment
import org.softeg.slartus.forpdaplus.common.impl.screens.fragments.emotics.EmoticsListener
import org.softeg.slartus.forpdaplus.core.interfaces.IOnBackPressed
import org.softeg.slartus.forpdaplus.qms.impl.databinding.FragmentQmsMessageBinding
import org.softeg.slartus.forpdaplus.qms.impl.databinding.LayoutQmsMessageContentBinding
import org.softeg.slartus.forpdaplus.qms.impl.databinding.LayoutQmsMessageTabIconBinding
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageAction
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageEvent
import org.softeg.slartus.forpdaplus.qms.impl.screens.thread.models.MessageState

@AndroidEntryPoint
class MessageFragment :
    BaseMessageFragment<FragmentQmsMessageBinding, LayoutQmsMessageContentBinding>(
        FragmentQmsMessageBinding::inflate,
        LayoutQmsMessageContentBinding::inflate
    ), IOnBackPressed, EmoticsListener, BbCodesListener {

    private var tabLayoutMediator: TabLayoutMediator? = null
    private var onMessageListener: MessageListener? = null
    private val viewModel: MessageViewModel by viewModels()

    fun clear() {
        viewModel.obtainEvent(MessageEvent.ClearRequest)
    }

    fun closePopup() {
        super.hidePopupWindow()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
        with(binding) {
            messageEditText.apply {
                doOnTextChanged { text, start: Int,
                                  before: Int,
                                  count: Int ->
                    viewModel.obtainEvent(
                        MessageEvent.MessageTextChanged(text?.toString().orEmpty())
                    )
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) hidePopupWindow()
                }
                setOnClickListener {
                    hidePopupWindow()
                }
            }

            advancedButton.setOnClickListener {
                togglePopupWindowVisibility()
            }
            sendButton.setOnClickListener {
                viewModel.obtainEvent(MessageEvent.SendClicked)
            }
        }
        configurePager()
        popupBinding.backspaceButton.setOnClickListener {
            binding.messageEditText.dispatchKeyEvent(
                KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
            )
        }
    }

    private fun configurePager() {
        val pagerAdapter = PagerAdapter(this)

        popupBinding.pager.adapter = pagerAdapter
        tabLayoutMediator =
            TabLayoutMediator(popupBinding.tabLayout, popupBinding.pager) { tab, position ->
                val tabView = LayoutQmsMessageTabIconBinding.inflate(layoutInflater, null, false)
                tabView.icon.setImageResource(pagerAdapter.getTabIcon(position))
                tab.customView = tabView.root

            }.apply {
                attach()
            }
    }

    private fun subscribeToViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.viewStates.collect { state ->
                        onState(state)
                    }
                }

                launch {
                    viewModel.viewActions.collect { action ->
                        onAction(action)
                    }
                }
            }
        }
    }

    private fun onState(state: MessageState) {
        with(binding) {
            messageEditText.apply {
                setTextKeepState(state.message)
            }
            sendButton.visibility = if (state.sendButtonVisible) View.VISIBLE else View.GONE
        }
    }

    private fun onAction(action: MessageAction?) {
        if (action != null)
            viewModel.obtainEvent(MessageEvent.ActionInvoked)
        when (action) {
            is MessageAction.InsertText -> insertText(action.text)
            is MessageAction.SendMessage -> onMessageListener?.onSendClick(action.text)
            null -> {
                // ignore
            }
        }
    }

    private fun insertText(text: String) {
        binding.messageEditText.apply {
            val selectionStart = selectionStart
            val selectionEnd = selectionEnd
            getText().replace(selectionStart, selectionEnd, text)
            setSelection(selectionStart + text.length)
            lifecycleScope.launch {
                delay(300)
                showKeyboard()
            }
        }
    }

    override fun getPopupFiller(): View = binding.bottomView
    override fun onBackPressed(): Boolean {
        if (isPopupWindowVisible()) {
            hidePopupWindow()
            return true
        }
        return false
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        popupBinding.pager.adapter = null
        super.onDestroyView()
    }

    override fun onEmoticSelected(text: String) {
        viewModel.obtainEvent(MessageEvent.EmoticSelected(text))
    }

    override fun onBbCodeSelected(text: String) {
        viewModel.obtainEvent(MessageEvent.BbCodeSelected(text))
    }

    override fun getTextInfo(): TextInfo {
        return TextInfo(
            text = binding.messageEditText.text.toString(),
            binding.messageEditText.selectionStart,
            binding.messageEditText.selectionEnd
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onMessageListener = getListener<MessageListener>(context)
    }

    override fun onDetach() {
        onMessageListener = null
        super.onDetach()
    }
}

interface MessageListener {
    fun onSendClick(text: String)
}

private class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val tabs = listOf(TabContent.Emotics, TabContent.BbCodes)

    fun getTabIcon(position: Int): Int = tabs[position].icon

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        val fragment = when (tabs[position]) {
            TabContent.Emotics -> EmoticsFragment()
            TabContent.BbCodes -> BbCodesFragment()
        }
        return fragment
    }

    private enum class TabContent(@DrawableRes val icon: Int) {
        Emotics(org.softeg.slartus.forpdaplus.core_res.R.drawable.ic_outline_emoji_emotions),
        BbCodes(org.softeg.slartus.forpdaplus.core_res.R.drawable.ic_baseline_code)
    }
}