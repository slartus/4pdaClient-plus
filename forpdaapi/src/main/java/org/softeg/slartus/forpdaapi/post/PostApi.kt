package org.softeg.slartus.forpdaapi.post

import androidx.core.util.Pair
import android.text.Html
import android.text.TextUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.softeg.slartus.forpdaapi.IHttpClient
import org.softeg.slartus.forpdaapi.ProgressState
import org.softeg.slartus.forpdacommon.BasicNameValuePair
import org.softeg.slartus.forpdacommon.NotReportException
import org.softeg.slartus.hosthelper.HostHelper
import ru.slartus.http.AppResponse
import ru.slartus.http.Http
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:04
 */
@Suppress("unused")
object PostApi {

    val NEW_POST_ID = "0"

    /**
     * Удаляет пост
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun delete(httpClient: IHttpClient, postId: String, authKey: CharSequence): Boolean {
        httpClient.performGet("https://${HostHelper.host}/forum/index.php?act=zmod&auth_key=$authKey&code=postchoice&tact=delete&selectedpids=$postId")
        return true// !TODO: проверка ответа
    }

    /**
     * Возвращает страницу с редактируемым постом
     *
     * @param postId NEW_POST_ID, для создания нового поста
     */
    @Throws(IOException::class)
    fun getEditPage(httpClient: IHttpClient, forumId: String, topicId: String, postId: String, authKey: String): String {
        val res: AppResponse
        if (postId == NEW_POST_ID)
            res = httpClient.performGet("https://${HostHelper.host}/forum/index.php?act=post&do=reply_post&f=" + forumId
                    + "&t=" + topicId)
        else
            res = httpClient.performGet("https://${HostHelper.host}/forum/index.php?act=post&do=edit_post&f=" + forumId
                    + "&t=" + topicId
                    + "&p=" + postId
                    + "&auth_key=" + authKey)


        return res.responseBody

    }

    @Throws(IOException::class)
    fun getAttachPage(httpClient: IHttpClient, postId: String): String? {
        return if (postId == NEW_POST_ID) null else httpClient.performGet("https://${HostHelper.host}/forum/index.php?&act=attach&code=attach_upload_show&attach_rel_id=$postId").responseBody
    }

    /**
     * Проверка страницы редактирования поста на ошибки (пост удалён ранее или нет прав на редактирование и т.д. )
     */
    fun checkEditPage(editPage: String): String {
        val startFlag = "<textarea name=\"post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">"
        val startIndex = editPage.indexOf(startFlag)
        if (startIndex == -1) {
            val pattern = Pattern.compile("<h4>Причина:</h4>\n" +
                    "\\s*\n" +
                    "\\s*<p>(.*)</p>", Pattern.MULTILINE)
            val m = pattern.matcher(editPage)
            return if (m.find()) {
                m.group(1)
            } else "Неизвестная причина"
        }
        return ""
    }

    /**
     * Отправка изменений в посте
     *
     * @param enablesig     включить подпись
     * @param enableEmo     включить смайлы
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @throws IOException
     */
    @Throws(IOException::class)
    fun applyEdit(httpClient: IHttpClient, forumId: String, themeId: String, authKey: String, postId: String,
                  enablesig: Boolean,
                  enableEmo: Boolean?, postText: String, addedFileList: String?, post_edit_reason: String): String {

        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["act"] = "post"
        additionalHeaders["s"] = ""
        additionalHeaders["f"] = forumId
        additionalHeaders["auth_key"] = authKey
        additionalHeaders["removeattachid"] = "0"
        additionalHeaders["MAX_FILE_SIZE"] = "0"
        additionalHeaders["CODE"] = "09"
        additionalHeaders["t"] = themeId
        additionalHeaders["p"] = postId
        additionalHeaders["view"] = "findpost"
        //if(!TextUtils.isEmpty(post_edit_reason))
        additionalHeaders["post_edit_reason"] = post_edit_reason
        additionalHeaders["file-list"] = addedFileList ?: ""

        additionalHeaders["Post"] = postText
        if (enablesig)
            additionalHeaders["enablesig"] = "yes"
        if (enableEmo!!)
            additionalHeaders["enableemo"] = "yes"


        return httpClient.performPost("https://${HostHelper.host}/forum/index.php", additionalHeaders).responseBody
    }

