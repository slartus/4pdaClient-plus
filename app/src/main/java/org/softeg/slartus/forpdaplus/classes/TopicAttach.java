package org.softeg.slartus.forpdaplus.classes;

/**
 * User: slinkin
 * Date: 22.05.12
 * Time: 9:38
 */
public class TopicAttach {
    private String Uri;

    private String FileName;
    private String FileSize;


    public TopicAttach(String url, String fileName, String fileSize) {

        Uri = url;
        FileName = fileName;
        FileSize = fileSize;

    }

    public String getUri() {
        return Uri;
    }


    @Override
    public String toString() {
        //  return "#"+PostNum+": "+ Html.fromHtml(FileName);
        return FileName + " (" + FileSize + ")";
    }
}
