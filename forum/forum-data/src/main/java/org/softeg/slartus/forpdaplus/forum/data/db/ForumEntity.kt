package org.softeg.slartus.forpdaplus.forum.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.softeg.slartus.forum.api.ForumItem

@Entity(tableName = "forum")
data class ForumEntity(
    @PrimaryKey(autoGenerate = true)
    val _id: Int? = null,
    val id: String,
    val title: String,
    val description: String,
    val isHasTopics: Boolean,
    val isHasForums: Boolean,
    val iconUrl: String?,
    val parentId: String?
)

fun ForumEntity.mapToItem() = ForumItem(
    this.id,
    this.title,
    this.description,
    this.isHasTopics,
    this.isHasForums,
    this.iconUrl,
    this.parentId
)

fun ForumItem.mapToDb() = ForumEntity(null,
    this.id,
    this.title,
    this.description,
    this.isHasTopics,
    this.isHasForums,
    this.iconUrl,
    this.parentId
)