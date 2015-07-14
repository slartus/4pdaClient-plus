package org.softeg.slartus.forpdaapi.post;

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdaapi.ProgressState;
import org.softeg.slartus.forpdacommon.NotReportException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:04
 */
public class PostApi {

    public static final String NEW_POST_ID = "0";

    /**
     * Удаляет пост
     *
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
     * Возвращает страницу с редактируемым постом
     *
     * @param postId NEW_POST_ID, для создания нового поста
     */
    public static String getEditPage(IHttpClient httpClient, String forumId, String topicId, String postId, String authKey) throws IOException {
        String res;
        if (postId.equals(NEW_POST_ID))
            res = httpClient.performGet("http://4pda.ru/forum/index.php?act=post&do=reply_post&f=" + forumId
                    + "&t=" + topicId);
        else
            res = httpClient.performGet("http://4pda.ru/forum/index.php?act=post&do=edit_post&f=" + forumId
                    + "&t=" + topicId
                    + "&p=" + postId
                    + "&auth_key=" + authKey);


        return res;

    }

    public static String getAttachPage(IHttpClient httpClient, String postId) throws IOException {
        if (postId.equals(NEW_POST_ID))
            return null;
        return httpClient.performGet("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_show&attach_rel_id=" + postId);
    }

    /**
     * Проверка страницы редактирования поста на ошибки (пост удалён ранее или нет прав на редактирование и т.д. )
     */
    public static String checkEditPage(String editPage) {
        String startFlag = "<textarea name=\"post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
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
     * @param enablesig     включить подпись
     * @param enableEmo     включить смайлы
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @throws IOException
     */
    public static String applyEdit(IHttpClient httpClient, String forumId, String themeId, String authKey, String postId,
                                   Boolean enablesig,
                                   Boolean enableEmo, String postText, String addedFileList, String post_edit_reason) throws IOException {

        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put("act", "post");
        additionalHeaders.put("s", "");
        additionalHeaders.put("f", forumId);
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("removeattachid", "0");
        additionalHeaders.put("MAX_FILE_SIZE", "0");
        additionalHeaders.put("CODE", "09");
        additionalHeaders.put("t", themeId);
        additionalHeaders.put("p", postId);
        additionalHeaders.put("view", "findpost");
        //if(!TextUtils.isEmpty(post_edit_reason))
        additionalHeaders.put("post_edit_reason", post_edit_reason);
        additionalHeaders.put("file-list", addedFileList);

        additionalHeaders.put("Post", postText);
        if (enablesig)
            additionalHeaders.put("enablesig", "yes");
        if (enableEmo)
            additionalHeaders.put("enableemo", "yes");


        return httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);
    }

    /**
     * Быстрый ответ
     *
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @throws IOException
     */
    public static String reply(IHttpClient httpClient, String forumId, String topicId, String authKey, String attachPostKey, String post,
                               Boolean enablesig, Boolean enableemo, String addedFileList, boolean quick) throws IOException {

        Map<String, String> additionalHeaders = new HashMap<>();
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

        return httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);


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
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @throws Exception
     */
    public static String attachFile(IHttpClient httpClient, String forumId, String topicId, String authKey,
                                    String attachPostKey, String postId, Boolean enablesig, Boolean enableEmo,
                                    String post, String filePath, String addedFileList,
                                    ProgressState progress, String post_edit_reason) throws Exception {
        Map<String, String> additionalHeaders = new HashMap<>();
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


        if (!postId.equals(NEW_POST_ID)) {
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

        if (postId.equals(NEW_POST_ID))
            return httpClient.uploadFile("http://4pda.ru/forum/index.php", filePath, additionalHeaders,
                    progress);

        httpClient.uploadFile("http://4pda.ru/forum/index.php", filePath, additionalHeaders, progress);
        progress.update("Файл загружен, получение страницы", 100);
        return getEditPage(httpClient, forumId, topicId, postId, authKey);
    }

    /**
     * Деаттачим файл
     *
     * @throws Exception
     */
    public static String deleteAttachedFile(IHttpClient httpClient, String forumId, String themeId, String authKey,
                                            String postId, Boolean enablesig, Boolean enableemo,
                                            String post, String attachToDeleteId, String fileList,
                                            String post_edit_reason) throws Exception {

        Map<String, String> additionalHeaders = new HashMap<>();
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

        if (!postId.equals(NEW_POST_ID)) {
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

        if (postId.equals(NEW_POST_ID))
            return httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);
        httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);
        return getEditPage(httpClient, forumId, themeId, postId, authKey);
    }

