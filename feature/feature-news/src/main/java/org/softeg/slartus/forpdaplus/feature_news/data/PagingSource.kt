package org.softeg.slartus.forpdaplus.feature_news.data

import androidx.paging.*
import org.softeg.slartus.forpdaplus.feature_news.di.NewsListService
import ru.slartus.http.HttpException
import java.io.IOException

//https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data
class NewsListPagingSource(
    private val backend: NewsListService,
    val page: Int?,
    val query: String
) : PagingSource<Int, NewsListItem>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, NewsListItem> {
        return try {
            // Start refresh at page 1 if undefined.
            val startPage = page ?: DEFAULT_START_PAGE
            val pageIndex = params.key ?: startPage
            val response = backend.all(pageIndex)

            val nextKey = if (response.isEmpty()) null else pageIndex + 1
            LoadResult.Page(
                data = response,
                prevKey = if (pageIndex == startPage) null else pageIndex, // Only paging forward.
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, NewsListItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        const val DEFAULT_START_PAGE = 1
    }
}

//
////https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data
////https://developer.android.com/topic/libraries/architecture/paging/v3-network-db
//@OptIn(ExperimentalPagingApi::class)
//class NewsListPagingSource(
//    val networkService: NewsListService,
//    val query: String
//) : RemoteMediator<Int, List<NewsListItem>>() {
//
////    override suspend fun initialize(): InitializeAction {
////        val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
////        return if (System.currentTimeMillis() - db.lastUpdated() >= cacheTimeout)
////        {
////            // Cached data is up-to-date, so there is no need to re-fetch
////            // from the network.
////            InitializeAction.SKIP_INITIAL_REFRESH
////        } else {
////            // Need to refresh cached data from network; returning
////            // LAUNCH_INITIAL_REFRESH here will also block RemoteMediator's
////            // APPEND and PREPEND from running until REFRESH succeeds.
////            InitializeAction.LAUNCH_INITIAL_REFRESH
////        }
////    }
//
//    override suspend fun load(
//        loadType: LoadType,
//        state: PagingState<Int, List<NewsListItem>>
//    ): MediatorResult {
//        return try {
//            // The network load method takes an optional after=<user.id>
//            // parameter. For every page after the first, pass the last user
//            // ID to let it continue from where it left off. For REFRESH,
//            // pass null to load the first page.
//            val loadKey = when (loadType) {
//                LoadType.REFRESH -> null
//                // In this example, you never need to prepend, since REFRESH
//                // will always load the first page in the list. Immediately
//                // return, reporting end of pagination.
//                LoadType.PREPEND ->
//                    return MediatorResult.Success(endOfPaginationReached = true)
//                LoadType.APPEND -> {
//                    val lastItem = state.lastItemOrNull()
//
//                    // You must explicitly check if the last item is null when
//                    // appending, since passing null to networkService is only
//                    // valid for initial load. If lastItem is null it means no
//                    // items were loaded after the initial REFRESH and there are
//                    // no more items to load.
//                    if (lastItem == null) {
//                        return MediatorResult.Success(
//                            endOfPaginationReached = true
//                        )
//                    }
//
//                    lastItem.size
//                }
//            }
//
//            // Suspending network load via Retrofit. This doesn't need to be
//            // wrapped in a withContext(Dispatcher.IO) { ... } block since
//            // Retrofit's Coroutine CallAdapter dispatches on a worker
//            // thread.
//            val response = networkService.getNewsList(
//                page = 1
//            )
//
////            database.withTransaction {
////                if (loadType == LoadType.REFRESH) {
////                    userDao.deleteByQuery(query)
////                }
////
////                // Insert new users into database, which invalidates the
////                // current PagingData, allowing Paging to present the updates
////                // in the DB.
////                userDao.insertAll(response.users)
////            }
//
//            MediatorResult.Success(
//                endOfPaginationReached = response.isEmpty()
//            )
//        } catch (e: IOException) {
//            MediatorResult.Error(e)
//        } catch (e: HttpException) {
//            MediatorResult.Error(e)
//        }
//
//    }
//
////    override fun getRefreshKey(state: PagingState<Int, NewsListItem>): Int? {
////        return state.anchorPosition?.let { anchorPosition ->
////            val anchorPage = state.closestPageToPosition(anchorPosition)
////            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
////        }
////    }
//}