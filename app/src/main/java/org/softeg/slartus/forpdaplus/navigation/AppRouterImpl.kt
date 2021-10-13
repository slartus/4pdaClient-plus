package org.softeg.slartus.forpdaplus.navigation

import com.github.terrakok.cicerone.Router
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppRouter
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppScreen
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppService
import org.softeg.slartus.forpdaplus.feature_notes.ui.newNote.NewNoteDialogFragment
import org.softeg.slartus.forpdaplus.feature_preferences.App
import org.softeg.slartus.forpdaplus.fragments.NoteFragment
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager
import javax.inject.Inject

class AppRouterImpl @Inject constructor(private val router: ExtendedRouter) : AppRouter {
    override fun navigateTo(appScreen: AppScreen) {
        when (appScreen) {
            is AppScreen.Topic -> ThemeFragment.showTopicById(appScreen.topicId)
            is AppScreen.Note -> NoteFragment.showNote(appScreen.noteId)
            is AppScreen.NewNote -> router.showDialog(getNewNoteScreen(appScreen))
        }
    }

    override fun startService(appService: AppService) {
        when (appService) {
            is AppService.VersionChecker -> startVersionChecked()
        }
    }

    override fun exit() {
        router.exit()
    }

    private fun startVersionChecked() {
        val notifiersManager = NotifiersManager()
        ForPdaVersionNotifier(notifiersManager, 0, true).start(App.getInstance())
    }

    private fun getNewNoteScreen(appScreen: AppScreen.NewNote) = DialogScreen {
        NewNoteDialogFragment.newInstance(
            appScreen.title,
            appScreen.body,
            appScreen.url,
            appScreen.topicId,
            appScreen.topicTitle,
            appScreen.postId,
            appScreen.userId,
            appScreen.userName
        )
    }
}

class ExtendedRouter @Inject constructor() : Router() {
    fun showDialog(screen: DialogScreen) {
        executeCommands(ShowDialog(screen))
    }
}