package org.softeg.slartus.forpdaplus.navigation

import org.softeg.slartus.forpdaplus.core_ui.navigation.AppNavigator
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppScreen
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppService
import org.softeg.slartus.forpdaplus.feature_preferences.App
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager
import javax.inject.Inject

class AppNavigatorImpl @Inject constructor() : AppNavigator {

    override fun navigateTo(appScreen: AppScreen) {
        when (appScreen) {
            is AppScreen.Topic -> ThemeFragment.showTopicById(appScreen.topicId)
        }
    }

    override fun startService(appService: AppService) {
        when (appService) {
            is AppService.VersionChecker -> startVersionChecked()
        }
    }

    private fun startVersionChecked() {
        val notifiersManager = NotifiersManager()
        ForPdaVersionNotifier(notifiersManager, 0, true).start(App.getInstance())
    }
}