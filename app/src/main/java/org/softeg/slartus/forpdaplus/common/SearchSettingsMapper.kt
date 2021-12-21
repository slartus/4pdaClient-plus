package org.softeg.slartus.forpdaplus.common

import org.softeg.slartus.forpdaplus.core.entities.SearchSettings

fun SearchSettings.map(): org.softeg.slartus.forpdaapi.search.SearchSettings {
    val result = org.softeg.slartus.forpdaapi.search.SearchSettings(this.searchType.toOld())

    result.query = this.query
    result.userName = this.userName
    result.setSearchInSubForums(this.searchInSubForums)
    result.resultView = this.resultView.toOld()
    result.sort = this.sortType.toOld()
    result.source = this.sourceType.toOld()
    result.topicIds.addAll(this.topicIds)
    result.forumsIds.addAll(this.forumIds)
    return result
}

fun SearchSettings.SearchType.toOld(): String {
    return when (this) {
        SearchSettings.SearchType.Forum -> org.softeg.slartus.forpdaapi.search.SearchSettings.SEARCH_TYPE_FORUM
        SearchSettings.SearchType.Topic -> org.softeg.slartus.forpdaapi.search.SearchSettings.SEARCH_TYPE_TOPIC
        SearchSettings.SearchType.UserTopics -> org.softeg.slartus.forpdaapi.search.SearchSettings.SEARCH_TYPE_USER_TOPICS
        SearchSettings.SearchType.UserPosts -> org.softeg.slartus.forpdaapi.search.SearchSettings.SEARCH_TYPE_USER_POSTS
    }
}

fun SearchSettings.ResultView.toOld(): String {
    return when (this) {
        SearchSettings.ResultView.Topics -> org.softeg.slartus.forpdaapi.search.SearchSettings.RESULT_VIEW_TOPICS
        SearchSettings.ResultView.Posts -> org.softeg.slartus.forpdaapi.search.SearchSettings.RESULT_VIEW_TOPICS
    }
}

fun SearchSettings.SortType.toOld(): String {
    return when (this) {
        SearchSettings.SortType.Relevant -> org.softeg.slartus.forpdaapi.search.SearchSettings.RESULT_SORT_RELEVANT
        SearchSettings.SortType.DateAsc -> org.softeg.slartus.forpdaapi.search.SearchSettings.RESULT_SORT_DATE
        SearchSettings.SortType.DateDesc -> org.softeg.slartus.forpdaapi.search.SearchSettings.RESULT_SORT_DATE_DESC
    }
}

fun SearchSettings.SourceType.toOld(): String {
    return when (this) {
        SearchSettings.SourceType.All -> org.softeg.slartus.forpdaapi.search.SearchSettings.SOURCE_ALL
        SearchSettings.SourceType.Topics -> org.softeg.slartus.forpdaapi.search.SearchSettings.SOURCE_TOPICS
        SearchSettings.SourceType.Posts -> org.softeg.slartus.forpdaapi.search.SearchSettings.SOURCE_POSTS
    }
}