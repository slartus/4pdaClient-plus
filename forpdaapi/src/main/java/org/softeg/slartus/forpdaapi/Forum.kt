package org.softeg.slartus.forpdaapi

import java.io.Serializable

class Forum(var id: String?, val title: String) : Serializable {
    var description: String? = null
    var isHasTopics = false
    var isHasForums = false
    var iconUrl: String? = null
    var parentId: String? = null

    override fun toString(): String {
        return title
    }

    override fun equals(other: Any?): Boolean {

        return other is Forum? && other?.hashCode() == hashCode()
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + title.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + isHasTopics.hashCode()
        result = 31 * result + isHasForums.hashCode()
        result = 31 * result + (iconUrl?.hashCode() ?: 0)
        result = 31 * result + (parentId?.hashCode() ?: 0)
        return result
    }
}