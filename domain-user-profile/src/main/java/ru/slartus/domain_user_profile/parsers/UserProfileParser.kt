package ru.slartus.domain_user_profile.parsers

import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jsoup.Jsoup
import org.softeg.slartus.forpdaplus.core.entities.UserProfile
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.services.UserProfileService
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.domain_user_profile.entities.UserProfileImpl
import java.util.regex.Pattern
import javax.inject.Inject

class UserProfileParser @Inject constructor() : Parser<UserProfile?> {
    override val id: String
        get() = UserProfileParser::class.java.simpleName

    private val _data = MutableStateFlow<UserProfile?>(null)
    override val data
        get() = _data.asStateFlow()

    override fun isOwn(url: String, args: Bundle?): Boolean {
        return args?.containsKey(UserProfileService.ARG_USER_ID) == true &&
                checkUrlRegex.matcher(url).find()
    }

    override suspend fun parse(page: String, args: Bundle?): UserProfile? {
        val id = args?.getString(UserProfileService.ARG_USER_ID)
        val jsoupDocument = Jsoup.parse(page, HostHelper.schemedHost)
        val userNick = jsoupDocument.select("div.user-box > h1").first()?.text()
        id ?: return null
        return UserProfileImpl(id, userNick)
    }

    companion object {
        private val checkUrlRegex by lazy {
            Pattern.compile(
                """showuser=\d+""",
                Pattern.CASE_INSENSITIVE
            )
        }
    }
}