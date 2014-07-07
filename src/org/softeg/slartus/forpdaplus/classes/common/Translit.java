package org.softeg.slartus.forpdaplus.classes.common;

/**
 * User: slinkin
 * Date: 02.08.12
 * Time: 14:18
 */
import java.util.Hashtable;

public class Translit {

    final static int NEUTRAL = 0;

    final static int UPPER = 1;

    final static int LOWER = 2;

    final static Hashtable map = makeTranslitMap();

    private static Hashtable makeTranslitMap() {
        Hashtable map = new Hashtable();
        map.put(new Character('а'), "a");
        map.put(new Character('б'), "b");
        map.put(new Character('в'), "v");
        map.put(new Character('г'), "g");
        map.put(new Character('д'), "d");
        map.put(new Character('е'), "e");
        map.put(new Character('ё'), "yo");
        map.put(new Character('ж'), "zh");
        map.put(new Character('з'), "z");
        map.put(new Character('и'), "i");
        map.put(new Character('й'), "j");
        map.put(new Character('к'), "k");
        map.put(new Character('л'), "l");
        map.put(new Character('м'), "m");
        map.put(new Character('н'), "n");
        map.put(new Character('о'), "o");
        map.put(new Character('п'), "p");
        map.put(new Character('р'), "r");
        map.put(new Character('с'), "s");
        map.put(new Character('т'), "t");
        map.put(new Character('у'), "u");
        map.put(new Character('ф'), "f");
        map.put(new Character('х'), "h");
        map.put(new Character('ц'), "ts");
        map.put(new Character('ч'), "ch");
        map.put(new Character('ш'), "sh");
        map.put(new Character('щ'), "sh'");
        map.put(new Character('ъ'), "`");
        map.put(new Character('ы'), "y");
        map.put(new Character('ь'), "'");
        map.put(new Character('э'), "e");
        map.put(new Character('ю'), "yu");
        map.put(new Character('я'), "ya");
        map.put(new Character('«'), "\"");
        map.put(new Character('»'), "\"");
        map.put(new Character('№'), "No");
        return map;
    }

    private static int charClass(char c) {
        if (Character.isLowerCase(c))
            return LOWER;
        if (Character.isUpperCase(c))
            return UPPER;
        return NEUTRAL;
    }

    public static String translit(String text) {
        int len = text.length();
        if (len == 0)
            return text;
        StringBuffer sb = new StringBuffer();
        int pc = NEUTRAL;
        char c = text.charAt(0);
        int cc = charClass(c);
        for (int i = 1; i <= len; i++) {
            char nextChar = (i < len ? text.charAt(i) : ' ');
            int nc = charClass(nextChar);
            Character co = new Character(Character.toLowerCase(c));
            String tr = (String) map.get(co);
            if (tr == null) {
                sb.append(c);
            } else {
                switch (cc) {
                    case LOWER:
                    case NEUTRAL:
                        sb.append(tr);
                        break;
                    case UPPER:
                        if (nc == LOWER || (nc == NEUTRAL && pc != UPPER)) {
                            sb.append(Character.toUpperCase(tr.charAt(0)));
                            if (tr.length() > 0) {
                                sb.append(tr.substring(1));
                            }
                        } else {
                            sb.append(tr.toUpperCase());
                        }
                }
            }
            c = nextChar;
            pc = cc;
            cc = nc;
        }
        return sb.toString();
    }

    public static String makeFileName(String text) {
        int len = text.length();
        if (len == 0)
            return text;
        StringBuffer sb = new StringBuffer();
        char lastAppended = 0;
        int count = 0;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if ((c & 0xFFFF) > 0x7F) {
                // keep non-ASCII as is
            } else if (c <= ' ' || c == '/' || c == '\\' || c == ':' || c == '~' || c == '"'
                    //|| c == '.'
                    ) {
                c = '_';
            }
            if (c == '_' && lastAppended == '_')
                continue;
            sb.append(c);
            if (++count > 50)
                break;
            lastAppended = c;
        }
        return sb.toString();
    }
}
