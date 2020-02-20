package org.softeg.slartus.forpdaplus

import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseActivity: AppCompatActivity() {
    private var compositeDisposable = CompositeDisposable()


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