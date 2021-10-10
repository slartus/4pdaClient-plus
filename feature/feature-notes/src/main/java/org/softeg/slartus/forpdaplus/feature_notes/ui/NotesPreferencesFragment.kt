package org.softeg.slartus.forpdaplus.feature_notes.ui

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
import kotlinx.coroutines.*
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.forpdacommon.dialogs.ProgressDialog
import org.softeg.slartus.forpdacommon.openUrl
import org.softeg.slartus.forpdaplus.feature_notes.NotesManager
import org.softeg.slartus.forpdaplus.feature_notes.R
import org.softeg.slartus.forpdaplus.feature_notes.data.NotesRepository
import org.softeg.slartus.forpdaplus.feature_notes.di.NotesPreferences
import timber.log.Timber
import javax.inject.Inject

@Suppress("unused")
@AndroidEntryPoint
class NotesPreferencesFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var notesManager: NotesManager

    @Inject
    lateinit var notesPreferences: NotesPreferences

    @Inject
    lateinit var notesRepository: NotesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>("notes.remote.url")?.let { p ->
            p.summary = notesPreferences.remoteUrl
            p.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                showNotesRemoteServerDialog()
                true
            }
        }
        findPreference<Preference>("notes.remote.help")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                openUrl(requireContext(), "https://github.com/slartus/4pdaClient-plus/wiki/Notes")
                true
            }
        refreshNotesEnabled()
    }

    private fun refreshNotesEnabled() {
//            findPreference("notes.remote.settings").isEnabled = !Preferences.Notes.isLocal()
//            findPreference("notes.backup.category").isEnabled = Preferences.Notes.isLocal()

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notes_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "notes.backup" -> {
                notesManager.backupNotes(requireContext())
            }
            "notes.restore" -> {
                notesManager.restoreNotes(requireContext())
            }
            "notes.remote.url" -> {
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
                try {
                    val baseUrl = editText?.text.toString()
                    setLoading(true)
                    val errorHandler = CoroutineExceptionHandler { _, throwable ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            setLoading(false)
                            Timber.e(
                                NotReportException(
                                    throwable.localizedMessage ?: throwable.message,
                                    throwable
                                )
                            )
                        }
                    }
                    lifecycleScope.launch(Dispatchers.IO + errorHandler) {
                        notesRepository.checkUrl(baseUrl)
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            notesPreferences.setPlacement("remote")
                            notesPreferences.remoteUrl = baseUrl
                            findPreference<Preference>("notes.remote.url")?.summary = baseUrl
                            refreshNotesEnabled()
                        }
                    }
                } catch (ex: Throwable) {
                    Timber.e(ex)
                }
            }
            .show()
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

}