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

        return pattern
            .matcher(pageBody)
            .map { matcher ->
                var group = 1
                val title = matcher.group(group++)
                val path = matcher.group(group)
                ApiNewsListCategoryItem(
                    title = title,
                    path = path
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
                    close()
                }
                htmlElement("div") {
                    tag("class", "search-div-mobile")
                }
            }.toString(), Pattern.CASE_INSENSITIVE
        )
    }

    private val menuMainItemRegex by lazy {
        regex {
            htmlElement("li") {
                tag("class", "menu-main-item")
                htmlElement("a") {
                    tag("href", "([^\"]+)")
                    +"(.*?)"
                    close()
                }
                close()
            }
        }
    }

    private val menuMainItemWSubRegex by lazy {
        regex {
            htmlElement("li") {
                tag("class", "menu-main-item w-sub")
                htmlElement("a") {
                    tag("href", "[^\"]+")
                    +"($MULTILINE_ANY_PATTERN)"
                    close()
                }
                htmlElement("div") {
                    tag("class", "menu-sub")
                    +"("
                    +menuSubItemRegex
                    +")+"
                    close()
                }

                close()
            }
        }
    }

    private val menuSubItemRegex by lazy {
        regex {
            htmlElement("a") {
                tag("class", "menu-sub-item")
                tag("href", "([^\"]+)")

                htmlElement("div") {
                    tag("class", "sub-title")
                    +"(.*?)"
                    close()
                }
                htmlElement("div") {
                    tag("class", "sub-desc")
                    +".*?"
                    close()
                }

                close()
            }
        }
    }
}
