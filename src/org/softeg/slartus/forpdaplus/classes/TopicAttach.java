package org.softeg.slartus.forpdaplus.classes;

/**
 * User: slinkin
 * Date: 22.05.12
 * Time: 9:38
 */
public class TopicAttach {
    private String Uri;
    private String PostId;
    private String PostNum;
    private String FileName;
    private String FileSize;
    private String DownloadsCount;

    public TopicAttach(String postId, String postNum, String url, String fileName, String fileSize, String downloadsCount) {
        PostId = postId;
        PostNum = postNum;
        Uri = url;
        FileName = fileName;
        FileSize = fileSize;
        DownloadsCount = downloadsCount;
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
