package org.softeg.slartus.forpdaapi.common

import org.jsoup.select.Elements

fun Elements?.getOrNull(index: Int) = if (this?.size ?: 0 > index) this?.get(index) else null