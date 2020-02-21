package org.softeg.slartus.forpdaplus.repositories

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.common.AppLog

class InternetConnection private constructor() {
    private object Holder {
        val INSTANCE = InternetConnection()
    }

    companion object {
        const val TAG = "InternetConnection"
        @JvmStatic
        val instance by lazy { Holder.INSTANCE }
    }

    fun subscribeInternetState() {
        mInternetSubscriber = ReactiveNetwork.observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isConnectedToInternet ->
                    mInternetState.onNext(isConnectedToInternet)
                }
        App.getInstance().addToDisposable(mInternetSubscriber)
    }

    private var mInternetState: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    private var mInternetSubscriber: Disposable? = null
    fun loadDataOnInternetConnected(action: () -> Unit) {
        var localSubscriber: Disposable? = null
        localSubscriber = mInternetState
                .filter { it }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isConnectedToInternet ->
                    if (isConnectedToInternet) {
                        localSubscriber?.dispose()

                        action()
                    }
                }, {
                    AppLog.e(it)
                })
        App.getInstance().addToDisposable(localSubscriber)
    }
}