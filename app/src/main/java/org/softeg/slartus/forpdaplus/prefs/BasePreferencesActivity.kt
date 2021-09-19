package org.softeg.slartus.forpdaplus.prefs

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceActivity
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.AppTheme.navBarColor
import org.softeg.slartus.forpdaplus.AppTheme.prefsThemeStyleResID
import java.util.*

/*
 * Created by slinkin on 27.12.13.
 */
open class BasePreferencesActivity : AppCompatActivity() {
    protected var mHandler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(prefsThemeStyleResID)
        super.onCreate(savedInstanceState)
        if (App.getInstance().preferences.getBoolean("coloredNavBar", true) &&
                Build.VERSION.SDK_INT >= 21) window.navigationBarColor = App.getInstance().resources.getColor(navBarColor)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        // Allow super to try and create a view first
        val result = super.onCreateView(name, context, attrs)
        if (result != null) {
            return result
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            when (name) {
                "EditText" -> return AppCompatEditText(this, attrs)
                "Spinner" -> return AppCompatSpinner(this, attrs)
                "CheckBox" -> return AppCompatCheckBox(this, attrs)
                "RadioButton" -> return AppCompatRadioButton(this, attrs)
                "CheckedTextView" -> return AppCompatCheckedTextView(this, attrs)
            }
        }
        return null
    }

    protected fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

//
//
//    override fun loadHeadersFromResource(resid: Int, target: List<Header>) {
//        super.loadHeadersFromResource(resid, target)
//        fragments.clear()
//        for (header in target) {
//            fragments.add(header.fragment)
//        }
//    }
//
//    override fun isValidFragment(fragmentName: String): Boolean {
//        return fragments.contains(fragmentName)
//    }

    companion object {
        private val fragments: MutableList<String> = ArrayList()
    }
}