    /**
     * Быстрый ответ
     *
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @throws IOException
     */
    @Throws(IOException::class)
    fun reply(forumId: String, topicId: String, authKey: String, attachPostKey: String?, post: String, enablesig: Boolean?,
              enableemo: Boolean?, addedFileList: String?, quick: Boolean): AppResponse {

        val additionalHeaders = HashMap<String, String>()
        if (!quick) {
            additionalHeaders["st"] = "0"
            additionalHeaders["removeattachid"] = "0"
            additionalHeaders["MAX_FILE_SIZE"] = "0"
            additionalHeaders["parent_id"] = "0"
            additionalHeaders["ed-0_wysiwyg_used"] = "0"
            additionalHeaders["editor_ids[]"] = "ed-0"
            additionalHeaders["iconid"] = "0"
            additionalHeaders["_upload_single_file"] = "1"
            additionalHeaders["file-list"] = addedFileList ?: ""

        } else {
            additionalHeaders["fast_reply_used"] = "1"
        }


        return quickReply(additionalHeaders, forumId, topicId, authKey, attachPostKey, post,
                enablesig, enableemo)

    }

    /**
     * Быстрый ответ
     */
    @Throws(IOException::class)
    fun quickReply(additionalHeaders: MutableMap<String, String>, forumId: String,
                   topicId: String, authKey: String, attachPostKey: String?, post: String,
                   enablesig: Boolean?, enableemo: Boolean?): AppResponse {

        additionalHeaders["act"] = "Post"
        additionalHeaders["CODE"] = "03"
        additionalHeaders["f"] = forumId
        additionalHeaders["t"] = topicId


        additionalHeaders["auth_key"] = authKey
        if (!TextUtils.isEmpty(attachPostKey))
            additionalHeaders["attach_post_key"] = attachPostKey ?: ""
        additionalHeaders["Post"] = post
        if (enablesig!!)
            additionalHeaders["enablesig"] = "yes"
        if (enableemo!!)
            additionalHeaders["enableemo"] = "yes"


        // additionalHeaders.put("referer", "https://${HostHelper.host}/forum/index.php?act=Post&CODE=03&f=" + forumId + "&t=" + topicId + "&st=20&auth_key=" + authKey + "&fast_reply_used=1");

        val listParams = ArrayList<Pair<String, String>>()
        for (key in additionalHeaders.keys) {
            listParams.add(Pair(key, additionalHeaders[key]))
        }
        return Http.instance.performPost("https://${HostHelper.host}/forum/index.php", listParams)
    }

    fun checkPostErrors(page: String?): String? {
        var checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                "\n" +
                "\t\t<p>(.*?)</p>", Pattern.MULTILINE)
        var m = checkPattern.matcher(page?:"")
        if (m.find()) {
            return m.group(1)
        }

