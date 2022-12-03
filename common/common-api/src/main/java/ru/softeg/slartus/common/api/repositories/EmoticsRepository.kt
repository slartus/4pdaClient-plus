package ru.softeg.slartus.common.api.repositories

import ru.softeg.slartus.common.api.models.Emotic

interface EmoticsRepository {
    suspend fun loadEmotics(): List<Emotic>
    suspend fun loadFavoriteEmotics(): List<Emotic>
    suspend fun addFavoriteEmotic(emotic: Emotic)
}