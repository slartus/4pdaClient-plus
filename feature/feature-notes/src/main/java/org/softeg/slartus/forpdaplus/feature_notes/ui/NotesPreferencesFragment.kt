package org.softeg.slartus.forpdaplus.feature_notes.ui

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.dialogs.ProgressDialog
import org.softeg.slartus.forpdacommon.openUrl
import org.softeg.slartus.forpdacommon.uiMessage
import org.softeg.slartus.forpdaplus.core_ui.ui.fragments.BasePreferenceFragment
import org.softeg.slartus.forpdaplus.feature_notes.NotesBackupManager
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences
import timber.log.Timber
import javax.inject.Inject

@Suppress("unused")
@AndroidEntryPoint
class NotesPreferencesFragment : BasePreferenceFragment() {
    @Inject
    lateinit var notesManager: NotesBackupManager

    @Inject
    lateinit var notesPreferences: NotesPreferences

    @Inject
    lateinit var notesRepository: NotesRepository

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener { _, s ->
            when (s) {
                KEY_REMOTE_URL -> updateRemoteUrlSummary()
                KEY_NOTES_PLACEMENT -> lifecycleScope.launch(Dispatchers.IO + errorHandler) { notesRepository.load() }
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
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notes_preferences, rootKey)
    }

    private fun updateRemoteUrlSummary() {
        findPreference<Preference>(KEY_REMOTE_URL)?.let { p ->
            p.summary = notesPreferences.remoteUrl
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            KEY_BACKUP -> {
                notesManager.backupNotes(requireContext())
            }
            KEY_RESTORE -> {
                notesManager.restoreNotes(requireContext())
            }
            KEY_REMOTE_URL -> {
                //showNotesRemoteServerDialog()
            }
            KEY_HELP -> {
                openUrl(requireContext(), "https://github.com/slartus/4pdaClient-plus/wiki/Notes")
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun checkRemoteUrl(baseUrl: String) {
        setLoading(true)


        lifecycleScope.launch(Dispatchers.IO + errorHandler) {
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

    companion object {
        private const val KEY_REMOTE_URL = "notes.remote.url"
        private const val KEY_RESTORE = "notes.restore"
        private const val KEY_BACKUP = "notes.backup"
        private const val KEY_HELP = "notes.remote.help"
        private const val KEY_NOTES_PLACEMENT = "notes.placement"
        private const val KEY_REMOTE_CATEGORY = "notes.remote.settings"
        private const val KEY_LOCAL_CATEGORY = "notes.backup.category"
    }

}