package org.softeg.slartus.forpdaplus.core_ui.navigation

interface AppRouter {
    fun navigateTo(appScreen: AppScreen)

    fun startService(appService: AppService)

    fun openUrl(url: String)
    /**
     * Sends data to listener with given key.
     */
    fun sendResult(key: String, data: Any)


    /**
     * Sets data listener with given key
     * and returns [ResultListenerHandler] for availability to dispose subscription.
     *
     * After first call listener will be removed.
     */
    fun setResultListener(
        key: String,
        listener: ResultListener
    ): ResultListenerHandler

    /**
     * Return to the previous screen in the chain.
     * Behavior in the case when the current screen is the root depends on the processing of the Back command in a Navigator implementation.
     */
    fun exit()
}


/**
 * Interface definition for a result callback.
 */
fun interface ResultListener {
    fun onResult(data: Any)
}

/**
 * Handler for manual delete subscription and avoid leak
 */
fun interface ResultListenerHandler {
    fun dispose()
}