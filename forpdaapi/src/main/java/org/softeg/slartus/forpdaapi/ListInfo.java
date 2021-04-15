package org.softeg.slartus.forpdaapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by slartus on 20.02.14.
 */
public class ListInfo {
    private int from;// начиная с какой страницы или порядкового номера темы

    private int outCount;// тут возвращается сколько всего в листе элементов (сколько всего тем)
    private String title;// заголовок страницы

    public int getFrom() {
        return from;
    }

    private final Map<String, String> m_Params = new HashMap<>();

    public Map<String, String> getParams() {
        return m_Params;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
