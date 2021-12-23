package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow
import org.softeg.slartus.forpdaplus.core.entities.UserInfo

interface UserInfoRepository {
    val userInfo: Flow<UserInfo>
    suspend fun isLogined(): Boolean

    @Deprecated("use QmsCountRepository instead")
    fun setQmsCount(value: Int?)
}