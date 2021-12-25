package ru.slartus.domain_user_profile

import androidx.core.os.bundleOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.core.entities.UserProfile
import org.softeg.slartus.forpdaplus.core.interfaces.ParseFactory
import org.softeg.slartus.forpdaplus.core.services.AppHttpClient
import org.softeg.slartus.forpdaplus.core.services.UserProfileService
import org.softeg.slartus.hosthelper.HostHelper
import javax.inject.Inject

class UserProfileServiceImpl @Inject constructor(
    private val httpClient: AppHttpClient,
    private val parseFactory: ParseFactory
) : UserProfileService {
    override suspend fun getUserProfile(userId: String, resultParserId: String?): UserProfile? =
        withContext(Dispatchers.IO) {
            val url = "${HostHelper.schemedHost}/forum/index.php?showuser=$userId"
            val pageBody = httpClient.performGet(url)
            parseFactory.parse(
                url,
                pageBody,
                resultParserId,
                bundleOf(UserProfileService.ARG_USER_ID to userId)
            )
        }

}