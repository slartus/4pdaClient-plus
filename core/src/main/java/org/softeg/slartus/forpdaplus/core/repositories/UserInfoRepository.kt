package org.softeg.slartus.forpdaplus.core.repositories

import kotlinx.coroutines.flow.Flow

interface UserInfoRepository {
    val userInfo: Flow<UserInfo>
}


/**
 * Информация о текущем пользователе
 */
data class UserInfo(
    val id: String = "",
    val name: String,
    val mentionsCount: Int? = 0,
    val qmsCount: Int? = 0,
    val logined: Boolean = false,
    val reputation: String = "",
    val avatarUrl: String = ""
)