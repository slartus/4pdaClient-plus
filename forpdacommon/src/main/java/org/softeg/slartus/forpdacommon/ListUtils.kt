package org.softeg.slartus.forpdacommon

infix fun <T> List<T>?.sameContentWith(other: List<T>?): Boolean {
    if (this == null && other != null) return false
    if (this != null && other == null) return false
    if (this == null && other == null) return true
    return this?.size == other?.size && this?.containsAll(other ?: emptyList()) == true
}