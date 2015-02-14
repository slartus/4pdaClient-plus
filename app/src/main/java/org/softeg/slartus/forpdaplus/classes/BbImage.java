package org.softeg.slartus.forpdaplus.classes;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 27.05.13
 * Time: 8:43
 * To change this template use File | Settings | File Templates.
 */
public class BbImage {
    public BbImage(String path, String fileName, String code) {
        FilePath = path + fileName;
        FileName = fileName;
        Code = code;
    }

    public String FileName;
    public String FilePath;
    public String Code;
}
