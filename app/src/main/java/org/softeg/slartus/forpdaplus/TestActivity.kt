package org.softeg.slartus.forpdaplus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.softeg.slartus.forpdaplus.AppTheme.themeStyleResID

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(themeStyleResID)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)
    }
}