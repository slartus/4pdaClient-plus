package org.softeg.slartus.forpdaplus.feature_notes.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.dialogs.ProgressDialog
import org.softeg.slartus.forpdacommon.openUrl
import org.softeg.slartus.forpdacommon.uiMessage
import org.softeg.slartus.forpdaplus.feature_notes.NotesBackupManager
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences
import timber.log.Timber
import javax.inject.Inject

@Suppress("unused")
@AndroidEntryPoint
class NotesPreferencesFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var notesManager: NotesBackupManager

    @Inject
    lateinit var notesPreferences: NotesPreferences

    @Inject
    lateinit var notesRepository: NotesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener { _, s ->
            when (s) {
                KEY_REMOTE_URL -> updateRemoteUrlSummary()
            }
        }
        findPreference<Preference>(KEY_REMOTE_URL)?.setOnPreferenceClickListener {
            showNotesRemoteServerDialog()
            true
        }
        findPreference<Preference>(KEY_HELP)?.setOnPreferenceClickListener {
            openUrl(requireContext(), "https://github.com/slartus/4pdaClient-plus/wiki/Notes")
            true
        }
        updateRemoteUrlSummary()
    }

    private fun updateRemoteUrlSummary() {
        findPreference<Preference>(KEY_REMOTE_URL)?.let { p ->
            p.summary = notesPreferences.remoteUrl
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notes_preferences, rootKey)
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
                showNotesRemoteServerDialog()
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun showNotesRemoteServerDialog() {
        val inflater =
            (activity?.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        val view = inflater.inflate(R.layout.input_notes_remote_url_layout, null as ViewGroup?)
        val editText = view.findViewById<EditText>(R.id.edit_text)
        editText.setText(notesPreferences.remoteUrl)
        MaterialDialog.Builder(requireContext())
            .title(R.string.notes_remote_url)
            .customView(view, true)
            .cancelable(true)
            .positiveText(R.string.ok)
            .negativeText(R.string.cancel)
            .onPositive { _: MaterialDialog?, _: DialogAction? ->
                val baseUrl = editText?.text.toString()

                checkRemoteUrl(baseUrl)
            }
            .show()
    }

    private fun checkRemoteUrl(baseUrl: String) {
        setLoading(true)

        val errorHandler = CoroutineExceptionHandler { _, throwable ->
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