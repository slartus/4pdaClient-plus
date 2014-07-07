package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:04
 */
public class PostApi {

    /**
     * Удаляет пост
     *
     * @param httpClient
     * @param forumId
     * @param topicId
     * @param postId
     * @param authKey
     * @return
     * @throws IOException
     */
    public static boolean delete(IHttpClient httpClient, String forumId, String topicId, String postId, CharSequence authKey) throws IOException {
        httpClient.performGet("http://4pda.ru/forum/index.php?act=Mod&CODE=04&f=" + forumId
                + "&t=" + topicId
                + "&p=" + postId
                + "&auth_key=" + authKey);
        return true;// !TODO: проверка ответа
    }

    /**
     * Возвращает страницу с новым постом
     *
     * @param httpClient
     * @param forumId
     * @param authKey
     * @return
     * @throws Exception
     */
    public static String getNewPostPage(IHttpClient httpClient, String forumId, String topicId, String authKey) throws IOException {
        return getEditPage(httpClient, forumId, topicId, "-1", authKey);
    }

    /**
     * Возвращает страницу с редактируемым постом
     *
     * @param httpClient
     * @param forumId
     * @param postId     -1, для создания нового поста
     * @param authKey
     * @return
     * @throws Exception
     */
    public static String getEditPage(IHttpClient httpClient, String forumId, String topicId, String postId, String authKey) throws IOException {
        String res;
        if (postId.equals("-1"))
            res = httpClient.performGet("http://4pda.ru/forum/index.php?act=post&do=reply_post&f=" + forumId
                    + "&t=" + topicId);
        else
            res = httpClient.performGet("http://4pda.ru/forum/index.php?act=post&do=edit_post&f=" + forumId
                    + "&t=" + topicId
                    + "&p=" + postId
                    + "&auth_key=" + authKey);


        return res;

    }

    /**
     * Проверка страницы редактирования поста на ошибки (пост удалён ранее или нет прав на редактирование и т.д. )
     *
     * @param editPage
     * @return
     */
    public static String checkEditPage(String editPage) {
        String startFlag = "<textarea name=\"Post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
        int startIndex = editPage.indexOf(startFlag);
        if (startIndex == -1) {
            Pattern pattern = Pattern.compile("<h4>Причина:</h4>\n" +
                    "\\s*\n" +
                    "\\s*<p>(.*)</p>", Pattern.MULTILINE);
            Matcher m = pattern.matcher(editPage);
            if (m.find()) {
                return m.group(1);
            }
            return "Неизвестная причина";
        }
        return "";
    }

    /**
     * Отправка изменений в посте
     *
     * @param httpClient
     * @param forumId
     * @param themeId
     * @param authKey
     * @param postId
     * @param enablesig     включить подпись
     * @param enableEmo     включить смайлы
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @throws IOException
     */
    public static void applyEdit(IHttpClient httpClient, String forumId, String themeId, String authKey, String postId,
                                 Boolean enablesig,
                                 Boolean enableEmo, String postText, String addedFileList, String post_edit_reason) throws IOException {

        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "Post");
        additionalHeaders.put("s", "");
        additionalHeaders.put("f", forumId);
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("removeattachid", "0");
        additionalHeaders.put("MAX_FILE_SIZE", "0");
        additionalHeaders.put("CODE", "09");
        additionalHeaders.put("t", themeId);
        additionalHeaders.put("p", postId);
        //if(!TextUtils.isEmpty(post_edit_reason))
        additionalHeaders.put("post_edit_reason", post_edit_reason);
        additionalHeaders.put("file-list", addedFileList);

