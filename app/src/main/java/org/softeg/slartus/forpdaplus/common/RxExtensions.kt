package org.softeg.slartus.forpdaplus.common

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject


fun <T> Subject<T>.runFromIoToUi() =
    this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

class RxExtensions {
}