package org.softeg.slartus.forpdaplus.classes.common;

import android.content.Context;

import java.util.List;

public class StringUtils {

    public static String join(List<String> values, String string) {

        StringBuilder sb = new StringBuilder();
        int c = values.size();
        for (String val : values) {
            if (c-- > 1)
                sb.append(val + string);
            else
                sb.append(val);

        }
        return sb.toString();
    }


    public static void copyToClipboard(Context context, String link) {


        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("url", link);
        clipboard.setPrimaryClip(clip);

    }


    public static void translite(CharSequence str) {
        String res = "";
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            if (ch < 'А' && ch > 'я') {
                res += ch;
                continue;
            }
            switch (ch) {
                case 'А':
                case 'а':
                    res += 'a';

            }
        }
    }


}
