package ru.slartus.domain_user_profile.entities

import org.softeg.slartus.forpdaplus.core.entities.UserProfile

data class UserProfileImpl(
    override val id: String,
    override val nick: String?
) : UserProfile