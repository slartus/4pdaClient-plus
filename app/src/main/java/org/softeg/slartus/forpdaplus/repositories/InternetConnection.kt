package org.softeg.slartus.forpdaplus.repositories

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.common.AppLog
import java.util.concurrent.TimeUnit

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
    fun loadDataOnInternetConnected(action: () -> Unit, timeoutSeconds: Long = 10) {
        var localSubscriber: Disposable? = null
        localSubscriber =
                Observable.merge(mInternetState.map { ConnectInfo(it, false) },
                        Observable.timer(timeoutSeconds, TimeUnit.SECONDS).map { ConnectInfo(connected = false, timeout = true) })
                        .filter { it.connected || it.timeout }
                        .firstOrError()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            localSubscriber?.dispose()
                            action()
                        }, {
                            AppLog.e(it)
                        })
        App.getInstance().addToDisposable(localSubscriber)
    }

    class ConnectInfo(val connected: Boolean, val timeout: Boolean)
}