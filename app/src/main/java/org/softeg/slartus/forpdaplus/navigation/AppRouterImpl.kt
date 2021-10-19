package org.softeg.slartus.forpdaplus.navigation

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.github.terrakok.cicerone.Router
import org.softeg.slartus.forpdaplus.IntentActivity
import org.softeg.slartus.forpdaplus.core_ui.navigation.*
import org.softeg.slartus.forpdaplus.feature_notes.ui.newNote.NewNoteDialogFragment
import org.softeg.slartus.forpdaplus.feature_preferences.App
import org.softeg.slartus.forpdaplus.fragments.NoteFragment
import org.softeg.slartus.forpdaplus.fragments.topic.ThemeFragment
import org.softeg.slartus.forpdaplus.mainnotifiers.ForPdaVersionNotifier
import org.softeg.slartus.forpdaplus.mainnotifiers.NotifiersManager
import java.lang.ref.WeakReference
import javax.inject.Inject

class AppRouterImpl @Inject constructor(
    context: Context,
    private val router: ExtendedRouter
) : AppRouter {
    private val contextRef = WeakReference(context)
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

    override fun openUrl(url: String) {
        contextRef.get()?.let {
            context->
            val handler= Handler(Looper.getMainLooper())
            IntentActivity
                .tryShowUrl(context, handler, url, true, false)
        }

    }

    override fun sendResult(key: String, data: Any) {
        router.sendResult(key, data)
    }

    override fun setResultListener(key: String, listener: ResultListener): ResultListenerHandler {
        val resultListenerHandler = router.setResultListener(key) { data ->
            listener.onResult(data)
        }
        return ResultListenerHandler {
            resultListenerHandler.dispose()
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