package org.softeg.slartus.forpdaapi.devdb;

import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.softeg.slartus.forpdaapi.IHttpClient;
import org.softeg.slartus.forpdacommon.ShowInBrowserException;
import org.softeg.slartus.hosthelper.HostHelper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by isanechek on 03.11.15.
 */
public class NewDevDbApi {

    public static ArrayList<DevCatalog> getStandartDevicesTypes() {
        ArrayList<DevCatalog> res = new ArrayList<>();
        res.add(new DevCatalog("https://" + HostHelper.getHost() + "/devdb/phones/", "Телефоны").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("https://" + HostHelper.getHost() + "/devdb/pad/", "Планшеты").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("https://" + HostHelper.getHost() + "/devdb/ebook/", "Электронные книги").setType(DevCatalog.DEVICE_TYPE));
        res.add(new DevCatalog("https://" + HostHelper.getHost() + "/devdb/smartwatch/", "Смарт часы").setType(DevCatalog.DEVICE_TYPE));
        return res;
    }

    public static ArrayList<DevCatalog> parseBrands(IHttpClient client, String devicesTypeUrl) throws Throwable {
        String pageBody = client.performGet(devicesTypeUrl + "all").getResponseBody();
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
        String pageBody = client.performGet(brandUrl + "/all").getResponseBody();

        Document doc = Jsoup.parse(pageBody);
        Elements con = doc.selectFirst("div.device-frame").getElementById("device-brand-items-list").children();

        for (int i = 0; i < con.size(); i++) {
            Element box = con.get(i).selectFirst("div.box-holder");
            if (box != null) {
                String link = box.selectFirst("a").attr("href");
                String title = box.selectFirst("a").attr("title");
                String image = box.selectFirst("img").attr("src");

                DevModel model = new DevModel(link, title);
                model.setImgUrl(image);
                model.setDescription(box.select(".frame .specifications-list").first().text());

                res.add(model);
            }
        }
        return res;
    }

    public static Boolean isCatalogUrl(String url) {
        return Pattern
                .compile(HostHelper.getHostPattern()+"\\/devdb\\/(?:phones|ebook|pad|smartwatch)?(?:\\/all\\/?|\\/?$)", Pattern.CASE_INSENSITIVE)
                .matcher(url).find();
    }

    public static Boolean isDevicesListUrl(String url) {
        return Pattern
                .compile(HostHelper.getHostPattern()+"\\/devdb\\/(?:phones|ebook|pad|smartwatch)\\/(?!all)", Pattern.CASE_INSENSITIVE)
                .matcher(url).find();
    }

    public static Boolean isDeviceUrl(String url) {
        return Pattern
                .compile(HostHelper.getHostPattern()+"\\/devdb\\/(?!phones|ebook|pad|smartwatch)[^$]+", Pattern.CASE_INSENSITIVE)
                .matcher(url).find();
    }

    public static DevCatalog getCatalog(String url) throws ShowInBrowserException {
        Matcher m = Pattern.compile(HostHelper.getHost() + "/devdb", Pattern.CASE_INSENSITIVE).matcher(url);
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
            case "smartwatch":
                title = "Смарт часы";
            default:
                return root;
        }
        DevCatalog devType = new DevCatalog("https://" + HostHelper.getHost() + "/devdb/" + uri.getPathSegments().get(0), title)
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
