package org.softeg.slartus.forpdaplus.common.data.repositories.emotics

import ru.softeg.slartus.common.api.models.Emotic
import ru.softeg.slartus.common.api.repositories.EmoticsRepository
import javax.inject.Inject

class EmoticsRepositoryImpl @Inject constructor(
    private val localEmoticsDataSource: LocalEmoticsDataSource
) : EmoticsRepository {
    override suspend fun loadEmotics(): List<Emotic> = localEmoticsDataSource.getEmotics()

    override suspend fun loadFavoriteEmotics(): List<Emotic> =
        localEmoticsDataSource.getFavoriteEmotics()

    override suspend fun addFavoriteEmotic(emotic: Emotic) =
        localEmoticsDataSource.addFavoriteEmotic(emotic)
}