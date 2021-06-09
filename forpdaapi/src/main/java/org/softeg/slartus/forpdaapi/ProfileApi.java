package org.softeg.slartus.forpdaapi;

import androidx.core.util.Pair;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.softeg.slartus.forpdaapi.classes.LoginFormData;
import org.softeg.slartus.forpdaapi.common.ParseFunctions;
import org.softeg.slartus.forpdacommon.NotReportException;
import org.softeg.slartus.hosthelper.HostHelper;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.slartus.http.AppResponse;
import ru.slartus.http.Http;

/*
 * Created by slinkin on 07.02.14.
 */
public class ProfileApi {
    /**
     * Проверка логина на странице
     */
    private static void checkLogin(String pageBody, LoginResult loginResult) {
        Matcher kMatcher = Pattern
                .compile("action=logout[^\\\"]*[;&]k=([^&\\\"]*)", Pattern.CASE_INSENSITIVE)
                .matcher(pageBody);

        Matcher userPattern = Pattern
                .compile("\"[^\"]*showuser=(\\d+)[^>]*>Профиль<\\/a>|<div class=\"user_home\">.*?<a href=\"[^\"]*showuser=(\\d+)\"",
                        Pattern.CASE_INSENSITIVE).matcher(pageBody);
        if (kMatcher.find()) {
            loginResult.setK(kMatcher.group(1));
        }
        if (userPattern.find()) {
            loginResult.setUserId(userPattern.group(1) != null ? userPattern.group(1) : userPattern.group(2));

            loginResult.setSuccess(true);

            String[] avatarPatterns = {"(?:'|\")([^'\"]*4pda.(?:to|ru)/*?forum/*?uploads/*?av-[^?'\"]*)",
                    "(?:'|\")([^'\"]*4pda.(?:to|ru)/*?forum/*?style_avatars/[^?'\"]*)"};
            for (String avatarPattern : avatarPatterns) {
                Matcher m = Pattern.compile(avatarPattern, Pattern.CASE_INSENSITIVE).matcher(pageBody);
                if (m.find()) {
                    loginResult.setUserAvatarUrl(m.group(1));
                    break;
                }
            }
        }
    }

