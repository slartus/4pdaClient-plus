package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import org.apache.http.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.softeg.slartus.forpdaapi.classes.LoginForm;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.forpdacommon.PatternExtensions;

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
                                    Boolean privacy, String capA, String capD, String capS, String session) throws Exception {
        LoginResult loginResult = new LoginResult();

        Map<String, String> additionalHeaders = new HashMap<String, String>();

        additionalHeaders.put("UserName", login);
        additionalHeaders.put("PassWord", password);
        additionalHeaders.put("CookieDate", "1");
        additionalHeaders.put("Privacy", privacy ? "1" : "0");
        additionalHeaders.put("act", "Login");
        additionalHeaders.put("CODE", "01");
        additionalHeaders.put("referer", "http://4pda.ru/forum/index.php?act=UserCP&CODE=24");
        additionalHeaders.put("s", session);
        additionalHeaders.put("cap_a", capA);
        additionalHeaders.put("cap_d", capD);
        additionalHeaders.put("cap_s", capS);




        String res = httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

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
                checkPattern = Pattern.compile("\t<div class=\"formsubtitle\">Обнаружены следующие ошибки:</div>\n" +
                        "\t<div class=\"tablepad\"><span class=\"postcolor\">(.*?)</span></div>");
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

        Document doc = Jsoup.parse(page);
        org.jsoup.nodes.Element element = doc.select("#header+div>div>ul").first();

        if (element != null) {
            doc.select("div.photo").append("<div class=\"img "+avType+"\" style=\"background-image: url("+doc.select("div.photo>img").first().absUrl("src")+");\"></div>");
            doc.select("div.photo>img").first().remove();

            profile.setHtmlBody("<div class=\"user-profile-list\">"+element.html()+"</div>");

            org.jsoup.nodes.Element userNickElement = element.select("div.user-box > h1").first();
            if (userNickElement != null)
                profile.setNick(userNickElement.text());

            
        }
        return profile;
    }

    public static String getUserNick(IHttpClient httpClient, CharSequence userID) throws IOException {
        return Jsoup.parse(httpClient.performGet("http://4pda.ru/forum/index.php?showuser=" + userID)).select("div.user-box > h1").first().text();
    }

    public static LoginForm getLoginForm(IHttpClient httpClient) throws IOException {
        String page = httpClient.performGet("http://4pda.ru/forum/index.php?act=login&CODE=01");

        Matcher m = Pattern
                .compile("<form[^>]*?action=\"http://4pda.ru/forum/index.php\"[^>]*?>([\\s\\S]*?)</form>")
                .matcher(page);
        if (!m.find())
            throw new NotReportException("Форма логина не найдена");
        String formText = m.group(1);
        m = Pattern
                .compile("<img[^>]*?src=\"([^\"]*?turing.gerkon.eu/captcha[^\"]*)\"")
                .matcher(formText);
        if(!m.find())
            throw new NotReportException("Капча не найдена");

        LoginForm loginForm=new LoginForm();
        loginForm.setCapPath(m.group(1));

        m = Pattern
                .compile("name=\"cap_d\"[^>]*?value=\"([^\"]*)\"")
                .matcher(formText);
        if(!m.find())
            throw new NotReportException("cap_d не найден");
        loginForm.setCapD(m.group(1));
        m = Pattern
                .compile("name=\"cap_s\"[^>]*?value=\"([^\"]*)\"")
                .matcher(formText);
        if(!m.find())
            throw new NotReportException("cap_s не найден");
        loginForm.setCapS(m.group(1));

        m = Pattern
                .compile("name=\"s\"[^>]*?value=\"([^\"]*)\"")
                .matcher(formText);
        if(m.find())
            loginForm.setSession(m.group(1));

        return loginForm;
    }
}
