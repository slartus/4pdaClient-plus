package org.softeg.slartus.forpdaplus.classes;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 22.05.12
 * Time: 9:39
 */
public class TopicAttaches extends ArrayList<TopicAttach> {
    private final Pattern FULL_ATTACH_PATTERN = Pattern.compile("href=\"(/forum/dl/post/\\d+/.*?)\".*?>(.*?)</a>\\s*\\(\\s*(.*?)\\s*\\)");


    public void parseAttaches(String postBody) {
        Pattern p = Pattern.compile(".*>(.*?)$");
        final Matcher m = FULL_ATTACH_PATTERN.matcher(postBody);
        while (m.find()) {
            Matcher m1 = p.matcher(m.group(2));
            if (m1.find())
                add("", "", "http://4pda.ru" + m.group(1), m1.group(1), m.group(3), "");

        }

    }


    private void add(String postId, String postNum, String url, String fileName, String fileSize, String downloadsCount) {
        add(new TopicAttach(postId, postNum, url, fileName, fileSize, downloadsCount));
    }

    public CharSequence[] getList() {
        CharSequence[] res = new CharSequence[size()];
        for (int i = 0; i < size(); i++) {
            res[i] = get(i).toString();
        }
        return res;
    }

}