        checkPattern = Pattern.compile("<div class=\".*?\">(<b>)?ОБНАРУЖЕНЫ СЛЕДУЮЩИЕ ОШИБКИ(</b>)?</div>\n" + "\\s*<div class=\".*?\">(.*?)</div>", Pattern.MULTILINE)
        m = checkPattern.matcher(page?:"")
        return if (m.find()) {
            Html.fromHtml(m.group(3)).toString()
        } else null
    }


    /**
     * Аттачим файл к посту.
     *
     * @param addedFileList список айдишек уже загруженных файлов. Например, 0,1892529,1892530,1892533
     * @throws Exception
     */
    @Throws(Exception::class)
    fun attachFile(httpClient: IHttpClient, forumId: String, topicId: String, authKey: String,
                   attachPostKey: String?, postId: String, enablesig: Boolean?, enableEmo: Boolean?,
                   post: String, filePath: String, addedFileList: String,
                   progress: ProgressState, post_edit_reason: String): String {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["st"] = "0"


        additionalHeaders["f"] = forumId
        additionalHeaders["auth_key"] = authKey
        additionalHeaders["removeattachid"] = "0"
        additionalHeaders["MAX_FILE_SIZE"] = "0"

        additionalHeaders["t"] = topicId

        if (attachPostKey != null)
            additionalHeaders["attach_post_key"] = attachPostKey

        additionalHeaders["parent_id"] = "0"
        additionalHeaders["ed-0_wysiwyg_used"] = "0"
        additionalHeaders["editor_ids[]"] = "ed-0"
        additionalHeaders["_upload_single_file"] = "1"
        additionalHeaders["upload_process"] = "Закачать"
        additionalHeaders["file-list"] = addedFileList
        additionalHeaders["post_edit_reason"] = post_edit_reason


        if (postId != NEW_POST_ID) {
            additionalHeaders["p"] = postId
            additionalHeaders["act"] = "attach"
            additionalHeaders["attach_rel_id"] = postId
            additionalHeaders["code"] = "attach_upload_process"
        } else {
            additionalHeaders["act"] = "Post"

        }
        additionalHeaders["CODE"] = "03"
        additionalHeaders["Post"] = post
        if (enablesig!!)
            additionalHeaders["enablesig"] = "yes"
        if (enableEmo!!)
            additionalHeaders["enableEmo"] = "yes"
        additionalHeaders["iconid"] = "0"

        if (postId == NEW_POST_ID)
            return httpClient.uploadFile("https://${HostHelper.host}/forum/index.php", filePath, additionalHeaders,
                    progress).responseBody

        httpClient.uploadFile("https://${HostHelper.host}/forum/index.php", filePath, additionalHeaders, progress)
        progress.update("Файл загружен, получение страницы", 100)
        return getEditPage(httpClient, forumId, topicId, postId, authKey)
    }

    /**
     * Деаттачим файл
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun deleteAttachedFile(httpClient: IHttpClient, forumId: String, themeId: String, authKey: String,
                           postId: String, enablesig: Boolean?, enableemo: Boolean?,
                           post: String, attachToDeleteId: String, fileList: String,
                           post_edit_reason: String): String {

        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["st"] = "0"

        additionalHeaders["f"] = forumId
        additionalHeaders["auth_key"] = authKey
        additionalHeaders["removeattachid"] = "0"
        additionalHeaders["MAX_FILE_SIZE"] = "0"

        additionalHeaders["t"] = themeId
        additionalHeaders["p"] = postId
        additionalHeaders["ed-0_wysiwyg_used"] = "0"
        additionalHeaders["editor_ids[]"] = "ed-0"
        additionalHeaders["Post"] = post
        additionalHeaders["_upload_single_file"] = "1"
        additionalHeaders["post_edit_reason"] = post_edit_reason

        additionalHeaders["file-list"] = fileList
        additionalHeaders["removeattach[$attachToDeleteId]"] = "Удалить!"

        if (postId != NEW_POST_ID) {
            additionalHeaders["act"] = "attach"
            additionalHeaders["code"] = "attach_upload_remove"
            additionalHeaders["attach_rel_id"] = postId
            additionalHeaders["attach_id"] = attachToDeleteId
        } else {
            additionalHeaders["act"] = "Post"

        }
        additionalHeaders["CODE"] = "03"
        if (enablesig!!)
            additionalHeaders["enablesig"] = "yes"
        if (enableemo!!)
            additionalHeaders["enableemo"] = "yes"

        if (postId == NEW_POST_ID)
            return httpClient.performPost("https://${HostHelper.host}/forum/index.php", additionalHeaders).responseBody
        httpClient.performPost("https://${HostHelper.host}/forum/index.php", additionalHeaders)
        return getEditPage(httpClient, forumId, themeId, postId, authKey)
    }

    /**
     * Жалоба на пост
     *
     * @return Ошибка или пустая строка в случае успеха
     * @throws IOException
     */
    @Throws(IOException::class)
    fun claim(httpClient: IHttpClient, topicId: String, postId: String, message: String): String? {
        val additionalHeaders = HashMap<String, String>()
        additionalHeaders["act"] = "report"
        additionalHeaders["send"] = "1"
        additionalHeaders["t"] = topicId
        additionalHeaders["p"] = postId
        additionalHeaders["message"] = message

        val res = httpClient.performPost("https://${HostHelper.host}/forum/index.php?act=report&amp;send=1&amp;t=$topicId&amp;p=$postId", additionalHeaders)

        val p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE)
        val m = p.matcher(res.responseBody)
        return if (m.find()) {

            "Ошибка отправки жалобы: " + m.group(1)
        } else null
    }

    private fun getStatusMessage(status: String): String {
        when (status) {
            "no_items" -> return "Ни одного файла не загружено"
            "uploading_file" -> return "Загрузка файла..."
            "init_progress" -> return "Инициализация системы..."
            "upload_ok" -> return "Файл успешно загружен и доступен в меню «Управление текущими файлами»"
            "upload_failed" -> return "Неудачная загрузка. Необходимо проверить настройки и права доступа. Пожалуйста, сообщите об этом администрации."
            "upload_too_big" -> return "Неудачная загрузка. Файл имеет размер больше допустимого"
            "invalid_mime_type" -> return "Неудачная загрузка. Вам запрещено загружать такой тип файлов"
            "no_upload_dir" -> return "Неудачная загрузка. Директория загрузок файлов не доступна. Пожалуйста, сообщите об этом администрации."
            "no_upload_dir_perms" -> return "Неудачная загрузка. Невозможно произвести запись файла в директорию загрузок. Пожалуйста, сообщите об этом администрации."
            "upload_no_file" -> return "Вы не выбрали файл для загрузки"
            "upload_banned_file" -> return "Неудачная загрузка. Вам запрещено загружать этот файл"
            "ready" -> return "Система готова для загрузки файлов"
            "attach_remove" -> return "Удалить файл"
            "attach_insert" -> return "Вставить файл в текстовый редактор"
            "remove_warn" -> return "Продолжить удаление файла?"
            "attach_removed" -> return "Файл успешно удален"
            "attach_removal" -> return "Удаление файла..."
            else -> return status
        }
    }

    @Throws(NotReportException::class)
    private fun checkAttachError(page: String) {
        val m = Pattern
                .compile("pipsatt.status_msg = '([^']*)';\\s*pipsatt.status_is_error = parseInt\\('(\\d+)'\\);", Pattern.CASE_INSENSITIVE)
                .matcher(page)
        if (m.find()) {
            if ("1" == m.group(2))
                throw NotReportException(getStatusMessage(m.group(1)))
        }
    }

    @Throws(Exception::class)
    fun deleteAttachedFile(httpClient: IHttpClient,
                           postId: String,
                           attachId: String) {
        val res = httpClient
                .performGet(String.format("https://${HostHelper.host}/forum/index.php?&act=attach&code=attach_upload_remove&attach_rel_id=%s&attach_id=%s", postId,
                        attachId))
        checkAttachError(res.responseBody)
    }

    @Throws(Exception::class)
    fun attachFile(httpClient: IHttpClient,
                   postId: String,
                   newFilePath: String,
                   progress: ProgressState): EditAttach? {


        val res = httpClient.uploadFile("https://${HostHelper.host}/forum/index.php?&act=attach&code=attach_upload_process&attach_rel_id=$postId",
                newFilePath, HashMap(),
                progress)
        val m = Pattern
                .compile("add_current_item\\(\\s*'(\\d+)',\\s*'([^']*)',\\s*'([^']*)',\\s*'([^']*)'\\s*\\);", Pattern.CASE_INSENSITIVE)
                .matcher(res.responseBody)
        if (m.find()) {
            return EditAttach(m.group(1), m.group(2))
        }
        checkAttachError(res.responseBody)
        return null

    }

    @Throws(IOException::class)
    fun sendPost(httpClient: IHttpClient, params: EditPostParams,
                 postBody: String, postEditReason: String?,
                 enablesig: Boolean?, enableemo: Boolean?): String {

        if (!params.containsKey("editor_ids[]"))
            params.put("editor_ids[]", "ed-0")
        if (!params.containsKey("file-list"))
            params.put("file-list", "")
        if (!params.containsKey("forum-attach-files"))
            params.put("forum-attach-files", "")
        if (!params.containsKey("iconid"))
            params.put("iconid", "0")

        params.delete("use-template-data")
        params.delete("1_attach_box_index")

        val nameValuePairs = params.listParams
        nameValuePairs.add(BasicNameValuePair("post", postBody))
        if (postEditReason != null)
            nameValuePairs.add(BasicNameValuePair("post_edit_reason", postEditReason))

        if (enableemo == true)
            nameValuePairs.add(BasicNameValuePair("enableemo", "yes"))

        if (enablesig == true)
            nameValuePairs.add(BasicNameValuePair("enablesig", "yes"))


        return httpClient.performPost("https://${HostHelper.host}/forum/index.php", nameValuePairs).responseBody
    }


    @Throws(IOException::class)
    fun editPost(httpClient: IHttpClient, forumId: String, topicId: String, postId: String, authKey: String): EditPost {
        val editPage = getEditPage(httpClient, forumId, topicId, postId, authKey)
        val doc = Jsoup.parse(editPage)
        val editPost = EditPost()
        val postFormElement = doc.select("#postingform").first()

        if (postFormElement != null) {
            for (el in postFormElement.select("input[type=hidden]")) {
                val value = el.attr("value")
                editPost.params.put(el.attr("name"), value)
            }

            // название опроса
            var element: Element? = postFormElement.select("input[name=poll_question]").first()
            if (element != null)
                editPost.params.put("poll_question", element.attr("value"))

            parseInterviewParams(postFormElement.html(), editPost)

            // текст поста
            element = postFormElement.select("textarea[name=Post]").first()
            if (element != null)
                editPost.body = element.text()

            // Причина редактирования
            element = postFormElement.select("input[name=post_edit_reason]").first()
            if (element != null)
                editPost.postEditReason = element.attr("value")

            // Включить смайлы?
            element = postFormElement.select("input[name=enableemo]").first()
            if (element != null)
                editPost.isEnableEmo = "checked" == element.attr("checked")

            // Включить подпись?
            element = postFormElement.select("input[name=enablesig]").first()
            if (element != null)
                editPost.isEnableSign = "checked" == element.attr("checked")

            // управление текущими файлами
            val attachBody = getAttachPage(httpClient, postId)
            if (attachBody != null) {

                val m = Pattern
                        .compile("add_current_item\\( '(\\d+)', '([^']*)', '([^']*)', '([^']*)' \\)", Pattern.CASE_INSENSITIVE)
                        .matcher(attachBody)
                while (m.find()) {
                    editPost.addAttach(EditAttach(m.group(1), m.group(2)))
                }
            }
        }


        if (editPost.body == null) {
            val pattern = Pattern.compile("<h4>Причина:</h4>\n" +
                    "\\s*\n" +
                    "\\s*<p>(.*)</p>", Pattern.MULTILINE)

            val m = pattern.matcher(editPage)
            if (m.find()) {
                editPost.error = m.group(1)

            } else {
                editPost.error = "Неизвестная причина"
            }

        }
        return editPost
    }

    /*
    Парсим опрос
     */
    private fun parseInterviewParams(editPage: String, editPost: EditPost) {
        var m = Pattern.compile("poll_questions\\s*=\\s*\\{(.*)?'\\}", Pattern.CASE_INSENSITIVE)
                .matcher(editPage)
        if (m.find()) {
            val s = m.group(1) + "'"
            m = Pattern.compile("\\s*(\\d+)\\s*:\\s*'([^']*)'", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
                    .matcher(s)

            val choicesText = getChoicesText(editPage)
            while (m.find()) {
                val questionNum = m.group(1)
                editPost.params.put("question[$questionNum]", Html.fromHtml(m.group(2)).toString())

                val choiceMatcher = Pattern.compile("\\s*'(" + questionNum + "_\\d+)'\\s*:\\s*'([^']*)'",
                        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
                        .matcher(choicesText)
                while (choiceMatcher.find()) {
                    editPost.params.put("choice[" + choiceMatcher.group(1) + "]", Html.fromHtml(choiceMatcher.group(2)).toString())
                }


                val multiMatcher = Pattern.compile("\\s*$questionNum\\s*:\\s*'([^']*)'", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
                        .matcher(s)
                while (multiMatcher.find()) {
                    if ("1" == multiMatcher.group(1)) {
                        editPost.params.put("multi[$questionNum]", "1")
                    }
                }
            }
        }
    }


    private fun getChoicesText(editPage: String): String {
        var choicesText = ""
        val choicesMatcher = Pattern.compile("poll_choices\\s*=\\s*\\{(.*)?'\\}", Pattern.CASE_INSENSITIVE)
                .matcher(editPage)
        if (choicesMatcher.find()) {
            choicesText = choicesMatcher.group(1) + "'"
        }
        return choicesText
    }


    private fun getMultiText(editPage: String): String {
        val m = Pattern.compile("poll_multi\\s*=\\s*\\{(.*)?'\\}", Pattern.CASE_INSENSITIVE)
                .matcher(editPage)
        return if (m.find()) {
            m.group(1) + "'"

        } else ""
    }
}
