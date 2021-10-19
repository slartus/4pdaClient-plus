package org.softeg.slartus.forpdaplus.feature_notes.ui

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.dialogs.ProgressDialog
import org.softeg.slartus.forpdacommon.openUrl
import org.softeg.slartus.forpdacommon.uiMessage
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppScreen
import org.softeg.slartus.forpdaplus.core_ui.ui.fragments.BasePreferenceFragment
import org.softeg.slartus.forpdaplus.feature_notes.NotesBackupManager
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NotesPreferencesFragment : BasePreferenceFragment() {
    @Inject
    lateinit var notesManager: NotesBackupManager

    @Inject
    lateinit var notesPreferences: NotesPreferences

    @Inject
    lateinit var notesRepository: NotesRepository

    @Inject
    lateinit var router: AppRouter

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        lifecycleScope.launch(Dispatchers.Main) {
            setLoading(false)
            Timber.e(
                NotReportException(
                    throwable.uiMessage,
                    throwable
                )
            )
        }
    }

    // https://habr.com/ru/company/e-Legion/blog/545934/
    private val chooseFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileUri = result.data?.data ?: return@registerForActivityResult
                notesManager.restoreNotes(requireContext(), fileUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener { _, s ->
            when (s) {
                KEY_REMOTE_URL -> {
                    updateRemoteUrlSummary()
                    reloadRepository()
                }
                KEY_NOTES_PLACEMENT -> {
                    reloadRepository()
                    refreshRemoteEnabled()
                }
            }
        }
        findPreference<EditTextPreference>(KEY_REMOTE_URL)?.apply {
            setOnBindEditTextListener { editText ->
                editText.setText(notesPreferences.remoteUrl ?: "")
                editText.inputType = InputType.TYPE_TEXT_VARIATION_URI
            }
            setOnPreferenceChangeListener { _, newValue ->
                checkRemoteUrl(newValue?.toString() ?: "")
                false
            }
            this.dialogIcon = null
        }
        updateRemoteUrlSummary()
        refreshRemoteEnabled()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notes_preferences, rootKey)
    }

    private fun updateRemoteUrlSummary() {
        findPreference<Preference>(KEY_REMOTE_URL)?.let { p ->
            p.summary = notesPreferences.remoteUrl
        }
    }

    private fun refreshRemoteEnabled() {
        findPreference<Preference>(KEY_REMOTE_URL)?.isEnabled = !notesPreferences.isLocal
        findPreference<Preference>(KEY_HELP)?.isEnabled = !notesPreferences.isLocal

        // !TODO: backup/restore remote notes
        findPreference<Preference>(KEY_BACKUP)?.isEnabled = notesPreferences.isLocal
        findPreference<Preference>(KEY_RESTORE)?.isEnabled = notesPreferences.isLocal
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            KEY_BACKUP -> notesManager.backupNotes(requireContext())
            KEY_RESTORE -> restoreNotes()
            KEY_HELP -> openUrl(
                requireContext(),
                "https://github.com/slartus/4pdaClient-plus/wiki/Notes"
            )
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun checkRemoteUrl(baseUrl: String) {
        setLoading(true)

        lifecycleScope.launch(Dispatchers.Default + errorHandler) {
            val uri = Uri.parse(baseUrl)
            val url = if (uri.scheme == null) {
                "https://$uri"
            } else {
                uri.toString()
            }
            notesRepository.checkUrl(url)
            withContext(Dispatchers.Main) {
                setLoading(false)
                notesPreferences.setPlacement("remote")
                notesPreferences.remoteUrl = url
            }
        }
    }

    private fun setLoading(progress: Boolean) {
        if (!isAdded) return

        val progressTag = "PROGRESS_TAG"
        val fragment = childFragmentManager.findFragmentByTag(progressTag)
        if (fragment != null && !progress) {
            (fragment as ProgressDialog).dismissAllowingStateLoss()
            childFragmentManager.executePendingTransactions()
        } else if (fragment == null && progress) {
            ProgressDialog().show(childFragmentManager, progressTag)
            childFragmentManager.executePendingTransactions()
        }
    }

    private fun reloadRepository() {
        lifecycleScope.launch(Dispatchers.Default + errorHandler) {
            notesRepository.load()
        }
    }

    private fun restoreNotes() {
//        val resultKey = "NotesPreferencesFragment.chooseFileKey"
//        router.setResultListener(resultKey) { data ->
//            Timber.d(data.toString())
//        }
//        router.navigateTo(AppScreen.ChooseFileDialog(resultKey))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val chooseFileIntent = Intent(ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                val mimeTypes = arrayOf("application/octet-stream")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            chooseFileLauncher.launch(chooseFileIntent)
        } else {
            notesManager.restoreNotes(requireContext())
        }
    }

    companion object {
        private const val KEY_REMOTE_URL = "notes.remote.url"
        private const val KEY_RESTORE = "notes.restore"
        private const val KEY_BACKUP = "notes.backup"
        private const val KEY_HELP = "notes.remote.help"
        private const val KEY_NOTES_PLACEMENT = "notes.placement"
    }
}