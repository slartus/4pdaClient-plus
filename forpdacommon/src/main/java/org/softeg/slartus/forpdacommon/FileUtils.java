package org.softeg.slartus.forpdacommon;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: slinkin
 * Date: 09.11.11
 * Time: 7:31
 */
public class FileUtils {


    public static String getRealPathFromURI(Context context, Uri contentUri) {
        if (!contentUri.toString().startsWith("content://"))
            return contentUri.getPath();

        // can post image
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri,
                filePathColumn, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    public static String readFileText(String filePath) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text.toString();
    }


    public static String getFileSizeString(float fileSize) {

        if (fileSize > 1024 * 1024 * 1024)
            return Math.round(fileSize / 1024 / 1024 / 1024 * 100) / 100.0 + " ГБ";
        if (fileSize > 1024 * 1024)
            return Math.round(fileSize / 1024 / 1024 * 100) / 100.0 + " МБ";
        if (fileSize > 1024)
            return Math.round(fileSize / 1024 * 100) / 100.0 + " КБ";
        return Math.round(fileSize * 100) / 100.0 + " Б";
    }

    public static String getUniqueFilePath(String dirPath, String fileName) {
        String name = fileName;
        String ext = "";
        int ind = fileName.lastIndexOf(".");
        if (ind != -1) {
            name = fileName.substring(0, ind);
            ext = fileName.substring(ind);
        }
        if (!dirPath.endsWith(File.separator))
            dirPath += File.separator;
        String suffix = "";
        int c = 0;
        while (new File(dirPath + name + suffix + ext).exists() || new File(dirPath + name + suffix + ext + "_download").exists()) {
            suffix = "(" + c + ")";
            c++;
        }
        return dirPath + name + suffix + ext;
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void checkDirPath(String dirPath) throws IOException {
        if (!dirPath.endsWith(File.separator))
            dirPath += File.separator;
        File dir = new File(dirPath);
        File file = new File(FileUtils.getUniqueFilePath(dirPath, "4pda.tmp"));

        if (!dir.exists() && !dir.mkdirs())
            throw new NotReportException("Не могу создать папку по указанному пути!");

        if (!file.createNewFile())
            throw new NotReportException("Не могу создать файл по указанному пути!");
        file.delete();
    }
}
