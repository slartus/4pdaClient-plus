package org.softeg.slartus.forpdaplus.mainnotifiers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.webkit.WebView;
import android.widget.TextView;

import org.softeg.slartus.forpdacommon.DateExtensions;
import org.softeg.slartus.forpdacommon.ExtPreferences;
import org.softeg.slartus.forpdacommon.Http;
import org.softeg.slartus.forpdaplus.Client;
import org.softeg.slartus.forpdaplus.MyApp;
import org.softeg.slartus.forpdaplus.classes.AlertDialogBuilder;
import org.softeg.slartus.forpdaplus.prefs.Preferences;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by slartus on 03.06.2014.
 */
public class TopicAttentionNotifier extends MainNotifier {
    public TopicAttentionNotifier() {
        super("TopicAttentionNotifier", 2);
    }

    public void start(Context context) {
        if (!isTime())
            return;
        saveTime();
        showNotify(context);
    }

    @Override
    protected boolean isTime() {
        GregorianCalendar lastShowpromoCalendar = new GregorianCalendar();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApp.getContext());
        Date lastCheckDate = ExtPreferences.getDateTime(prefs, "notifier." + name, null);
        if (lastCheckDate == null) {
            saveTime();
            return true;
        }

        lastShowpromoCalendar.setTime(lastCheckDate);

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        int hours = DateExtensions.getHoursBetween(calendar.getTime(), lastShowpromoCalendar.getTime());
        return hours >= period;
    }


    public static void showNotify(final Context context) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                try {
                    String url = "http://4pda.ru/forum/index.php?showtopic=271502";
                    String page = Http.getPage(url, "windows-1251");
                    Matcher m = Pattern
                            .compile("<a name=\"(attention_\\d+_\\d+_\\d+_\\d+)\" title=\"attention_\\d+_\\d+_\\d+_\\d+\">.*?</a>(.*?)<br\\s*/>\\s*---+\\s*<br\\s*/>",
                                    Pattern.CASE_INSENSITIVE).matcher(page);
                    if (!m.find())
                        return;

                    String topicAttentionId = m.group(1);
                    String lastAttentionId = Preferences.Attention.getAttentionId();

                    if (topicAttentionId.equals(lastAttentionId))
                        return;

                    Preferences.Attention.setAttentionId(topicAttentionId);

                    final String topicAttention = m.group(2);
                    handler.post(new Runnable() {
                        public void run() {
                            StringBuilder body=new StringBuilder();
                            body.append("<http>\n");
                            body.append("<head>\n");
                            body.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
                            body.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n");
                            body.append("</head>");
                            body.append("<body>");
                            body.append(topicAttention);
                            body.append("</body>");
                            body.append("</html>");
                            WebView webView=new WebView(context);
                            webView.getSettings().supportZoom();
                            AlertDialog dialog = new AlertDialogBuilder(context)
                                    .setTitle("Обьявление")
                                    .setView(webView)
                                    .setPositiveButton("Я прочитал", null)
                                    .create();
                            dialog.show();
                            webView.loadDataWithBaseURL("http://4pda.ru/forum/", body.toString(), "text/html", "UTF-8", null);

                        }
                    });

                } catch (Throwable ignored) {

                }
            }
        }).start();

    }

}
