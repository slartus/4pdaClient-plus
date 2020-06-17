//package org.softeg.slartus.forpdaapi.devdb;
//
//import android.net.Uri;
//import android.text.Html;
//
//import org.softeg.slartus.forpdaapi.IHttpClient;
//import org.softeg.slartus.forpdaapi.IListItem;
//import org.softeg.slartus.forpdacommon.ShowInBrowserException;
//import org.softeg.slartus.forpdacommon.UrlExtensions;
//
//import java.io.IOException;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Created by slartus on 06.03.14.
// */
//public class DevDbApi {
//
//    public static ArrayList<DevCatalog> parseDevicesTypes(IHttpClient client) throws Throwable {
//        String pageBody = client.performGet("https://devdb.ru");
//
//        Pattern pattern = Pattern.compile("<a href=\"(https://devdb.ru/\\w+)/\"><p><img src=\"([^\"]*)\" alt=\"([^\"]*)\"[^/>]*/><br /><br />([^<]*)</a></p>",
//                Pattern.CASE_INSENSITIVE);
//        Matcher m = pattern.matcher(pageBody);
//        ArrayList<DevCatalog> res = new ArrayList<>();
//        while (m.find()) {
//            DevCatalog f = new DevCatalog(m.group(1), m.group(4));
//            f.setDescription(m.group(3));
//            f.setImageUrl(UrlExtensions.removeDoubleSplitters("https://devdb.ru/" + m.group(2)));
//            f.setType(DevCatalog.DEVICE_TYPE);
//            res.add(f);
//        }
//        res.add(new DevCatalog("https://devdb.ru/accessories/", "Акссесуары").setType(DevCatalog.DEVICE_TYPE));
//        return res;
//    }
//
//    public static ArrayList<DevCatalog> getStandartDevicesTypes() {
//        ArrayList<DevCatalog> res = new ArrayList<>();
//
//        res.add(new DevCatalog("https://devdb.ru/pda/", "Коммуникаторы").setType(DevCatalog.DEVICE_TYPE));
//        res.add(new DevCatalog("https://devdb.ru/phone/", "Сотовые телефоны").setType(DevCatalog.DEVICE_TYPE));
//        res.add(new DevCatalog("https://devdb.ru/pnd/", "Навигаторы").setType(DevCatalog.DEVICE_TYPE));
//        res.add(new DevCatalog("https://devdb.ru/ebook/", "Электронные книги").setType(DevCatalog.DEVICE_TYPE));
//        res.add(new DevCatalog("https://devdb.ru/pad/", "Планшеты").setType(DevCatalog.DEVICE_TYPE));
//        res.add(new DevCatalog("https://devdb.ru/accessories/", "Акссесуары").setType(DevCatalog.DEVICE_TYPE));
//        return res;
//    }
//
//    public static ArrayList<DevCatalog> parseBrands(IHttpClient client, String devicesTypeUrl) throws Throwable {
//        String pageBody = client.performGet(devicesTypeUrl);
//
//        Pattern pattern = Pattern.compile("<li><a href=\"(https://devdb.ru/\\w+/[^\"]*)\">([^\"]*)</a></li>",
//                Pattern.CASE_INSENSITIVE);
//        Matcher m = pattern.matcher(pageBody);
//        ArrayList<DevCatalog> res = new ArrayList<>();
//        while (m.find()) {
//            DevCatalog f = new DevCatalog(m.group(1), m.group(2));
//            f.setType(DevCatalog.DEVICE_BRAND);
//            res.add(f);
//        }
//        return res;
//    }
//
//    public static ArrayList<DevModel> parseModels(IHttpClient client, String brandUrl) throws Throwable {
//        ArrayList<DevModel> res = new ArrayList<>();
//        String pageBody = client.performGet(brandUrl);
//        //  Pattern trPattern = Pattern.compile("<tr class=\"brand_model_preview_bp\">([\\s\\S]*?)compare_toggleRecord", Pattern.CASE_INSENSITIVE);
//        Pattern devicePattern = Pattern.compile("<a href=\"([^\"]*)\">([^<]*)</a>", Pattern.CASE_INSENSITIVE);
//        Pattern ratePattern = Pattern.compile("alt=\"Рейтинг: ([^\"]*)\" ", Pattern.CASE_INSENSITIVE);
//        Pattern imgPattern = Pattern.compile("<td[^>]*class=\"preview_img\"><a[^>]*><img src=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
//        Pattern descPattern = Pattern.compile("<td[^>]*class=\"small_description\">(.*?)</td>", Pattern.CASE_INSENSITIVE);
//
//        //Matcher trMatcher = trPattern.matcher(pageBody);
//        String[] trs = pageBody.split("<tr class=\"brand_model_preview_bp\">");
//        for (int i = 1; i < trs.length; i++) {
//
//            String trBody = trs[i];
//            Matcher m = devicePattern.matcher(trBody);
//
//            if (m.find()) {
//                DevModel f = new DevModel(m.group(1), Html.fromHtml(m.group(2)).toString());
//                m = ratePattern.matcher(trBody);
//                if (m.find())
//                    f.setRate(m.group(1));
//
//                m = imgPattern.matcher(trBody);
//                if (m.find())
//                    f.setImgUrl(m.group(1));
//
//                m = descPattern.matcher(trBody);
//                if (m.find())
//                    f.setDescription(Html.fromHtml(m.group(1).replace("<ul><li>", "").replace("<li>", "<br/>")));
//
//                res.add(f);
//            } else {
//                throw new Exception("Не смог получить модель " + brandUrl);
//            }
//        }
//        return res;
//    }
//
//    public static Boolean isCatalogUrl(String url) {
//        return Pattern
//                .compile("devdb.ru(?:(?:/|$)(?:pda|phone|pnd|ebook|pad|accessories)?(?:/$|$))", Pattern.CASE_INSENSITIVE)
//                .matcher(url).find();
//    }
//
//    public static Boolean isDevicesListUrl(String url) {
//        return Pattern
//                .compile("devdb.ru/(?:pda|phone|pnd|ebook|pad|accessories)/([^/$]+)", Pattern.CASE_INSENSITIVE)
//                .matcher(url).find();
//    }
//
//    public static Boolean isDeviceUrl(String url) {
//        return Pattern
//                .compile("devdb.ru/([^/$?&]+)", Pattern.CASE_INSENSITIVE)
//                .matcher(url).find();
//    }
//
//    public static DevCatalog getCatalog(String url) throws ShowInBrowserException {
//        Matcher m = Pattern.compile("devdb.ru", Pattern.CASE_INSENSITIVE).matcher(url);
//        if (!m.find())
//            throw new ShowInBrowserException("Не умею обрабаывать ссылки такого типа!", url);
//
//        Uri uri = Uri.parseCount(url);
//        DevCatalog root = new DevCatalog("-1", "DevDb.ru").setType(DevCatalog.ROOT);
//        if (uri.getPathSegments() == null || uri.getPathSegments().size() <= 0)
//            return root;
//
//        String title = uri.getPathSegments().get(0);
//        switch (uri.getPathSegments().get(0).toLowerCase()) {
//            case "pda":
//                title = "Коммуникаторы";
//                break;
//            case "phone":
//                title = "Сотовые телефоны";
//                break;
//            case "pnd":
//                title = "Навигаторы";
//                break;
//            case "ebook":
//                title = "Электронные книги";
//                break;
//            case "pad":
//                title = "Планшеты";
//                break;
//            case "accessories":
//                title = "Акссесуары";
//                break;
//            default:
//                return root;
//        }
//        DevCatalog devType = new DevCatalog("https://devdb.ru/" + uri.getPathSegments().get(0), title)
//                .setType(DevCatalog.DEVICE_TYPE);
//        devType.setParent(root);
//        if (uri.getPathSegments().size() < 2) {
//            return devType;
//        }
//        DevCatalog catalog = new DevCatalog(url, uri.getPathSegments().get(1));
//        catalog.setType(DevCatalog.DEVICE_BRAND);
//        catalog.setParent(devType);
//        return catalog;
//
//    }
//
//}