        additionalHeaders.put("Post", postText);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableEmo)
            additionalHeaders.put("enableemo", "yes");


        httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

    }

    /**
     * Быстрый ответ
     *
     * @param httpClient
     * @param forumId
     * @param authKey
     * @param attachPostKey
     * @param post
     * @param enablesig
     * @param enableemo
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @return
     * @throws IOException
     */
    public static String reply(IHttpClient httpClient, String forumId, String topicId, String authKey, String attachPostKey, String post,
                               Boolean enablesig, Boolean enableemo, String addedFileList, boolean quick) throws IOException {

        Map<String, String> additionalHeaders = new HashMap<String, String>();
        if (!quick) {
            additionalHeaders.put("st", "0");
            additionalHeaders.put("removeattachid", "0");
            additionalHeaders.put("MAX_FILE_SIZE", "0");
            additionalHeaders.put("parent_id", "0");
            additionalHeaders.put("ed-0_wysiwyg_used", "0");
            additionalHeaders.put("editor_ids[]", "ed-0");
            additionalHeaders.put("iconid", "0");
            additionalHeaders.put("_upload_single_file", "1");
            additionalHeaders.put("file-list", addedFileList);

        } else {
            additionalHeaders.put("fast_reply_used", "1");
        }


        return quickReply(httpClient, additionalHeaders, forumId, topicId, authKey, attachPostKey, post,
                enablesig, enableemo);

    }

    /**
     * Быстрый ответ
     *
     * @param httpClient
     * @param forumId
     * @param authKey
     * @param attachPostKey
     * @param post
     * @param enablesig
     * @param enableemo
     * @return
     * @throws IOException
     */
    public static String quickReply(IHttpClient httpClient, Map<String, String> additionalHeaders, String forumId,
                                    String topicId, String authKey, String attachPostKey, String post,
                                    Boolean enablesig, Boolean enableemo) throws IOException {

        additionalHeaders.put("act", "Post");
        additionalHeaders.put("CODE", "03");
        additionalHeaders.put("f", forumId);
        additionalHeaders.put("t", topicId);


        additionalHeaders.put("auth_key", authKey);
        if (!TextUtils.isEmpty(attachPostKey))
            additionalHeaders.put("attach_post_key", attachPostKey);
        additionalHeaders.put("Post", post);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableemo)
            additionalHeaders.put("enableemo", "yes");


        // additionalHeaders.put("referer", "http://4pda.ru/forum/index.php?act=Post&CODE=03&f=" + forumId + "&t=" + topicId + "&st=20&auth_key=" + authKey + "&fast_reply_used=1");

        String res = httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

        return res;


    }

    public static String checkPostErrors(String page) {
        Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                "\n" +
                "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
        Matcher m = checkPattern.matcher(page);
        if (m.find()) {
            return m.group(1);
        }

        checkPattern = Pattern.compile("<div class=\".*?\">(<b>)?ОБНАРУЖЕНЫ СЛЕДУЮЩИЕ ОШИБКИ(</b>)?</div>\n" +
                "\\s*<div class=\".*?\">(.*?)</div>", Pattern.MULTILINE);
        m = checkPattern.matcher(page);
        if (m.find()) {
            return Html.fromHtml(m.group(3)).toString();
        }
        return null;
    }


    /**
     * Аттачим файл к посту.
     *
     * @param httpClient
     * @param forumId
     * @param topicId
     * @param authKey
     * @param attachPostKey
     * @param postId
     * @param enablesig
     * @param enableEmo
     * @param post
     * @param filePath
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @return
     * @throws Exception
     */
    public static String attachFile(IHttpClient httpClient, String forumId, String topicId, String authKey,
                                    String attachPostKey, String postId, Boolean enablesig, Boolean enableEmo,
                                    String post, String filePath, String addedFileList,
                                    ProgressState progress, String post_edit_reason) throws Exception {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("st", "0");


        additionalHeaders.put("f", forumId);
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("removeattachid", "0");
        additionalHeaders.put("MAX_FILE_SIZE", "0");

        additionalHeaders.put("t", topicId);

        if (attachPostKey != null)
            additionalHeaders.put("attach_post_key", attachPostKey);

        additionalHeaders.put("parent_id", "0");
        additionalHeaders.put("ed-0_wysiwyg_used", "0");
        additionalHeaders.put("editor_ids[]", "ed-0");
        additionalHeaders.put("_upload_single_file", "1");
        additionalHeaders.put("upload_process", "Закачать");
        additionalHeaders.put("file-list", addedFileList);
        additionalHeaders.put("post_edit_reason", post_edit_reason);


        if (!postId.equals("-1")) {
            additionalHeaders.put("p", postId);
            additionalHeaders.put("act", "attach");
            additionalHeaders.put("attach_rel_id", postId);
            additionalHeaders.put("code", "attach_upload_process");
        } else {
            additionalHeaders.put("act", "Post");

        }
        additionalHeaders.put("CODE", "03");
        additionalHeaders.put("Post", post);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableEmo)
            additionalHeaders.put("enableEmo", "yes");
        additionalHeaders.put("iconid", "0");

        if (postId.equals("-1"))
            return httpClient.uploadFile("http://4pda.ru/forum/index.php", filePath, additionalHeaders,
                    progress);

        httpClient.uploadFile("http://4pda.ru/forum/index.php", filePath, additionalHeaders, progress);
        progress.update("Файл загружен, получение страницы", 100);
        return getEditPage(httpClient, forumId, topicId, postId, authKey);
    }

    /**
     * Деаттачим файл
     *
     * @param forumId
     * @param themeId
     * @param authKey
     * @param postId
     * @param enablesig
     * @param post
     * @param attachToDeleteId
     * @return
     * @throws Exception
     */
    public static String deleteAttachedFile(IHttpClient httpClient, String forumId, String themeId, String authKey,
                                            String postId, Boolean enablesig, Boolean enableemo,
                                            String post, String attachToDeleteId, String fileList,
                                            String post_edit_reason) throws Exception {

        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("st", "0");

        additionalHeaders.put("f", forumId);
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("removeattachid", "0");
        additionalHeaders.put("MAX_FILE_SIZE", "0");

        additionalHeaders.put("t", themeId);
        additionalHeaders.put("p", postId);
        additionalHeaders.put("ed-0_wysiwyg_used", "0");
        additionalHeaders.put("editor_ids[]", "ed-0");
        additionalHeaders.put("Post", post);
        additionalHeaders.put("_upload_single_file", "1");
        additionalHeaders.put("post_edit_reason", post_edit_reason);

        additionalHeaders.put("file-list", fileList);
        additionalHeaders.put("removeattach[" + attachToDeleteId + "]", "Удалить!");

        if (!postId.equals("-1")) {
            additionalHeaders.put("act", "attach");
            additionalHeaders.put("code", "attach_upload_remove");
            additionalHeaders.put("attach_rel_id", postId);
            additionalHeaders.put("attach_id", attachToDeleteId);
        } else {
            additionalHeaders.put("act", "Post");

        }
        additionalHeaders.put("CODE", "03");
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableemo)
            additionalHeaders.put("enableemo", "yes");

        if (postId.equals("-1"))
            return httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);
        httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);
        return getEditPage(httpClient, forumId, themeId, postId, authKey);
    }

    /**
     * Жалоба на пост
     *
     * @param httpClient
     * @param topicId
     * @param postId
     * @param message
     * @return Ошибка или пустая строка в случае успеха
     * @throws IOException
     */
    public static String claim(IHttpClient httpClient, String topicId, String postId, String message) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "report");
        additionalHeaders.put("send", "1");
        additionalHeaders.put("t", topicId);
        additionalHeaders.put("p", postId);
        additionalHeaders.put("message", message);

        String res = httpClient.performPost("http://4pda.ru/forum/index.php?act=report&amp;send=1&amp;t=" + topicId + "&amp;p=" + postId, additionalHeaders);

        Pattern p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE);
        Matcher m = p.matcher(res);
        if (m.find()) {

            return "Ошибка отправки жалобы: " + m.group(1);
        }
        return null;
    }
}
