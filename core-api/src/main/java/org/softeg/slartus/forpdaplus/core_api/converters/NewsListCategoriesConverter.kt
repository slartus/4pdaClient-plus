package org.softeg.slartus.forpdaplus.core_api.converters

import okhttp3.ResponseBody
import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListCategoryItem
import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListItem
import org.softeg.slartus.forpdaplus.core_api.utils.MULTILINE_ANY_PATTERN
import org.softeg.slartus.forpdaplus.core_api.utils.closeQuietly
import org.softeg.slartus.forpdaplus.core_api.utils.map
import org.softeg.slartus.forpdaplus.core_api.utils.regex
import org.softeg.slartus.hosthelper.HostHelper.Companion.DEFAULT_CHARSET
import retrofit2.Converter
import java.nio.charset.Charset
import java.util.regex.Pattern

object NewsListCategoriesConverter : Converter<ResponseBody, List<ApiNewsListCategoryItem>> {
    override fun convert(value: ResponseBody): List<ApiNewsListCategoryItem> {
        val source = value.source()
        try {
            val pageBody = source.readString(Charset.forName(DEFAULT_CHARSET))

            return emptyList()
        } finally {
            source.closeQuietly()
        }
    }

    fun parseBody(pageBody: String): List<ApiNewsListCategoryItem> {
        val matcher = menuMainMobilePattern.matcher(pageBody)

        matcher.find() || return emptyList()

        val menuMainMobile = matcher.group(1)

        return menuMainItemPattern
            .matcher(menuMainMobile)
            .map { itemMatcher ->
                val title = itemMatcher.group(2) ?: return@map null
                if (title.contains("<")) return@map null
                val body = itemMatcher.group(3)
                val path = if (body.isNullOrEmpty()) itemMatcher.group(1) else null
                val children = if (!body.isNullOrEmpty()) {
                    menuSubItemPattern
                        .matcher(body)
                        .map { subItemMatcher ->

                            ApiNewsListCategoryItem(
                                title = subItemMatcher.group(2),
                                path = subItemMatcher.group(1),
                                children = null
                            )
                        }.toList()
                } else null

                ApiNewsListCategoryItem(
                    title = title,
                    path = path,
                    children = children
                )
            }
            .filterNotNull()
            .toList()
    }

    private val menuMainMobilePattern by lazy {
        Pattern.compile(
            regex {
                htmlElement("div") {
                    tag("class", "menu-main-mobile")
                    +"($MULTILINE_ANY_PATTERN)"
                }
                +"(?="
                htmlElement("div") {
                    tag("class", "search-div-mobile")
                }
                +")"
            }.toString(), Pattern.CASE_INSENSITIVE
        )
    }

    private val menuMainItemPattern by lazy {
        Pattern.compile(menuMainItemRegex.toString(), Pattern.CASE_INSENSITIVE)
    }

    private val menuSubItemPattern by lazy {
        Pattern.compile(menuSubItemRegex.toString(), Pattern.CASE_INSENSITIVE)
    }

    private val menuMainItemRegex by lazy {
        regex {
            htmlElement("li") {
                tagAny("class", "menu-main-item")
                htmlElement("a") {
                    tag("href", "([^\"]+)")
                    +"($MULTILINE_ANY_PATTERN)"
                    close()
                }
                +"($MULTILINE_ANY_PATTERN)"

                close()
            }
        }
    }

    private val menuSubItemRegex by lazy {
        regex {
            htmlElement("a") {
                tag("class", "menu-sub-item")
                tag("href", "([^\"]+)")
                +"\\s*"
                htmlElement("div") {
                    tag("class", "sub-title")
                    +"(.*?)"
                    close()
                }
                +"\\s*"
                htmlElement("div") {
                    tag("class", "sub-desc")
                    +".*?"
                    close()
                }
                +"\\s*"
                close()
            }
        }
    }
}
