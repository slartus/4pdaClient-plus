package org.softeg.slartus.forpdaplus.navigation

import androidx.fragment.app.FragmentActivity
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.androidx.AppNavigator
import org.softeg.slartus.forpdaplus.R
import javax.inject.Inject

data class ShowDialog(val screen: DialogScreen) : Command

class MainActivityNavigator @Inject constructor(activity: FragmentActivity) :
    AppNavigator(activity, R.id.content_frame) {

    override fun applyCommand(command: Command) {
        if (command is ShowDialog) {
            val tag = command.screen.screenKey
            val dialog = command.screen.createDialog(fragmentFactory)
            dialog.show(fragmentManager, tag)
        } else {
            super.applyCommand(command)
        }
    }
}