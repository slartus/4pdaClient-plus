package org.softeg.slartus.forpdaplus.styles;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by slinkin on 17.06.13.
 */
public class CssStyle {
    public Boolean ExistsInfo = true;
    public String Title;
    public String Version;


    public String Author;
    public String Comment;
    public ArrayList<CssStyleScreenShot> ScreenShots = new ArrayList<CssStyleScreenShot>();

    public static CssStyle parseStyleFromAssets(Context context, String filePath) {
        CssStyle cssStyle = new CssStyle();

        cssStyle.Title = new File(filePath).getName();
        try {

            cssStyle = parseStyle(cssStyle, context.getAssets().open(filePath));
        } catch (Throwable ex) {
            cssStyle.ExistsInfo = false;
        }
        return cssStyle;
    }

    public static CssStyle parseStyleFromFile(String filePath) {
        CssStyle cssStyle = new CssStyle();
        File file = new File(filePath);
        cssStyle.Title = file.getName().replace(".xml", "");
        try {
            if (file.exists())
                cssStyle = parseStyle(cssStyle, new FileInputStream(filePath));
            else
                cssStyle.ExistsInfo = false;
        } catch (Throwable ex) {
            cssStyle.ExistsInfo = false;
        }


        return cssStyle;
    }

    public static CssStyle parseStyle(Context context, String filePath) {
        if (filePath.startsWith("/android_asset"))
            return parseStyleFromAssets(context, filePath.replace("/android_asset/", ""));
        return parseStyleFromFile(filePath);

    }

    private static CssStyle parseStyle(CssStyle cssStyle, InputStream in) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);


        int eventType = parser.getEventType();
        if (eventType == XmlPullParser.END_DOCUMENT)
            return null;

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            eventType = parser.getEventType();

            if (eventType == XmlPullParser.START_TAG) {
                String name = parser.getName().toLowerCase();
                if (name.equals("title")) {
                    parser.next();
                    eventType = parser.getEventType();
                    if (eventType == XmlPullParser.TEXT)
                        cssStyle.Title = parser.getText();
                } else if (name.equals("version")) {
                    parser.next();
                    eventType = parser.getEventType();
                    if (eventType == XmlPullParser.TEXT)
                        cssStyle.Version = parser.getText();
                } else if (name.equals("author")) {
                    parser.next();
                    eventType = parser.getEventType();
                    if (eventType == XmlPullParser.TEXT)
                        cssStyle.Author = parser.getText();
                } else if (name.equals("comment")) {
                    parser.next();
                    eventType = parser.getEventType();
                    if (eventType == XmlPullParser.TEXT)
                        cssStyle.Comment = parser.getText();
                } else if (name.equals("screenshot")) {
                    cssStyle.ScreenShots.add(parseScreenShot(parser));
                }

            }
        }
        return cssStyle;
    }

    public static CssStyleScreenShot parseScreenShot(XmlPullParser parser) throws IOException, XmlPullParserException {
        CssStyleScreenShot screenShot = new CssStyleScreenShot();
        screenShot.Preview = parser.getAttributeValue(null, "Preview");
        screenShot.FullView = parser.getAttributeValue(null, "FullView");
        return screenShot;
    }
}
