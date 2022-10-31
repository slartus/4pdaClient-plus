package org.softeg.slartus.forpdaplus.forum.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.softeg.slartus.forpdaplus.forum.data.models.ForumItemResponse
import org.softeg.slartus.forpdaplus.forum.data.models.mapToForumItemOrNull
import ru.softeg.slartus.forum.api.ForumItem
import javax.inject.Inject
import org.softeg.slartus.forpdacommon.loadAssetsText

class AssetsForumDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getAll(): List<ForumItem> = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            val response = context.loadAssetsText("forum_struct.json")

            val itemsListType = object : TypeToken<List<ForumItemResponse>>() {}.type
            val responseItems: List<ForumItemResponse> = Gson().fromJson(response, itemsListType)
            responseItems.mapNotNull { it.mapToForumItemOrNull() }
        }.getOrNull() ?: emptyList()
    }
}