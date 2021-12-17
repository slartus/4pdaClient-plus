package org.softeg.slartus.forpdaplus.repositories

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.softeg.slartus.forpdaplus.App
import org.softeg.slartus.forpdaplus.R
import org.softeg.slartus.forpdaplus.core.entities.UserInfo
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import ru.slartus.http.Http
import ru.slartus.http.PersistentCookieStore

class UserInfoRepositoryImpl(cookieStore: PersistentCookieStore) : UserInfoRepository {
    private object Holder {
        val INSTANCE = UserInfoRepositoryImpl(Http.instance.cookieStore)
    }

    private val _userInfo =
        MutableStateFlow(UserInfo(name = App.getInstance().getString(R.string.guest)))
    override val userInfo: Flow<UserInfo>
        get() = _userInfo

    override suspend fun isLogined(): Boolean {
        return _userInfo.value.logined
    }

    private var tempUserInfo: UserInfo =
        UserInfo(name = App.getInstance().getString(R.string.guest))

    init {
        App.getInstance().addToDisposable(
            cookieStore
                .memberId
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { memberId ->
                    setNewUserInfo(
                        if (memberId == "deleted" || memberId.isNullOrEmpty())
                            UserInfo(name = App.getInstance().getString(R.string.guest))
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

    override fun setQmsCount(value: Int?) {
        setNewUserInfo(_userInfo.value.copy(qmsCount = value))
    }

    fun setName(value: String) {
        setNewUserInfo(_userInfo.value.copy(name = value))
    }

    fun clear() {
        setNewUserInfo(UserInfo(name = App.getInstance().getString(R.string.guest)))
    }

    companion object {
        const val TAG = "UserInfoRepository"

        @JvmStatic
        @Deprecated("use hilt interface UserInfoRepository")
        val instance by lazy { Holder.INSTANCE }
    }
}

