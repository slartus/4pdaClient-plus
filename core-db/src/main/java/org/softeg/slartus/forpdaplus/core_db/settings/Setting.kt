package org.softeg.slartus.forpdaplus.core_db.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Setting (
    @PrimaryKey
    val id: Int? = null
)