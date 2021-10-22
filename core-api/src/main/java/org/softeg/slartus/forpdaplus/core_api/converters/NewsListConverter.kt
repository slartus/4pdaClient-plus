package org.softeg.slartus.forpdaplus.core_api.converters

import okhttp3.ResponseBody
import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListCategoryItem
import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListItem
import org.softeg.slartus.forpdaplus.core_api.utils.*
import org.softeg.slartus.hosthelper.HostHelper.Companion.DEFAULT_CHARSET
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class NewsListConverterFactory private constructor() : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        val parameterizedType = type as? ParameterizedType? ?: return null
        if (parameterizedType.actualTypeArguments.any { it === ApiNewsListItem::class.java }) return NewsListConverter
        if (parameterizedType.actualTypeArguments.any { it === ApiNewsListCategoryItem::class.java }) return NewsListCategoriesConverter

        return null
    }

    companion object {
        fun create(): NewsListConverterFactory {
            return NewsListConverterFactory()
        }
    }
}

object NewsListConverter : Converter<ResponseBody, List<ApiNewsListItem>> {
    override fun convert(value: ResponseBody): List<ApiNewsListItem> {
        val source = value.source()
        try {
            val pageBody = source.readString(Charset.forName(DEFAULT_CHARSET))

            return parseBody(pageBody)
        } finally {
            source.closeQuietly()
        }
    }

    fun parseBody(pageBody: String): List<ApiNewsListItem> {
        return articlePattern
            .matcher(pageBody)
            .map { matcher ->
                var group = 1
                val id = matcher.group(group++)
                val url = matcher.group(group++)
                val title = matcher.group(group++)
                val imgUrl = matcher.group(group++)
                val commentsCount = matcher.group(group++)?.toIntOrNull()
                val authorId = matcher.group(group++)
                val author = matcher.group(group++)
                val date = matcher.group(group++)
                val description = matcher.group(group)
                ApiNewsListItem(
                    id = id,
                    url = url,
                    title = title,
                    imgUrl = imgUrl,
                    authorId = authorId,
                    author = author,
                    date = parseDate(date),
                    description = description,
                    avatar = null,
                    commentsCount = commentsCount,
                    tags = emptyList()
                )
            }
            .filterNotNull()
            .toList()
    }

    private val dateFormat by lazy {
        SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ",
            Locale.getDefault()
        )
    }

    private fun parseDate(dateString: String?): Date? {
        return if (dateString == null) null else try {
            dateFormat.parse(dateString)
        } catch (ex: Exception) {
            return null
        }
    }

    private val articlePattern by lazy {
        Pattern.compile(
            regex {
                htmlElement("article") {
                    tag("class", "post[^\"]+")
                    tag("itemtype", "[^\"]*Article")
                    tag("itemid", "(\\d+)")
                    +MULTILINE_ANY_PATTERN
                    +articleRegex
                    +MULTILINE_ANY_PATTERN
                    close()
                }
            }.toString(), Pattern.CASE_INSENSITIVE
        )
    }

    private val articleRegex by lazy {
        regex {

            @Description("название")
            htmlElement("a") {
                tag("href", "([^\\\"]+)")
                tag("title", "([^\\\"]+)")
            }
            multilinePattern()

            @Description("постер")
            htmlElement("img") {
                tag("itemprop", "image")
                tag("src", "([^\"]+)")
            }
            multilinePattern()

            @Description("кол-во комментариев")
            htmlElement("a") {
                tag("class", "v-count")
                +"(\\d+)"
                close()
            }
            multilinePattern()

            @Description("автор")
            htmlElement("a") {
                tag("href", "[^\"]+showuser=(\\d+)[^\"]*")
                +"($MULTILINE_ANY_PATTERN)"
                close()
            }
            multilinePattern()

            @Description("дата публикации")
            htmlElement("meta") {
                tag("itemprop", "datePublished")
                tag("content", "([^\"]+)")
            }
            multilinePattern()

            @Description("тело новости")
            htmlElement("div") {
                tag("itemprop", "description")
                +"($MULTILINE_ANY_PATTERN)"
                close()
            }
        }
    }
}
