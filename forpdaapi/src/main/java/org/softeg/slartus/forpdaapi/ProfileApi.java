package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.softeg.slartus.forpdaapi.classes.LoginForm;
import org.softeg.slartus.forpdacommon.NotReportException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by slinkin on 07.02.14.
 */
public class ProfileApi {
    /**
     * Проверка логина на странице
     *
     * @param pageBody
     * @return если залогинен - true
     */
    public static void checkLogin(String pageBody, LoginResult loginResult) {

        Matcher m = Pattern.compile("showuser=(\\d+)\">([^<]*)</a></b>.*?k=([a-z0-9]{32})", Pattern.CASE_INSENSITIVE)
                .matcher(pageBody);

        if (m.find()) {
            loginResult.setUserId(m.group(1));
            loginResult.setUserLogin(m.group(2));
            loginResult.setK(m.group(3));
            loginResult.setSuccess(true);

            String[] avatarPatterns = {"(?:'|\")([^'\"]*4pda.(?:to|ru)/*?forum/*?uploads/*?av-[^?'\"]*)",
                    "(?:'|\")([^'\"]*4pda.(?:to|ru)/*?forum/*?style_avatars/[^?'\"]*)"};
            for (String avatarPattern : avatarPatterns) {
                m = Pattern.compile(avatarPattern, Pattern.CASE_INSENSITIVE).matcher(pageBody);
                if (m.find()) {
                    loginResult.setUserAvatarUrl(m.group(1));
                    break;
                }
            }
        }
    }

    /**
     * @param httpClient
     * @param login
     * @param password
     * @param privacy
     * @return
     * @throws Exception
     */
    public static LoginResult login(IHttpClient httpClient, String login, String password,
                                    Boolean privacy, String capVal, String capTime, String capSig, String session) throws Exception {
        LoginResult loginResult = new LoginResult();

        Map<String, String> additionalHeaders = new HashMap<String, String>();

        additionalHeaders.put("login", login);
        additionalHeaders.put("password", password);
        //additionalHeaders.put("CookieDate", "1");
        if (privacy)
            additionalHeaders.put("hidden", "1");
        additionalHeaders.put("act", "auth");
        //additionalHeaders.put("CODE", "01");
        additionalHeaders.put("referer", "http://4pda.ru/forum/index.php");
        //additionalHeaders.put("s", session);
        additionalHeaders.put("captcha", capVal);
        additionalHeaders.put("captcha-time", capTime);
        additionalHeaders.put("captcha-sig", capSig);
        additionalHeaders.put("return", "http://4pda.ru/forum/index.php");


        String res = httpClient.performPost("http://4pda.ru/forum/index.php?act=auth", additionalHeaders);

        if (TextUtils.isEmpty(res)) {
            loginResult.setLoginError("Сервер вернул пустую страницу");
            return loginResult;
        }

        for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
            if ("member_id".equals(cookie.getName())) {
                // id пользователя. если он есть - логин успешный
                loginResult.setUserId(cookie.getValue());
                loginResult.setUserLogin(cookie.getValue());
                loginResult.setSuccess(true);
            } else if ("pass_hash".equals(cookie.getName())) {
                // хэш пароля
            } else if ("session_id".equals(cookie.getName())) {
                // id сессии
            }
        }

        checkLogin(res, loginResult);

        if (!loginResult.isSuccess()) {
            loginResult.setLoginError("Неизвестная ошибка");

            Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                    "\n" +
                    "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
            Matcher m = checkPattern.matcher(res);
            if (m.find()) {
                loginResult.setLoginError(m.group(1));
            } else {
                checkPattern = Pattern.compile("<ul[\\s\\S]*?<li>([\\s\\S]*?)</li>");
                m = checkPattern.matcher(res);
                if (m.find()) {
                    loginResult.setLoginError(m.group(1));
                } else {
                    loginResult.setLoginError(Html.fromHtml(res).toString());
                }
            }
        }


        return loginResult;
    }

    /**
     * ЛОгаут
     *
     * @param httpClient
     * @param k          идентификатор, полученный при логине
     * @return
     * @throws Throwable
     */
    public static String logout(IHttpClient httpClient, String k) throws Throwable {
        return httpClient.performGet("http://4pda.ru/forum/index.php?act=Login&CODE=03&k=" + k);
    }

    public static Profile getProfile(IHttpClient httpClient, CharSequence userID, String avType) throws IOException {
        Profile profile = new Profile();
        profile.setId(userID);
        String page = httpClient.performGet("http://4pda.ru/forum/index.php?showuser=" + userID);

        Matcher matcher = Pattern.compile("<form action=\"http:\\/\\/4pda\\.ru\\/forum\\/index\\.php\\?showuser[^>]*>[\\s\\S]*?<ul[^>]*>([\\s\\S]*)<\\/ul>[\\s\\S]*?<\\/form>").matcher(page);
        if (matcher.find()) {
            page = matcher.group(1).replaceFirst("<div class=\"photo\">[^<]*<img src=\"([^\"]*)\"[^<]*</div>",
                    "<div class=\"photo\"><div class=\"img " + avType + "\" style=\"background-image: url($1);\"></div></div>");
            matcher = Pattern.compile("<div class=\"user-box\">[\\s\\S]*?<h1>([\\s\\S]*?)</h1>").matcher(page);
            if (matcher.find())
                profile.setNick(matcher.group(1));
            page = page.replaceAll("<div class=\"profile-edit-links\">", "<div class=\"profile-edit-links\" style=\"display:none;\">");
            profile.setHtmlBody("<div class=\"user-profile-list\">" + page + "</div>");
        }
        return profile;
    }

    public static String getUserNick(IHttpClient httpClient, CharSequence userID) throws IOException {
        return Jsoup.parse(httpClient.performGet("http://4pda.ru/forum/index.php?showuser=" + userID)).select("div.user-box > h1").first().text();
    }

    public static LoginForm getLoginForm(IHttpClient httpClient) throws IOException {
        String page = httpClient.performGet("http://4pda.ru/forum/index.php?act=login&CODE=00");

        Matcher m = Pattern
                .compile("<form[^>]*?>([\\s\\S]*?)</form>")
                .matcher(page);
        if (!m.find())
            throw new NotReportException("Форма логина не найдена");
        String formText = m.group(1);
        m = Pattern
                .compile("<img[^>]*?src=\"([^\"]*?turing.gerkon.eu/captcha[^\"]*)\"")
                .matcher(formText);
        if (!m.find())
            throw new NotReportException("Капча не найдена");

        LoginForm loginForm = new LoginForm();
        loginForm.setCapPath(m.group(1));

        m = Pattern
                .compile("name=\"captcha-time\"[^>]*?value=\"([^\"]*)\"")
                .matcher(formText);
        if (!m.find())
            throw new NotReportException("cap_time не найден");
        loginForm.setCapTime(m.group(1));
        m = Pattern
                .compile("name=\"captcha-sig\"[^>]*?value=\"([^\"]*)\"")
                .matcher(formText);
        if (!m.find())
            throw new NotReportException("cap_sig не найден");
        loginForm.setCapSig(m.group(1));

        m = Pattern
                .compile("name=\"s\"[^>]*?value=\"([^\"]*)\"")
                .matcher(formText);
        if (m.find())
            loginForm.setSession(m.group(1));

        return loginForm;
    }
}
