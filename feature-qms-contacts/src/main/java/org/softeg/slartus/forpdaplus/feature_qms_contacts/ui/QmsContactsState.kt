package org.softeg.slartus.forpdaplus.feature_qms_contacts.ui

import android.widget.Toast
import androidx.annotation.StringRes
import org.softeg.slartus.forpdaplus.feature_qms_contacts.ui.fingerprints.QmsContactItem

data class QmsContactsState(
    val items: List<QmsContactItem> = emptyList(),
    val filteredItems: List<QmsContactItem> = items
)

sealed class QmsContactsAction {
    object Empty : QmsContactsAction()
    data class Error(val exception: Throwable) : QmsContactsAction()
    data class ShowToast(
        @StringRes val resId: Int,// maybe need use enum for clear model
        val duration: Int = Toast.LENGTH_SHORT
    ) : QmsContactsAction()
}

sealed class QmsContactsEvent {
    data class HiddenChanged(val hidden: Boolean) : QmsContactsEvent()
    data class OnSearchTextChanged(val text: String) : QmsContactsEvent()
}


enum class AccentColor {
    Standard, Blue, Gray
}