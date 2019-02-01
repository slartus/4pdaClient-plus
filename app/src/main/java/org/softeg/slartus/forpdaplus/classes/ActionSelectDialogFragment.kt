package org.softeg.slartus.forpdaplus.classes

import android.content.Context
import android.text.TextUtils

import com.afollestad.materialdialogs.MaterialDialog

import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R

import java.util.Arrays

/**
 * Created by radiationx on 16.02.16.
 */
object ActionSelectDialogFragment {


    interface OkListener {
        fun execute(value: CharSequence?)
    }

    fun showSaveNavigateActionDialog(context: Context,
                                     preferenceKey: String,
                                     selectedAction: String,
                                     showTopicAction: Runnable) {
        val navigateAction = App.getInstance().preferences
                .getString(preferenceKey, null)
        if (navigateAction == null || selectedAction != navigateAction) {
            showSelectDialog(context, preferenceKey, selectedAction, showTopicAction)
        } else {
            showTopicAction.run()
        }

    }

    private fun showSelectDialog(context: Context,
                                 preferenceKey: String,
                                 selectedAction: String,
                                 showTopicAction: Runnable) {
        MaterialDialog.Builder(context)
                .content(R.string.assign_default)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .neutralText(R.string.ask)
                .onPositive { _, _ ->
                    App.getInstance().preferences
                            .edit().putString(preferenceKey, selectedAction).apply()

                    showTopicAction.run()
                }
                .onNegative { _, _ -> showTopicAction.run() }
                .onNeutral { _, _ ->
                    App.getInstance().preferences
                            .edit().putString(preferenceKey, null).apply()

                    showTopicAction.run()
                }
                .show()
    }

    fun execute(context: Context,
                title: String,
                preferenceKey: String,
                titles: Array<CharSequence>,
                values: Array<CharSequence>,
                okListener: OkListener,
                hintForChangeDefault: String) {
        val value = App.getInstance().preferences.getString(preferenceKey, null)

        if (Arrays.asList(*values).contains(value)) {
            okListener.execute(value)
            return
        }


        val selectedValue = intArrayOf(0)
        MaterialDialog.Builder(context)
                .title(title)
                .items(*titles)
                .itemsCallbackSingleChoice(0) { _, _, which, _ ->
                    selectedValue[0] = which
                    true
                }
                .alwaysCallSingleChoiceCallback()
                .positiveText(R.string.always)
                .neutralText(R.string.only_now)
                .onPositive { _, _ ->
                    val newValue = values[selectedValue[0]]
                    App.getInstance().preferences
                            .edit()
                            .putString(preferenceKey, newValue.toString())
                            .apply()

                    if (!TextUtils.isEmpty(hintForChangeDefault))
                        MaterialDialog.Builder(context)
                                .title(R.string.hint)
                                .content(hintForChangeDefault)
                                .cancelable(false)
                                .positiveText(R.string.ok)
                                .onPositive { _, _ -> okListener.execute(newValue) }
                                .show()
                    else
                        okListener.execute(newValue)
                }
                .onNeutral { _, _ -> okListener.execute(values[selectedValue[0]]) }
                .show()
    }
}
