package org.softeg.slartus.forpdaplus.core_ui.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import javax.inject.Inject

@AndroidEntryPoint
class ChooseFileDialogFragment : BaseDialogFragment() {
    @Inject
    lateinit var router: AppRouter

    private val chooseFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            dismiss()
            if (result.resultCode == Activity.RESULT_OK) {
                val fileUri = result.data?.data ?: return@registerForActivityResult
                router.sendResult(resultKey, fileUri)
            }
        }

    lateinit var resultKey: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultKey = requireArguments().getString(ARG_RESULT_KEY)!!
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val chooseFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                val mimeTypes = arrayOf("application/octet-stream")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            chooseFileLauncher.launch(chooseFileIntent)
        }
        return super.onCreateDialog(savedInstanceState)
    }

    companion object {
        private const val ARG_RESULT_KEY = "ChooseFileDialogFragment.resultKey"
        fun newInstance(resultKey: String) = ChooseFileDialogFragment().apply {
            arguments = bundleOf(ARG_RESULT_KEY to resultKey)
        }
    }
}