    public static LoginResult login(String login, String password,
                                    Boolean privacy, String capVal, String capTime, String capSig) throws Exception {
        LoginResult loginResult = new LoginResult();

        Map<String, String> additionalHeaders = new HashMap<>();

        additionalHeaders.put("login", login);
        additionalHeaders.put("password", password);
        //additionalHeaders.put("CookieDate", "1");
        if (privacy)
            additionalHeaders.put("hidden", "1");
        additionalHeaders.put("act", "auth");
        //additionalHeaders.put("CODE", "01");
        additionalHeaders.put("referer", "https://" + HostHelper.getHost() + "/forum/index.php");
        //additionalHeaders.put("s", session);
        additionalHeaders.put("captcha", capVal);
        additionalHeaders.put("captcha-time", capTime);
        additionalHeaders.put("captcha-sig", capSig);
        additionalHeaders.put("return", "https://" + HostHelper.getHost() + "/forum/index.php");

        ArrayList<Pair<String, String>> listParams = new ArrayList<>();
        for (String key : additionalHeaders.keySet()) {
            listParams.add(new Pair<>(key, additionalHeaders.get(key)));
        }
        AppResponse response = Http.Companion.getInstance()
                .performPost("https://" + HostHelper.getHost() + "/forum/index.php?act=auth&return=" + "https://" + HostHelper.getHost() + "/forum/index.php", listParams);
        String res = response.getResponseBody();

        if (TextUtils.isEmpty(res)) {
            loginResult.setLoginError("Сервер вернул пустую страницу");
            return loginResult;
        }

        String errorMsg = null;

        for (HttpCookie cookie : Http.Companion.getInstance().getCookieStore().getCookies()) {
            if (!"deleted".equals(cookie.getValue()) && "member_id".equals(cookie.getName())) {
                // id пользователя. если он есть - логин успешный

                loginResult.setUserId(cookie.getValue());
                loginResult.setUserLogin(cookie.getValue());
                loginResult.setSuccess(true);
                break;
            } else if ("deleted".equals(cookie.getValue())) {
                errorMsg = "Неправильный логин, пароль или капча!";
            } else //noinspection StatementWithEmptyBody
                if ("pass_hash".equals(cookie.getName())) {
                    // хэш пароля
                } else //noinspection StatementWithEmptyBody
                    if ("session_id".equals(cookie.getName())) {
                        // id сессии
                    }
        }

        if (errorMsg != null) {
            loginResult.setLoginError(errorMsg);
            return loginResult;
        }

        checkLogin(res, loginResult);
        loginResult.setUserLogin(login);

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
     * @param k идентификатор, полученный при логине
     */
    public static String logout(IHttpClient httpClient, String k) throws Throwable {
        return httpClient.performGet("https://" + HostHelper.getHost() + "/forum/index.php?act=Login&CODE=03&k=" + k).getResponseBody();
    }

    public static Profile getProfile(IHttpClient httpClient, CharSequence userID, String avType) throws IOException {
        Profile profile = new Profile();
        profile.setId(userID);
        String page = httpClient.performGet("https://" + HostHelper.getHost() + "/forum/index.php?showuser=" + userID).getResponseBody();
        page = ParseFunctions.decodeEmails(page);
        Matcher matcher = Pattern.compile("<form action=\"[^\"]*?" + HostHelper.getHostPattern() + "/forum/index\\.php\\?showuser[^>]*>[\\s\\S]*?<ul[^>]*>([\\s\\S]*)</ul>[\\s\\S]*?</form>").matcher(page);
        if (matcher.find()) {
            page = matcher.group(1).replaceFirst("<div class=\"photo\">[^<]*<img src=\"([^\"]*)\"[^<]*</div>",
                    "<div class=\"photo\"><div class=\"img " + avType + "\" style=\"background-image: url($1);\"></div></div>");
            matcher = Pattern.compile("<div class=\"user-box\">[\\s\\S]*?<h1>([\\s\\S]*?)</h1>").matcher(page);
            if (matcher.find())
                profile.setNick(Html.fromHtml(matcher.group(1)));
            page = page.replaceAll("<div class=\"profile-edit-links\">", "<div class=\"profile-edit-links\" style=\"display:none;\">");
            profile.setHtmlBody("<div class=\"user-profile-list\">" + page + "</div>");
        }
        return profile;
    }

    public static String getUserNick(IHttpClient httpClient, CharSequence userID) throws IOException {
        return Jsoup.parse(httpClient.performGet("https://" + HostHelper.getHost() + "/forum/index.php?showuser=" + userID).getResponseBody()).select("div.user-box > h1").first().text();
    }

    private static String RequestUrl(OkHttpClient client, String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static LoginFormData getLoginForm(Context context) throws IOException {
        OkHttpClient client = Http.newClientBuiler(context).build();

        String prevPage = RequestUrl(client, "https://" + HostHelper.getHost() + "/forum/index.php?act=auth");
        Matcher m = Pattern.compile("act=auth[^\"]*[;&]k=([^&\"]*)", Pattern.CASE_INSENSITIVE).matcher(prevPage);
        String k = UUID.randomUUID().toString();
        if (m.find())
            k = m.group(1);
        String page = RequestUrl(client, "https://" + HostHelper.getHost() + "/forum/index.php?act=auth&k=" + k);
        return parseLoginForm(page);
    }

    public static LoginFormData parseLoginForm(String page) throws IOException {
        Document doc = Jsoup.parse(page);
        Element formElement = doc.selectFirst("form[id=auth]");
        if (formElement == null)
            throw new NotReportException("Форма логина не найдена");

        Element captchaDiv = formElement.selectFirst(".captcha");
        if (captchaDiv == null)
            throw new NotReportException("Контейнер капчи не найден");

        Element el = captchaDiv.selectFirst("img");
        if (el == null)
            throw new NotReportException("Капча не найдена");

        LoginFormData loginFormData = new LoginFormData();
        loginFormData.setCapPath(el.attr("src"));

        el = captchaDiv.selectFirst("input[name=captcha-time]");
        if (el == null)
            throw new NotReportException("cap_time не найден");
        loginFormData.setCapTime(el.attr("value"));

        el = captchaDiv.selectFirst("input[name=captcha-sig]");
        if (el == null)
            throw new NotReportException("cap_sig не найден");
        loginFormData.setCapSig(el.attr("value"));

        el = captchaDiv.selectFirst("[name=s]");
        if (el != null)
            loginFormData.setSession(el.attr("value"));


        return loginFormData;
    }
}
