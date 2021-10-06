package org.softeg.slartus.forpdaplus.navigation

import org.softeg.slartus.forpdaplus.core_ui.navigation.AppNavigator
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppScreen
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment
import javax.inject.Inject

class AppNavigatorImpl @Inject constructor() : AppNavigator {
    override fun navigateTo(appScreen: AppScreen) {
        when (appScreen) {
            is AppScreen.Topic -> ThemeFragment.showTopicById(appScreen.topicId)
        }
    }
}