package org.softeg.slartus.forpdaplus.core.entities

data class SearchSettings(
    val query: String? = null,
    val userName: String? = null,
    val searchType: SearchType = SearchType.Forum,
    val searchInSubForums: Boolean = true,
    val resultView: ResultView = ResultView.Posts,
    val sortType: SortType = SortType.DateDesc,
    val sourceType: SourceType = SourceType.All,
    val topicIds: Set<String> = emptySet(),
    val forumIds: Set<String>
) {
    enum class SearchType {
        Forum, Topic, UserTopics, UserPosts
    }

    enum class ResultView {
        Topics, Posts
    }

    enum class SortType {
        Relevant, DateAsc, DateDesc
    }

    enum class SourceType {
        All, Topics, Posts
    }
}