    /**
     * Жалоба на пост
     *
     * @return Ошибка или пустая строка в случае успеха
     * @throws IOException
     */
    public static String claim(IHttpClient httpClient, String topicId, String postId, String message) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<>();
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

    private static String getStatusMessage(String status) {
        switch (status) {
            case "no_items":
                return "Ни одного файла не загружено";
            case "uploading_file":
                return "Загрузка файла...";
            case "init_progress":
                return "Инициализация системы...";
            case "upload_ok":
                return "Файл успешно загружен и доступен в меню «Управление текущими файлами»";
            case "upload_failed":
                return "Неудачная загрузка. Необходимо проверить настройки и права доступа. Пожалуйста, сообщите об этом администрации.";
            case "upload_too_big":
                return "Неудачная загрузка. Файл имеет размер больше допустимого";
            case "invalid_mime_type":
                return "Неудачная загрузка. Вам запрещено загружать такой тип файлов";
            case "no_upload_dir":
                return "Неудачная загрузка. Директория загрузок файлов не доступна. Пожалуйста, сообщите об этом администрации.";
            case "no_upload_dir_perms":
                return "Неудачная загрузка. Невозможно произвести запись файла в директорию загрузок. Пожалуйста, сообщите об этом администрации.";
            case "upload_no_file":
                return "Вы не выбрали файл для загрузки";
            case "upload_banned_file":
                return "Неудачная загрузка. Вам запрещено загружать этот файл";
            case "ready":
                return "Система готова для загрузки файлов";
            case "attach_remove":
                return "Удалить файл";
            case "attach_insert":
                return "Вставить файл в текстовый редактор";
            case "remove_warn":
                return "Продолжить удаление файла?";
            case "attach_removed":
                return "Файл успешно удален";
            case "attach_removal":
                return "Удаление файла...";
            default:
                return status;
        }
    }

    private static void checkAttachError(String page) throws NotReportException {
        Matcher m = Pattern
                .compile("pipsatt.status_msg = '([^']*)';\\s*pipsatt.status_is_error = parseInt\\('(\\d+)'\\);", Pattern.CASE_INSENSITIVE)
                .matcher(page);
        if (m.find()) {
            if ("1".equals(m.group(2)))
                throw new NotReportException(getStatusMessage(m.group(1)));
        }
    }

    public static void deleteAttachedFile(IHttpClient httpClient,
                                          String postId,
                                          String attachId) throws Exception {
        String res = httpClient
                .performGet(String.format("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_remove&attach_rel_id=%s&attach_id=%s", postId,
                        attachId));
        checkAttachError(res);
    }

    public static EditAttach attachFile(IHttpClient httpClient,
                                        String postId,
                                        String newFilePath,
                                        ProgressState progress) throws Exception {


        String res = httpClient.uploadFile("http://4pda.ru/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=" + postId,
                newFilePath, new HashMap<String, String>(),
                progress);
        Matcher m = Pattern
                .compile("add_current_item\\(\\s*'(\\d+)',\\s*'([^']*)',\\s*'([^']*)',\\s*'([^']*)'\\s*\\);", Pattern.CASE_INSENSITIVE)
                .matcher(res);
        if (m.find()) {
            return new EditAttach(m.group(1), m.group(2), m.group(3), m.group(4));
        }
        checkAttachError(res);
        return null;

    }

    public static String sendPost(IHttpClient httpClient, EditPostParams params,
                                  String postBody, String postEditReason,
                                  Boolean enablesig, Boolean enableemo) throws IOException {

        List<NameValuePair> nameValuePairs=params.getListParams();
        nameValuePairs.add(new BasicNameValuePair("Post", postBody));
        if (postEditReason != null)
            nameValuePairs.add(new BasicNameValuePair("post_edit_reason", postEditReason));

        if (enableemo)
            nameValuePairs.add(new BasicNameValuePair("enableemo", "yes"));

        if (enablesig)
            nameValuePairs.add(new BasicNameValuePair("enablesig", "yes"));


        return httpClient.performPost("http://4pda.ru/forum/index.php", nameValuePairs);
    }

