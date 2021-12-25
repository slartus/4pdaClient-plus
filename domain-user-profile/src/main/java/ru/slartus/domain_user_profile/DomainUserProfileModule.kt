package ru.slartus.domain_user_profile

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.softeg.slartus.forpdaplus.core.entities.UserProfile
import org.softeg.slartus.forpdaplus.core.interfaces.Parser
import org.softeg.slartus.forpdaplus.core.repositories.UserProfileRepository
import org.softeg.slartus.forpdaplus.core.services.UserProfileService
import ru.slartus.domain_user_profile.parsers.UserProfileParser
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DomainUserProfileModule {
    @Binds
    @Singleton
    fun provideUserProfileServiceImpl(userProfileServiceImpl: UserProfileServiceImpl): UserProfileService

    @Binds
    @Singleton
    fun provideUserProfileParser(parser: UserProfileParser): Parser<UserProfile>

    @Binds
    fun provideUserProfileRepository(userProfileRepositoryImpl: UserProfileRepositoryImpl): UserProfileRepository
}
