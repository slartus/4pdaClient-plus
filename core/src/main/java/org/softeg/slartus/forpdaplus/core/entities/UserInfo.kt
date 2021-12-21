package org.softeg.slartus.forpdaplus.core.entities

data class UserInfo(
    val id: String = "",
    val name: String,
    val mentionsCount: Int? = 0,
    val qmsCount: Int? = 0,
    val logined: Boolean = false,
    val reputation: String = "",
    val avatarUrl: String = ""
)