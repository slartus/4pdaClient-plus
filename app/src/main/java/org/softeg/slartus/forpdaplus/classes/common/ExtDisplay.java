package org.softeg.slartus.forpdaplus.classes.common;

import android.graphics.Point;
import android.view.Display;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 03.12.12
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class ExtDisplay {
    public static Point getDisplaySize(Display display){
        
        
            Point screenSize = new Point();
            display.getSize(screenSize);
            return screenSize;
        
    } 
}
