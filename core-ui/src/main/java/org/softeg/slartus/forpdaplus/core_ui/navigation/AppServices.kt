package org.softeg.slartus.forpdaplus.core_ui.navigation

sealed class AppService {
    object VersionChecker : AppService()
}