package org.softeg.slartus.forpdaplus.core_ui.navigation

interface AppRouter {
    fun navigateTo(appScreen: AppScreen)

    fun startService(appService: AppService)

    fun exit()
}