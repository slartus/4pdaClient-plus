package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.softeg.slartus.forpdaplus.feature_preferences.R

@Suppress("unused")
class NotesPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.notes_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {

        }
        return super.onPreferenceTreeClick(preference)
    }
//
//
//    private fun restoreNotes() {
//        OpenFileDialog(activity)
//            .setFilter(".*\\.(?i:sqlite)")
//            .setOpenDialogListener { fileName: String? ->
//                try {
//                    val sourceuri = Uri.parse(fileName)
//                    if (sourceuri == null) {
//                        Toast.makeText(activity, "Файл не выбран!", Toast.LENGTH_SHORT).show()
//                        return@setOpenDialogListener
//                    }
//                    val notes = NotesTable.getNotesFromFile(fileName)
//                    AlertDialog.Builder(requireContext())
//                        .setTitle("Внимание!")
//                        .setMessage(
//                            """
//    Заметок для восстановления: ${notes.size}
//
//    Восстановление заметок приведёт к полной потере всех существующих заметок!
//    """.trimIndent()
//                        )
//                        .setPositiveButton("Продолжить") { dialogInterface: DialogInterface, _: Int ->
//                            dialogInterface.dismiss()
//                            try {
//                                val count = NotesTable.restoreFrom(notes)
//                                AlertDialog.Builder(requireContext())
//                                    .setTitle("Успех!")
//                                    .setMessage("Резервная копия заметок восстановлена!\nЗаметок восстановлено: $count")
//                                    .setPositiveButton("ОК", null)
//                                    .create().show()
//                            } catch (ex: Throwable) {
//                                AppLog.e(activity, ex)
//                            }
//                        }
//                        .setNegativeButton("Отмена", null)
//                        .create().show()
//                } catch (ex: Throwable) {
//                    AppLog.e(activity, ex)
//                }
//            }
//            .show()
//    }
}