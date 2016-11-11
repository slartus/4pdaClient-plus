package org.softeg.slartus.forpdaplus.classes.common;

import android.graphics.Color;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * User: slinkin
 * Date: 18.06.12
 * Time: 9:14
 */
public class ExtColor {

    public static HashMap<String, String> getColorNames() {
        HashMap<String, String> colornames = new HashMap<String, String>();
        colornames.put("aliceblue", "#f0f8ff");
        colornames.put("antiquewhite", "#faebd7");
        colornames.put("aqua", "#00ffff");
        colornames.put("aquamarine", "#7fffd4");
        colornames.put("azure", "#f0ffff");
        colornames.put("beige", "#f5f5dc");
        colornames.put("bisque", "#ffe4c4");
        colornames.put("black", "#000000");
        colornames.put("blanchedalmond", "#ffebcd");
        colornames.put("blue", "#0000ff");
        colornames.put("blueviolet", "#8a2be2");
        colornames.put("brown", "#a52a2a");
        colornames.put("burlywood", "#deb887");
        colornames.put("cadetblue", "#5f9ea0");
        colornames.put("chartreuse", "#7fff00");
        colornames.put("chocolate", "#d2691e");
        colornames.put("coral", "#ff7f50");
        colornames.put("cornflowerblue", "#6495ed");
        colornames.put("cornsilk", "#fff8dc");
        colornames.put("crimson", "#dc143c");
        colornames.put("cyan", "#00ffff");
        colornames.put("darkblue", "#00008b");
        colornames.put("darkcyan", "#008b8b");
        colornames.put("darkgoldenrod", "#b8860b");
        colornames.put("darkgray", "#a9a9a9");
        colornames.put("darkgrey", "#a9a9a9");
        colornames.put("darkgreen", "#006400");
        colornames.put("darkkhaki", "#bdb76b");
        colornames.put("darkmagenta", "#8b008b");
        colornames.put("darkolivegreen", "#556b2f");
        colornames.put("darkorange", "#ff8c00");
        colornames.put("darkorchid", "#9932cc");
        colornames.put("darkred", "#8b0000");
        colornames.put("darksalmon", "#e9967a");
        colornames.put("darkseagreen", "#8fbc8f");
        colornames.put("darkslateblue", "#483d8b");
        colornames.put("darkslategray", "#2f4f4f");
        colornames.put("darkslategrey", "#2f4f4f");
        colornames.put("darkturquoise", "#00ced1");
        colornames.put("darkviolet", "#9400d3");
        colornames.put("deeppink", "#ff1493");
        colornames.put("deepskyblue", "#00bfff");
        colornames.put("dimgray", "#696969");
        colornames.put("dimgrey", "#696969");
        colornames.put("dodgerblue", "#1e90ff");
        colornames.put("firebrick", "#b22222");
        colornames.put("floralwhite", "#fffaf0");
        colornames.put("forestgreen", "#228b22");
        colornames.put("fuchsia", "#ff00ff");
        colornames.put("gainsboro", "#dcdcdc");
        colornames.put("ghostwhite", "#f8f8ff");
        colornames.put("gold", "#ffd700");
        colornames.put("goldenrod", "#daa520");
        colornames.put("gray", "#808080");
        colornames.put("grey", "#808080");
        colornames.put("green", "#008000");
        colornames.put("greenyellow", "#adff2f");
        colornames.put("honeydew", "#f0fff0");
        colornames.put("hotpink", "#ff69b4");
        colornames.put("indianred ", "#cd5c5c");
        colornames.put("indigo ", "#4b0082");
        colornames.put("ivory", "#fffff0");
        colornames.put("khaki", "#f0e68c");
        colornames.put("lavender", "#e6e6fa");
        colornames.put("lavenderblush", "#fff0f5");
        colornames.put("lawngreen", "#7cfc00");
        colornames.put("lemonchiffon", "#fffacd");
        colornames.put("lightblue", "#add8e6");
        colornames.put("lightcoral", "#f08080");
        colornames.put("lightcyan", "#e0ffff");
        colornames.put("lightgoldenrodyellow", "#fafad2");
        colornames.put("lightgray", "#d3d3d3");
        colornames.put("lightgrey", "#d3d3d3");
        colornames.put("lightgreen", "#90ee90");
        colornames.put("lightpink", "#ffb6c1");
        colornames.put("lightsalmon", "#ffa07a");
        colornames.put("lightseagreen", "#20b2aa");
        colornames.put("lightskyblue", "#87cefa");
        colornames.put("lightslategray", "#778899");
        colornames.put("lightslategrey", "#778899");
        colornames.put("lightsteelblue", "#b0c4de");
        colornames.put("lightyellow", "#ffffe0");
        colornames.put("lime", "#00ff00");
        colornames.put("limegreen", "#32cd32");
        colornames.put("linen", "#faf0e6");
        colornames.put("magenta", "#ff00ff");
        colornames.put("maroon", "#800000");
        colornames.put("mediumaquamarine", "#66cdaa");
        colornames.put("mediumblue", "#0000cd");
        colornames.put("mediumorchid", "#ba55d3");
        colornames.put("mediumpurple", "#9370d8");
        colornames.put("mediumseagreen", "#3cb371");
        colornames.put("mediumslateblue", "#7b68ee");
        colornames.put("mediumspringgreen", "#00fa9a");
        colornames.put("mediumturquoise", "#48d1cc");
        colornames.put("mediumvioletred", "#c71585");
        colornames.put("midnightblue", "#191970");
        colornames.put("mintcream", "#f5fffa");
        colornames.put("mistyrose", "#ffe4e1");
        colornames.put("moccasin", "#ffe4b5");
        colornames.put("navajowhite", "#ffdead");
        colornames.put("navy", "#000080");
        colornames.put("oldlace", "#fdf5e6");
        colornames.put("olive", "#808000");
        colornames.put("olivedrab", "#6b8e23");
        colornames.put("orange", "#ffa500");
        colornames.put("orangered", "#ff4500");
        colornames.put("orchid", "#da70d6");
        colornames.put("palegoldenrod", "#eee8aa");
        colornames.put("palegreen", "#98fb98");
        colornames.put("paleturquoise", "#afeeee");
        colornames.put("palevioletred", "#d87093");
        colornames.put("papayawhip", "#ffefd5");
        colornames.put("peachpuff", "#ffdab9");
        colornames.put("peru", "#cd853f");
        colornames.put("pink", "#ffc0cb");
        colornames.put("plum", "#dda0dd");
        colornames.put("powderblue", "#b0e0e6");
        colornames.put("purple", "#800080");
        colornames.put("red", "#ff0000");
        colornames.put("rosybrown", "#bc8f8f");
        colornames.put("royalblue", "#4169e1");
        colornames.put("saddlebrown", "#8b4513");
        colornames.put("salmon", "#fa8072");
        colornames.put("sandybrown", "#f4a460");
        colornames.put("seagreen", "#2e8b57");
        colornames.put("seashell", "#fff5ee");
        colornames.put("sienna", "#a0522d");
        colornames.put("silver", "#c0c0c0");
        colornames.put("skyblue", "#87ceeb");
        colornames.put("slateblue", "#6a5acd");
        colornames.put("slategray", "#708090");
        colornames.put("slategrey", "#708090");
        colornames.put("snow", "#fffafa");
        colornames.put("springgreen", "#00ff7f");
        colornames.put("steelblue", "#4682b4");
        colornames.put("tan", "#d2b48c");
        colornames.put("teal", "#008080");
        colornames.put("thistle", "#d8bfd8");
        colornames.put("tomato", "#ff6347");
        colornames.put("turquoise", "#40e0d0");
        colornames.put("violet", "#ee82ee");
        colornames.put("wheat", "#f5deb3");
        colornames.put("white", "#ffffff");
        colornames.put("whitesmoke", "#f5f5f5");
        colornames.put("yellow", "#ffff00");
        return colornames;
    }

    public static int parseColor(String colorString) {

        try {
            return Color.parseColor(colorString);
        } catch (Exception ex) {
            Log.e("ExtColor", ex.toString());
        }

        HashMap<String, String> colornames = getColorNames();
        return Color.parseColor(colornames.get(colorString.toLowerCase()));
    }

    public static String encodeRGB(int color) {
        HashMap<String, String> colornames = getColorNames();

        String res = "#" + Integer.toHexString(color).substring(2);

        for (Map.Entry<String, String> entry : colornames.entrySet()) {
            if (entry.getValue().equals(res))
                return entry.getKey();
        }
        return res;
    }
}
