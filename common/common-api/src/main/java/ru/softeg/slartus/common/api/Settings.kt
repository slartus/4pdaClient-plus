package ru.softeg.slartus.common.api


import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface Settings {
    suspend fun remove(key: String)

    suspend fun hasKey(key: String): Boolean

    suspend fun putString(key: String, value: String)

    suspend fun getString(key: String, defaultValue: String?): String?
}

suspend fun Settings.putStringList(key: String, value: List<String>) {
    putString(key, Json.encodeToString(value))
}

suspend fun Settings.getStringList(key: String, defaultValue: List<String>?): List<String>? =
    getString(key, defaultValue?.let { Json.encodeToString(it) })?.let {
        Json.decodeFromString(it)
    }

suspend inline fun <reified T> Settings.putList(key: String, value: List<T>) {
    putString(key, Json.encodeToString(value))
}

suspend inline fun <reified T> Settings.getList(key: String, defaultValue: List<T>?): List<T>? =
    getString(key, defaultValue?.let { Json.encodeToString(it) })?.let {
        Json.decodeFromString(it)
    }