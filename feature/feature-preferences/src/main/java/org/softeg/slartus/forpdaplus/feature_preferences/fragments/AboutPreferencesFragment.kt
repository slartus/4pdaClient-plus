package org.softeg.slartus.forpdaplus.feature_preferences.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import org.softeg.slartus.forpdacommon.appFullName
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppNavigator
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppScreen
import org.softeg.slartus.forpdaplus.core_ui.navigation.AppService
import org.softeg.slartus.forpdaplus.feature_preferences.App
import org.softeg.slartus.forpdaplus.feature_preferences.R
import org.softeg.slartus.hosthelper.HostHelper
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject

@Suppress("unused")
@AndroidEntryPoint
class AboutPreferencesFragment : PreferenceFragmentCompat() {
    @Inject
    lateinit var appNavigator: AppNavigator

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            "About.AppVersion" -> {
                showAbout(requireContext())
                return true
            }
            "About.History" -> {
                showAboutHistory(requireContext())
                return true
            }
            "About.ShareIt" -> {
                showShareIt(requireContext())
                return true
            }
            "About.ShowTheme" -> {
                showTheme("271502")
                return true
            }
            "About.CheckNewVersion" -> {
                checkUpdates()
                return true
            }
            "About.OpenThemeForPda" -> {
                showTheme("820313")
                return true
            }
        }
        return false
    }

    private fun showTheme(themeId: String) {
        appNavigator.navigateTo(AppScreen.Topic(themeId))
        activity?.finish()
    }

    private fun checkUpdates() {
        appNavigator.startService(AppService.VersionChecker)
    }


    private fun showAbout(context: Context) {
        val text = """
                <b>Неофициальный клиент для сайта <a href="https://www.4pda.ru">4pda.ru</a></b><br/><br/>
                <b>Автор: </b> Артём Слинкин aka <a href="https://4pda.ru/forum/index.php?showuser=236113">slartus</a><br/>
                <b>E-mail:</b> <a href="mailto:slartus+4pda@gmail.com">slartus+4pda@gmail.com</a><br/><br/>
                <b>Разработчик(v3.x): </b> Евгений Низамиев aka <a href="https://4pda.ru/forum/index.php?showuser=2556269">Radiation15</a><br/>
                <b>E-mail:</b> <a href="mailto:radiationx@yandex.ru">radiationx@yandex.ru</a><br/><br/>
                <b>Разработчик(v3.x):</b> Александр Тайнюк aka <a href="https://4pda.ru/forum/index.php?showuser=1726458">iSanechek</a><br/>
                <b>E-mail:</b> <a href="mailto:devuicore@gmail.com">devuicore@gmail.com</a><br/><br/>
                <b>Помощник разработчиков: </b> Алексей Шолохов aka <a href="https://4pda.ru/forum/index.php?showuser=96664">Морфий</a>
                <b>E-mail:</b> <a href="mailto:asolohov@gmail.com">asolohov@gmail.com</a><br/><br/>
                <b>Благодарности: </b> <br/>
                * <b><a href="https://4pda.ru/forum/index.php?showuser=1657987">__KoSyAk__</a></b> Иконка программы<br/>
                * <b>Пользователям 4pda</b> (тестирование, идеи, поддержка)
                <br/><br/>Copyright 2011-2016 Artem Slinkin <slartus@gmail.com>
                """.trimIndent().replace("4pda.ru", HostHelper.host)
        @Suppress("DEPRECATION")
        MaterialDialog.Builder(context)
            .title(context.appFullName)
            .content(Html.fromHtml(text))
            .positiveText(R.string.ok)
            .show()
        //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        //textView.setTextSize(12);

        //textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private fun showAboutHistory(context: Context) {
        val sb = StringBuilder()
        try {
            val br = BufferedReader(
                InputStreamReader(
                    App.getInstance().assets.open("history.txt"),
                    "UTF-8"
                )
            )
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
        } catch (e: IOException) {
            Timber.e(e)
        }
        MaterialDialog.Builder(context)
            .title(context.getString(R.string.ChangesHistory))
            .content(sb)
            .positiveText(R.string.ok)
            .show()
        //TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        //textView.setTextSize(12);
    }

    private fun showShareIt(context: Context) {
        val sendMailIntent = Intent(Intent.ACTION_SEND)
        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.Recomend))
        sendMailIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.RecommendText))
        sendMailIntent.type = "text/plain"
        context.startActivity(
            Intent.createChooser(
                sendMailIntent,
                context.getString(R.string.SendBy_)
            )
        )
    }
}