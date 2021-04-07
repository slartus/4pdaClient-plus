package org.softeg.slartus.forpdaplus.fragments.base

import android.app.DialogFragment
import android.os.Bundle

import android.view.LayoutInflater
import android.view.ViewGroup
import org.softeg.slartus.forpdaplus.R

class ProgressDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.ProgressDialogTheme)
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        LayoutInflater.from(context).inflate(R.layout.fragment_progress, null)
}