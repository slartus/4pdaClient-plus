package ru.slartus.http

/**
 * User: slinkin
 * Date: 02.08.12
 * Time: 14:18
 */
import java.util.Hashtable

@Suppress("unused")
object Translit {

    private const val NEUTRAL = 0

    private const val UPPER = 1

    private const val LOWER = 2

    internal val map: Hashtable<Char, String> = makeTranslitMap()

    private fun makeTranslitMap(): Hashtable<Char, String> {
        val map = Hashtable<Char, String>()
        map['а'] = "a"
        map['б'] = "b"
        map['в'] = "v"
        map['г'] = "g"
        map['д'] = "d"
        map['е'] = "e"
        map['ё'] = "yo"
        map['ж'] = "zh"
        map['з'] = "z"
        map['и'] = "i"
        map['й'] = "j"
        map['к'] = "k"
        map['л'] = "l"
        map['м'] = "m"
        map['н'] = "n"
        map['о'] = "o"
        map['п'] = "p"
        map['р'] = "r"
        map['с'] = "s"
        map['т'] = "t"
        map['у'] = "u"
        map['ф'] = "f"
        map['х'] = "h"
        map['ц'] = "ts"
        map['ч'] = "ch"
        map['ш'] = "sh"
        map['щ'] = "sh'"
        map['ъ'] = "`"
        map['ы'] = "y"
        map['ь'] = "'"
        map['э'] = "e"
        map['ю'] = "yu"
        map['я'] = "ya"
        map['«'] = "\""
        map['»'] = "\""
        map['№'] = "No"
        return map
    }

    private fun charClass(c: Char): Int {
        if (Character.isLowerCase(c))
            return LOWER
        return if (Character.isUpperCase(c)) UPPER else NEUTRAL
    }

    fun translit(text: String): String {
        val len = text.length
        if (len == 0)
            return text
        val sb = StringBuffer()
        var pc = NEUTRAL
        var c = text[0]
        var cc = charClass(c)
        for (i in 1..len) {
            val nextChar = if (i < len) text[i] else ' '
            val nc = charClass(nextChar)
            val co = Character.toLowerCase(c)
            val tr = map[co]
            if (tr == null) sb.append(c)
            else
                when (cc) {
                    LOWER, NEUTRAL -> sb.append(tr)
                    UPPER -> if (nc == LOWER || nc == NEUTRAL && pc != UPPER) {
                        sb.append(Character.toUpperCase(tr[0]))
                        if (tr.isNotEmpty()) {
                            sb.append(tr.substring(1))
                        }
                    } else {
                        sb.append(tr.toUpperCase())
                    }
                }
            c = nextChar
            pc = cc
            cc = nc
        }
        return sb.toString()
    }

    fun makeFileName(text: String): String {
        val len = text.length
        if (len == 0)
            return text
        val sb = StringBuffer()
        var lastAppended: Char = 0.toChar()
        var count = 0
        for (i in 0 until len) {
            var c = text[i]
            if (c.toInt() and 0xFFFF > 0x7F) {
                // keep non-ASCII as is
            } else if (c <= ' ' || c == '/' || c == '\\' || c == ':' || c == '~' || c == '"') {
                c = '_'
            }//|| c == '.'
            if (c == '_' && lastAppended == '_')
                continue
            sb.append(c)
            if (++count > 50)
                break
            lastAppended = c
        }
        return sb.toString()
    }
}
