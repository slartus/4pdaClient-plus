package org.softeg.slartus.forpdaplus.fragments.search

data class SearchResultPost(
    @JvmField
    val titleHtml: String? = null,
    @JvmField
    val dateTimeHtml: String? = null,
    @JvmField
    val userState: String? = null,
    @JvmField
    val userId: String? = null,
    @JvmField
    val userName: String? = null,
    @JvmField
    val postBodyHtml: String? = null
)