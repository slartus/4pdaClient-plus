package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.softeg.slartus.forpdaapi.users.User
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import ru.slartus.http.Http
import ru.slartus.http.PersistentCookieStore

interface UserInfoRepository {
    val userInfo: Flow<UserInfo>
}

class UserInfoRepositoryImpl(cookieStore: PersistentCookieStore) : UserInfoRepository {
    private object Holder {
        val INSTANCE = UserInfoRepositoryImpl(Http.instance.cookieStore)
    }

    private val _userInfo = MutableStateFlow(UserInfo())
    override val userInfo: Flow<UserInfo>
        get() = _userInfo
    private var tempUserInfo: UserInfo = UserInfo()

    init {
        App.getInstance().addToDisposable(
            cookieStore
                .memberId
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { memberId ->
                    setNewUserInfo(
                        if (memberId == "deleted" || memberId.isNullOrEmpty())
                            UserInfo()
                        else
                            _userInfo.value.copy(
                                id = memberId,
                                logined = true
                            )
                    )
                })
    }

    private fun setNewUserInfo(value: UserInfo) {
        _userInfo.value = value
        tempUserInfo = value
    }

    fun getLogined() = tempUserInfo.logined

    fun getId() = tempUserInfo.id

    fun getName() = tempUserInfo.name
    fun getQmsCount() = tempUserInfo.qmsCount

    fun setMentionsCount(value: Int?) {
        setNewUserInfo(_userInfo.value.copy(mentionsCount = value))
    }

    fun setQmsCount(value: Int?) {
        setNewUserInfo(_userInfo.value.copy(qmsCount = value))
    }

    fun setName(value: String) {
        setNewUserInfo(_userInfo.value.copy(name = value))
    }

    fun clear() {
        setNewUserInfo(UserInfo())
    }

    companion object {
        const val TAG = "UserInfoRepository"

        @JvmStatic
        @Deprecated("use hilt interface UserInfoRepository")
        val instance by lazy { Holder.INSTANCE }
    }
}

/**
 * Информация о текущем пользователе
 */
data class UserInfo(
    val id: String = "",
    val name: String = App.getInstance().getString(R.string.guest),
    val mentionsCount: Int? = 0,
    val qmsCount: Int? = 0,
    val logined: Boolean = false,
    val reputation: String = "",
    val avatarUrl: String = ""
)
