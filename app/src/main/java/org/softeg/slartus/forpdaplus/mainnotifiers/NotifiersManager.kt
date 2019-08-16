package org.softeg.slartus.forpdaplus.mainnotifiers

/*
 * Created by slinkin on 10.07.2014.
 */

import android.content.DialogInterface

import com.afollestad.materialdialogs.MaterialDialog

import java.util.ArrayList

class NotifiersManager : DialogInterface.OnDismissListener {

    private val dialogs = ArrayList<DialogInterface>()
    private val lock = Any()
    fun addNotifyDialog(alertDialog: MaterialDialog) {
        try {
            alertDialog.setOnDismissListener(this)
            synchronized(lock) {
                dialogs.add(alertDialog)
                if (dialogs.size == 1)
                    alertDialog.show()
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }

    }

    override fun onDismiss(dialogInterface: DialogInterface) {
        try {
            synchronized(lock) {
                if (dialogs.contains(dialogInterface))
                    dialogs.remove(dialogInterface)
                if (dialogs.size > 0)
                    (dialogs[0] as MaterialDialog).show()
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }
}
