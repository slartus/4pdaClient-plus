package org.softeg.slartus.forpdaapi;

import java.util.ArrayList;

/**
 * Created by isanechek on 1/31/18.
 */

public class NewsPair {
    public ArrayList<News> news;
    public int sizeResponse;

    public NewsPair(ArrayList<News> news, int sizeResponse) {
        this.news = news;
        this.sizeResponse = sizeResponse;
    }

    public ArrayList<News> getNews() {
        return news;
    }

    public void setNews(ArrayList<News> news) {
        this.news = news;
    }

    public int getSizeResponse() {
        return sizeResponse;
    }

    public void setSizeResponse(int sizeResponse) {
        this.sizeResponse = sizeResponse;
    }
}
