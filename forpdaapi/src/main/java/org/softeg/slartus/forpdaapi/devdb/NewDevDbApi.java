package org.softeg.slartus.forpdaapi.devdb;

import android.net.Uri;
import android.text.Html;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by isanechek on 03.11.15.
 */
public class NewDevDbApi {

    public static ArrayList<DevCatalog> getStandartDevicesTypes() {
        ArrayList<DevCatalog> res = new ArrayList<>();
        res.add(new DevCatalog("http://4pda.ru/devdb/phones/", "Телефоны").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("http://4pda.ru/devdb/pad/", "Планшеты").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("http://4pda.ru/devdb/ebook/", "Электронные книги").setType(DevCatalog.DEVICE_TYPE));
        return res;
    }

    public static ArrayList<DevCatalog> parseBrands(IHttpClient client, String devicesTypeUrl) throws Throwable {
        String pageBody = client.performGet(devicesTypeUrl + "all");
        Document doc = Jsoup.parse(pageBody);
        ArrayList<DevCatalog> res = new ArrayList<>();

        Elements con = doc.getElementsByClass("word-list");
        Elements con1 = con.select("li");
        for (Element element1 : con1) {
            String brandsLink = element1.getElementsByTag("a").attr("href");
            String brandsName = element1.text();
            DevCatalog f = new DevCatalog(brandsLink, brandsName);
            f.setType(DevCatalog.DEVICE_BRAND);
            res.add(f);
        }
        return res;
    }

    public static ArrayList<DevModel> parseModels(IHttpClient client, String brandUrl) throws Throwable {
        ArrayList<DevModel> res = new ArrayList<>();
        String pageBody = client.performGet(brandUrl + "/all");

        Document doc = Jsoup.parse(pageBody);
        Elements con = doc.select("div.box-holder");

        for (int i = 0; i < con.size(); i++) {
            String link = con.get(i).select(".visual a").first().attr("href");
            String title = con.get(i).select(".visual a").first().attr("title");
            String image = con.get(i).select(".visual img").first().attr("src");

            DevModel model = new DevModel(link, title);
            model.setImgUrl(image);
            model.setDescription(con.get(i).select(".frame .specifications-list").first().text());

            res.add(model);
        }
        return res;
    }

    public static Boolean isCatalogUrl(String url) {
        return Pattern
                .compile("4pda.ru/devdb(?:(?:/|$)(?:phones|ebook|pad)?(?:/$|$))", Pattern.CASE_INSENSITIVE)
                .matcher(url).find();
    }

    public static Boolean isDevicesListUrl(String url) {
        return Pattern
                .compile("4pda.ru/devdb(?:phones|ebook|pad)/([^/$]+)", Pattern.CASE_INSENSITIVE)
                .matcher(url).find();
    }

    public static Boolean isDeviceUrl(String url) {
        return Pattern
                .compile("4pda.ru/devdb/([^/$?&]+)", Pattern.CASE_INSENSITIVE)
                .matcher(url).find();
    }

    public static DevCatalog getCatalog(String url) throws ShowInBrowserException {
        Matcher m = Pattern.compile("4pda.ru/devdb", Pattern.CASE_INSENSITIVE).matcher(url);
        if (!m.find())
            throw new ShowInBrowserException("Не умею обрабаывать ссылки такого типа!", url);

        Uri uri = Uri.parse(url);
        DevCatalog root = new DevCatalog("-1", "DevDb").setType(DevCatalog.ROOT);
        if (uri.getPathSegments() == null || uri.getPathSegments().size() <= 0)
            return root;

        String title = uri.getPathSegments().get(0);
        switch (uri.getPathSegments().get(0).toLowerCase()) {
            case "phone":
                title = "Сотовые телефоны";
                break;
            case "ebook":
                title = "Электронные книги";
                break;
            case "pad":
                title = "Планшеты";
                break;
            default:
                return root;
        }
        DevCatalog devType = new DevCatalog("http://4pda.ru/devdb/" + uri.getPathSegments().get(0), title)
                .setType(DevCatalog.DEVICE_TYPE);
        devType.setParent(root);
        if (uri.getPathSegments().size() < 2) {
            return devType;
        }
        DevCatalog catalog = new DevCatalog(url, uri.getPathSegments().get(1));
        catalog.setType(DevCatalog.DEVICE_BRAND);
        catalog.setParent(devType);
        return catalog;

    }
}
