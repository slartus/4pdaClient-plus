package org.softeg.slartus.forpdaplus.core.services

import org.softeg.slartus.forpdaplus.core.entities.UserProfile

interface UserProfileService {
    suspend fun getUserProfile(userId: String, resultParserId:String?=null): UserProfile?
    companion object{
        const val ARG_USER_ID = "UserProfileService.ARG_USER_ID"
    }
}