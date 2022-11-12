package org.softeg.slartus.forpdaapi

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 28.02.13
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
abstract class ProgressState {
    abstract fun update(message: String, percents: Long)
}