    public static EditPost editPost(IHttpClient httpClient, String forumId, String topicId, String postId, String authKey) throws IOException {
        String editPage = getEditPage(httpClient, forumId, topicId, postId, authKey);
        Document doc = Jsoup.parse(editPage);
        EditPost editPost = new EditPost();
        Element postFormElement = doc.select("#postingform").first();
        if (postFormElement != null) {
            for (Element el : postFormElement.select("input[type=hidden]")) {
                editPost.getParams().put(el.attr("name"), el.attr("value"));
            }
            // название опроса
            Element element = postFormElement.select("input[name=poll_question]").first();
            if (element != null)
                editPost.getParams().put("poll_question", element.attr("value"));

            parseInterviewParams(postFormElement.html(), editPost);

            // текст поста
            element = postFormElement.select("textarea[name=Post]").first();
            if (element != null)
                editPost.setBody(element.text());

            // Причина редактирования
            element = postFormElement.select("input[name=post_edit_reason]").first();
            if (element != null)
                editPost.setPostEditReason(element.attr("value"));

            // Включить смайлы?
            element = postFormElement.select("input[name=enableemo]").first();
            if (element != null)
                editPost.setEnableEmo("checked".equals(element.attr("checked")));

            // Включить подпись?
            element = postFormElement.select("input[name=enablesig]").first();
            if (element != null)
                editPost.setEnableSign("checked".equals(element.attr("checked")));

            // управление текущими файлами
            editPage = getAttachPage(httpClient, postId);
            if (editPage != null) {

                Matcher m = Pattern
                        .compile("add_current_item\\( '(\\d+)', '([^']*)', '([^']*)', '([^']*)' \\)", Pattern.CASE_INSENSITIVE)
                        .matcher(editPage);
                while (m.find()) {
                    editPost.addAttach(new EditAttach(m.group(1), m.group(2), m.group(3), m.group(4)));
                }
            }
        }


        if (editPost.getBody() == null) {
            Pattern pattern = Pattern.compile("<h4>Причина:</h4>\n" +
                    "\\s*\n" +
                    "\\s*<p>(.*)</p>", Pattern.MULTILINE);
            assert editPage != null;
            Matcher m = pattern.matcher(editPage);
            if (m.find()) {
                editPost.setError(m.group(1));

            } else {
                editPost.setError("Неизвестная причина");
            }

        }
        return editPost;
    }

    /*
    Парсим опрос
     */
    private static void parseInterviewParams(String editPage, EditPost editPost) {
        Matcher m = Pattern.compile("poll_questions\\s*=\\s*\\{(.*)?'\\}", Pattern.CASE_INSENSITIVE)
                .matcher(editPage);
        if (m.find()) {
            String s = m.group(1) + "'";
            m = Pattern.compile("\\s*(\\d+)\\s*:\\s*'([^']*)'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
                    .matcher(s);

            String choicesText = getChoicesText(editPage);
            while (m.find()) {
                String questionNum = m.group(1);
                editPost.getParams().put("question[" + questionNum + "]", Html.fromHtml(m.group(2)).toString());

                Matcher choiceMatcher = Pattern.compile("\\s*'(" + questionNum + "_\\d+)'\\s*:\\s*'([^']*)'",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
                        .matcher(choicesText);
                while (choiceMatcher.find()) {
                    editPost.getParams().put("choice[" + choiceMatcher.group(1) + "]", Html.fromHtml(choiceMatcher.group(2)).toString());
                }


                Matcher multiMatcher = Pattern.compile("\\s*" + questionNum + "\\s*:\\s*'([^']*)'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
                        .matcher(s);
                while (multiMatcher.find()) {
                    if ("1".equals(multiMatcher.group(1))) {
                        editPost.getParams().put("multi[" + questionNum + "]", "1");
                    }
                }
            }
        }
    }

    @NonNull
    private static String getChoicesText(String editPage) {
        String choicesText = "";
        Matcher choicesMatcher = Pattern.compile("poll_choices\\s*=\\s*\\{(.*)?'\\}", Pattern.CASE_INSENSITIVE)
                .matcher(editPage);
        if (choicesMatcher.find()) {
            choicesText = choicesMatcher.group(1) + "'";
        }
        return choicesText;
    }

    @NonNull
    private static String getMultiText(String editPage) {
        Matcher m = Pattern.compile("poll_multi\\s*=\\s*\\{(.*)?'\\}", Pattern.CASE_INSENSITIVE)
                .matcher(editPage);
        if (m.find()) {
            return m.group(1) + "'";

        }
        return "";
    }
}
