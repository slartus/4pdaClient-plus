package org.softeg.slartus.forpdaplus.core_api.utils

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Description(val text: String)

interface Element {
    fun render(builder: StringBuilder)
}

open class TextElement(val text: String) : Element {
    override fun render(builder: StringBuilder) {
        builder.append(text)
    }
}

@DslMarker
annotation class RegexTagMarker

@RegexTagMarker
abstract class GroupElement : Element {
    private val children = arrayListOf<Element>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    protected fun <T : Element> addTag(tag: T): T {
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder) {
        for (c in children) {
            c.render(builder)
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder)
        return builder.toString()
    }
}

class REGEX : GroupElement() {
    fun pattern(pattern: String) = addTag(TextElement(pattern))

    fun htmlElement(name: String, init: HtmlElement.() -> Unit) =
        initTag(HtmlElement(name), init)

    fun multilinePattern() = addTag(MultilinePattern())

    fun regex(init: REGEX.() -> Unit) = initTag(REGEX(), init)

    operator fun String.unaryPlus() {
        addTag(TextElement(this))
    }

    operator fun Element.unaryPlus() {
        addTag(this)
    }
}

class Tag(val name: String, val value: String? = null) : Element {
    override fun render(builder: StringBuilder) {
        builder.append(name)
        if (value != null) {
            builder.append("""="$value"""")
        }
    }
}

class MultilinePattern : TextElement(MULTILINE_ANY_PATTERN)

class HtmlElement(val tag: String) : Element {
    private val children = arrayListOf<Element>()
    private val tags = arrayListOf<Element>()

    private fun <T : Element> addTag(tag: T): T {
        tags.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder) {
        builder.append("<\\s*$tag")
        for (c in tags) {
            builder.append("[^>]*")
            c.render(builder)
        }
        builder.append("[^>]*")
        builder.append(">")
        for (c in children) {
            c.render(builder)
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder)
        return builder.toString()
    }

    fun tag(name: String, value: String?) = addTag(Tag(name, value))

    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }

    operator fun Element.unaryPlus() {
        children.add(this)
    }

    fun close(){
        children.add(TextElement("<\\/$tag>"))
    }
}

fun regex(init: REGEX.() -> Unit): REGEX {
    val html = REGEX()
    html.init()
    return html
}