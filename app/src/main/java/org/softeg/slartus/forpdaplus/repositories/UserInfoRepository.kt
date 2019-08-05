package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.subjects.BehaviorSubject

class UserInfoRepository private constructor() {
    private object Holder {
        val INSTANCE = UserInfoRepository()
    }

    companion object {
        const val TAG = "UserInfoRepository"
        val instance by lazy { Holder.INSTANCE }
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

    fun setQmsCount(value: Int?) {
        if (userInfo.hasValue() && userInfo.value?.qmsCount != value) {
            userInfo.value?.let {
                it.qmsCount = value
                userInfo.onNext(it)
            }
        }
    }
}

/**
 * Информация о текущем пользователе
 */
class UserInfo {
    var mentionsCount: Int? = 0

    fun mentionsCountOrDefault(defaultValue: Int): Int {
        return mentionsCount ?: defaultValue
    }

    var qmsCount: Int? = 0

    fun qmsCountOrDefault(defaultValue: Int): Int {
        return qmsCount ?: defaultValue
    }
}
