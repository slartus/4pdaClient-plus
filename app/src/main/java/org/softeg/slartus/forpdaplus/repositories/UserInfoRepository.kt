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

    fun getLogined() = userInfo.value?.logined?:false

    fun setLogined(value: Boolean) {
        if (userInfo.hasValue() && userInfo.value?.logined != value) {
            userInfo.value?.let {
                it.logined = value
                userInfo.onNext(it)
            }
        }
    }

    fun getId() = userInfo.value?.id?:""

    fun setId(value: String) {
        if (userInfo.hasValue() && userInfo.value?.id != value) {
            userInfo.value?.let {
                it.id = value
                userInfo.onNext(it)
            }
        }
    }

    fun getName() = userInfo.value?.name?:""
    fun setName(value: String) {
        if (userInfo.hasValue() && userInfo.value?.name != value) {
            userInfo.value?.let {
                it.name = value
                userInfo.onNext(it)
            }
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
}
