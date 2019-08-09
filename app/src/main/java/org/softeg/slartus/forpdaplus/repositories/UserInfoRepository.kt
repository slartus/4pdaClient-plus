package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.softeg.slartus.forpdaplus.App
import ru.slartus.http.Http

class UserInfoRepository private constructor() {
    private object Holder {
        val INSTANCE = UserInfoRepository()
    }

    companion object {
        const val TAG = "UserInfoRepository"
        val instance by lazy { Holder.INSTANCE }
    }

    init {
        App.getInstance().addToDisposable(Http.instance.cookieStore
                .memberId
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { memberId ->
                    userInfo.value?.let {
                        it.id = if (memberId == "deleted") "" else memberId
                        if (it.id.isEmpty()) {
                            it.clear()
                        } else {
                            it.logined = true
                        }
                        userInfo.onNext(it)
                    }
                })
    }

    val userInfo: BehaviorSubject<UserInfo> = BehaviorSubject.createDefault(UserInfo())


    fun setMentionsCount(value: Int?) {
        if (userInfo.hasValue() && userInfo.value?.mentionsCount != value) {
            userInfo.value?.let {
                it.mentionsCount = value
                userInfo.onNext(it)
            }
        }
    }

    fun getQmsCount() = userInfo.value?.qmsCount ?: 0
    fun setQmsCount(value: Int?) {
        if (userInfo.hasValue() && userInfo.value?.qmsCount != value) {
            userInfo.value?.let {
                it.qmsCount = value
                userInfo.onNext(it)
            }
        }
    }

    fun getLogined() = userInfo.value?.logined ?: false

    fun getId() = userInfo.value?.id ?: ""

    fun getName() = userInfo.value?.name ?: ""
    fun setName(value: String) {
        if (userInfo.hasValue() && userInfo.value?.name != value) {
            userInfo.value?.let {
                it.name = value
                userInfo.onNext(it)
            }
        }
    }

    fun clear() {
        userInfo.value?.let {
            it.clear()
            userInfo.onNext(it)
        }
    }
}

/**
 * Информация о текущем пользователе
 */
class UserInfo {
    var id = ""
    var name = "Гость"
    var mentionsCount: Int? = 0

    fun mentionsCountOrDefault(defaultValue: Int): Int {
        return mentionsCount ?: defaultValue
    }

    var qmsCount: Int? = 0

    fun qmsCountOrDefault(defaultValue: Int): Int {
        return qmsCount ?: defaultValue
    }

    var logined = false

    fun clear() {
        logined = false
        id = ""
        name = "Гость"
        mentionsCount = 0
        qmsCount = 0
    }
}
