package ru.slartus.domain_user_profile

import org.softeg.slartus.forpdaplus.core.entities.UserProfile
import org.softeg.slartus.forpdaplus.core.repositories.UserProfileRepository
import org.softeg.slartus.forpdaplus.core.services.UserProfileService
import ru.slartus.domain_user_profile.parsers.UserProfileParser
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileParser: UserProfileParser,
    private val userProfileService: UserProfileService
) : UserProfileRepository {
    override suspend fun getUserProfile(userId: String): UserProfile? {
        return userProfileService.getUserProfile(userId, userProfileParser.id)
    }
}