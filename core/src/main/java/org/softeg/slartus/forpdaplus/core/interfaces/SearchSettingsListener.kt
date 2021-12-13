package org.softeg.slartus.forpdaplus.core.interfaces

import org.softeg.slartus.forpdaplus.core.entities.SearchSettings

interface SearchSettingsListener {
    fun getSearchSettings(): SearchSettings?
}