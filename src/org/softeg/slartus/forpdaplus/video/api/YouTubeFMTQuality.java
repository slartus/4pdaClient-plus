package org.softeg.slartus.forpdaplus.video.api;

/**
 * Youtube format quality of the video.
 */
public class YouTubeFMTQuality {

    public static final int GPP3_LOW = 13;        //3GPP (MPEG-4 encoded) Low quality
    public static final int GPP3_MEDIUM = 17;        //3GPP (MPEG-4 encoded) Medium quality
    public static final int MP4_NORMAL = 18;        //MP4  (H.264 encoded) Normal quality
    public static final int MP4_HIGH = 22;        //MP4  (H.264 encoded) High quality
    public static final int MP4_HIGH1 = 37;        //MP4  (H.264 encoded) High quality

    public static final CharSequence GPP3_LOW_TITLE = "Низкое-240p";        //3GPP (MPEG-4 encoded) Low quality
    public static final CharSequence GPP3_MEDIUM_TITLE = "Среднее-360p";        //3GPP (MPEG-4 encoded) Medium quality
    public static final CharSequence MP4_NORMAL_TITLE = "Высокое-480p";        //MP4  (H.264 encoded) Normal quality
    public static final CharSequence MP4_HIGH_TITLE = "HD-720p";        //MP4  (H.264 encoded) High quality
    public static final CharSequence MP4_HIGH1_TITLE = "HD-1080p";        //MP4  (H.264 encoded) High quality

    public static final int[] supported = {
            GPP3_LOW,
            GPP3_MEDIUM,
            MP4_NORMAL,
            MP4_HIGH,
            MP4_HIGH1
    };

    public static final CharSequence[] supported_titles = {
            GPP3_LOW_TITLE,
            GPP3_MEDIUM_TITLE,
            MP4_NORMAL_TITLE,
            MP4_HIGH_TITLE,
            MP4_HIGH1_TITLE
    };

    public static int getPreviousSupportedFormat(int fmtId) {
        int prevFmt = fmtId;
        for (int i = supported.length - 1; i >= 0; i--) {
            if (fmtId == supported[i] && i > 0) {
                prevFmt = supported[i - 1];
            }
        }
        return prevFmt;
    }
}
