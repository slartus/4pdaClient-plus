package org.softeg.slartus.forpdacommon


open class NameValuePair(val name: String, val value: String?)
class BasicNameValuePair(name: String, value: String?) : NameValuePair(name, value)