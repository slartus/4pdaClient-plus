package org.softeg.slartus.forpdaplus.domain_qms.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.softeg.slartus.qms.api.models.QmsContact

@Entity(tableName = "qms_contacts")
data class QmsContactEntity(
    @PrimaryKey
    val id: Long,
    val nick: String,
    val avatarUrl: String?,
    val messagesCount: Int,
    val sort: Int
)


fun QmsContactEntity.map(): QmsContact =
    QmsContact(this.id.toString(), this.nick, this.avatarUrl, this.messagesCount)

fun QmsContact.map(sort: Int): QmsContactEntity =
    QmsContactEntity(id.toLong(), this.nick, this.avatarUrl, this.newMessagesCount, sort)

