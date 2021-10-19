package org.softeg.slartus.forpdaplus

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.softeg.slartus.forpdaplus.log.ActivityTimberTree
import timber.log.Timber
import java.lang.ref.WeakReference

abstract class BaseActivity : AppCompatActivity() {
    private var compositeDisposable = CompositeDisposable()

    @Suppress("LeakingThis")
    private val timberTree = ActivityTimberTree(WeakReference(this))

    override fun onStart() {
        super.onStart()
        Timber.plant(timberTree)
    }

    override fun onStop() {
        super.onStop()
        Timber.uproot(timberTree)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun addToDisposable(disposable: Disposable?) {
        disposable?.let {
            compositeDisposable.add(it)
        }
    }
}