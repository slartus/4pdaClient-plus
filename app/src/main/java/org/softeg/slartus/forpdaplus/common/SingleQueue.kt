package org.softeg.slartus.forpdaplus.common

import io.reactivex.Single
import io.reactivex.SingleObserver
import org.softeg.slartus.forpdaplus.App

class SingleQueue(private val queue: List<() -> Single<Boolean>>) : Single<Boolean>() {
    private var index = -1
    private var result: SingleObserver<in Boolean>? = null


    private fun nextStep() {
        index++
        if (index >= queue.size) {
            result?.onSuccess(true)
        } else {
            App.getInstance().addToDisposable(
                    queue[index]()
                            .subscribe(
                                    {
                                        if (!it) {
                                            result?.onSuccess(false)
                                        } else {
                                            nextStep()
                                        }
                                    },
                                    {
                                        result?.onError(it)
                                    }
                            ))
        }
    }

    override fun subscribeActual(observer: SingleObserver<in Boolean>) {
        result = observer
        nextStep()
    }
}