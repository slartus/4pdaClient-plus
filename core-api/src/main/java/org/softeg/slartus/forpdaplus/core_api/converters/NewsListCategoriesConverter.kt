package org.softeg.slartus.forpdaplus.core_api.converters

import okhttp3.ResponseBody
import org.softeg.slartus.forpdaplus.core_api.model.ApiNewsListCategoryItem
import org.softeg.slartus.forpdaplus.core_api.utils.closeQuietly
import org.softeg.slartus.hosthelper.HostHelper.Companion.DEFAULT_CHARSET
import retrofit2.Converter
import java.nio.charset.Charset

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
}
