package org.softeg.slartus.forpdaplus.classes

abstract class HtmlElement(private val name: String) {
    private val attrs = mutableMapOf<String, String>()
    private var body: String = ""

    fun body(body: String) {
        this.body = body
    }

    fun attr(attrName: String, attrValue: String){
        attrs[attrName] = attrValue
    }

    fun addClass(clazz: String) {
        attrs["class"] = (attrs.getOrElse("class") { "" } + " " + clazz.trim()).trim()
    }

    fun style(style: String) {
        attrs["style"] = style
    }

    override fun toString(): String {
        return buildString {
            append("<").append(name)
            if (attrs.any()) {
                attrs.forEach {attr->
                    append(buildAttr(attr.key, attr.value))
                }
            }
            if (body.isEmpty()) {
                append("/>")
            } else {
                append(">")
                append(body)
                append("</").append(name).append(">")
            }
        }
    }

    companion object {
        fun buildAttr(attrName: String, attrValue: String): String = buildString {
            append(" ")
            append(attrName)
            append("=\"")
            append(attrValue)
            append("\"")
        }
    }
}

class Div : HtmlElement("div")

fun div(innerHtml: Div.() -> String): String {
    return Div().apply {
        body(innerHtml())
    }.toString()
}

class A : HtmlElement("a") {
    fun title(value: String) {
        attr("title", value)
    }
}

fun a(innerHtml: A.() -> String): String {
    return A().apply {
        body(innerHtml())
    }.toString()
}

class Span : HtmlElement("span")

fun span(innerHtml: Span.() -> String): String {
    return Span().apply {
        body(innerHtml())
    }.toString()
}

class B : HtmlElement("b")

fun b(innerHtml: B.() -> String): String {
    return B().apply {
        body(innerHtml())
    }.toString()
}