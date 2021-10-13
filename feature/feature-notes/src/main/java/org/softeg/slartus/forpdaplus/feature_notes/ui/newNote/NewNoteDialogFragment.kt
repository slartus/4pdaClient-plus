package org.softeg.slartus.forpdaplus.feature_notes.ui.newNote

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdacommon.dip2px
import org.softeg.slartus.forpdacommon.getDisplaySize
import org.softeg.slartus.forpdaplus.core.di.GenericSavedStateViewModelFactory
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.databinding.FragmentNewNoteBinding
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NewNoteDialogFragment : DialogFragment() {

    @Inject
    internal lateinit var viewModelFactory: NewNoteViewModelFactory

    @Inject
    internal lateinit var router: AppRouter

    private val viewModel: NewNoteViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this, arguments)
    }

    private val widthPercentsOfScreen: Float
        get() = 90f

    override fun onStart() {
        super.onStart()

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = FragmentNewNoteBinding.inflate(inflater)
        val view = binding.root

        binding.titleEditText.addTextChangedListener {
            viewModel.setTitle(it.toString())
        }
        binding.clearTitleButton.setOnClickListener {
            viewModel.clearTitle()
        }

        binding.bodyEditText.addTextChangedListener {
            viewModel.setBody(it.toString())
        }
        binding.clearBodyButton.setOnClickListener {
            viewModel.clearBody()
        }
        binding.titleEditText.requestFocus()

        viewModel.state.observe(this, {
            binding.titleEditText.setText(it.title)
            binding.bodyEditText.setText(it.body)
        })

        viewModel.closeEvent.observe(this, {
            Toast.makeText(requireContext(), getString(R.string.NoteSaved), Toast.LENGTH_LONG)
                .show()
            dialog?.dismiss()
        })

        viewModel.error.observe(this, {
            Toast.makeText(
                requireContext(),
                it.localizedMessage ?: it.message ?: it.toString(),
                Toast.LENGTH_SHORT
            ).show()
            Timber.e(it)
        })

        return MaterialDialog.Builder(requireContext())
            .customView(view, false)
            .title(R.string.NewNote)
            .positiveText(R.string.Save)
            .negativeText(R.string.cancel)
            .autoDismiss(false)
            .onPositive { _, _ ->
                viewModel.saveNote()
            }
            .onNegative { dialog, _ ->
                dialog.dismiss()
            }
            .cancelable(true)
            .build()
    }

    companion object {
        fun newInstance(
            title: String? = null,
            body: String? = null,
            url: String? = null,
            topicId: String? = null,
            topicTitle: String? = null,
            postId: String? = null,
            userId: String? = null,
            userName: String? = null
        ) = NewNoteDialogFragment().apply {
            arguments = Bundle().apply {
                putString(NewNoteViewModel.TITLE_KEY, title)
                putString(NewNoteViewModel.BODY_KEY, body)
                putString(NewNoteViewModel.URL_KEY, url)
                putString(NewNoteViewModel.TOPIC_ID_KEY, topicId)
                putString(NewNoteViewModel.TOPIC_TITLE_KEY, topicTitle)
                putString(NewNoteViewModel.POST_ID_KEY, postId)
                putString(NewNoteViewModel.USER_ID_KEY, userId)
                putString(NewNoteViewModel.USER_NAME_KEY, userName)
            }
        }
    }

}