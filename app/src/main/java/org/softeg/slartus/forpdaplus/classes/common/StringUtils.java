package org.softeg.slartus.forpdaplus.classes.common;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

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

    public static String fromClipboard(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        assert clipboardManager != null;
        if (clipboardManager.hasPrimaryClip()) {
            android.content.ClipData data = clipboardManager.getPrimaryClip();
            if (data != null)
                for (int i = 0; i < data.getItemCount(); i++) {
                    CharSequence clipboardText = data.getItemAt(i).getText();
                    if (clipboardText != null)
                        if ("primaryClip".contentEquals(clipboardText) || "clipboardManager".contentEquals(clipboardText))
                            clipboardText = null;
                    if (clipboardText != null)
                        clipboardText = clipboardText.toString().trim();
                    if (!TextUtils.isEmpty(clipboardText))
                        return clipboardText.toString().trim();
                }
        }
        return null;
    }

    public static void copyToClipboard(Context context, String link) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("url", link);
        clipboard.setPrimaryClip(clip);
    }
}
