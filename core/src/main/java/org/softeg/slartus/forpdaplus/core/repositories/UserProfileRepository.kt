package org.softeg.slartus.forpdaplus.core.repositories

import org.softeg.slartus.forpdaplus.core.entities.UserProfile

interface UserProfileRepository {
    suspend fun getUserProfile(userId: String): UserProfile?
}