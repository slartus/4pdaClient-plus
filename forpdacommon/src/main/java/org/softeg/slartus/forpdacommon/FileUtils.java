package org.softeg.slartus.forpdacommon;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 09.11.11
 * Time: 7:31
 */
public class FileUtils {
    public static String getAppExternalFolderPath() throws NotReportException {
        Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
        File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
        File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);
        String externalDirPath = Environment.getExternalStorageDirectory() == null ? null : Environment.getExternalStorageDirectory().toString();
        if (externalDirPath == null) {
            if (externalSdCard != null)
                externalDirPath = externalSdCard.toString();
            else if (sdCard != null)
                externalDirPath = sdCard.toString();
        }

        String path = externalDirPath + "/data/4pdaClient/";
        if (!FileUtils.hasStorage(path, true))
            throw new NotReportException("Нет доступа к папке программы: " + path);
        return path;
    }

    public static String readTrimRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = buffreader.readLine()) != null) {

                text.append(line.trim());
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    public static Uri saveFile(Context context, String dirPath, String fileName, String content) throws IOException {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(context, "Внешнее хранилище недоступно!", Toast.LENGTH_SHORT).show();
            return null;
        }


        File file = new File(dirPath, fileName);
        FileWriter out = new FileWriter(file);
        out.write(content);
        out.close();
        return Uri.fromFile(file);


    }

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

    public static float parseFileSize(String sizeStr) {

        Matcher m = Pattern.compile("(\\d+(?:(?:\\.|,)\\d+)?)\\s*(\\w+)\\s*").matcher(sizeStr);

        if (m.find()) {
            long k = 1;
            switch (m.group(2).toUpperCase()) {
                case "КБ":
                    k = 1024;
                    break;
                case "МБ":
                    k = 1024 * 1024;
                    break;
                case "ГБ":
                    k = 1024 * 1024 * 1024;
                    break;
            }
            return Float.parseFloat(m.group(1).replace(',', '.')) * k;
        }
        return 0;
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

    public static byte[] toByteArray(File file) throws IOException {

        InputStream input_stream = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(os);
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?',
            '*', '\\', '<', '>', '|', '\"', ':', '%'};

    /*
     * Нормализует(уберает иллегальные символы)
     */
    private static String normalize(String fileName) {
//        for (char illegalChar : ILLEGAL_CHARACTERS) {
//            fileName = fileName.replace(illegalChar, '_');
//        }
        return fileName.replaceAll("[^а-яА-Яa-zA-z0-9._-]", "_");
    }

    public static String getFileNameFromUrl(String url) throws UnsupportedEncodingException {
        String decodedUrl = UrlExtensions.decodeUrl(url).toString();
        int index = decodedUrl.lastIndexOf("/");

        return normalize(decodedUrl.substring(index + 1));
    }

    public static String getDirPath(String filePath) {

        return filePath.substring(0, filePath.lastIndexOf(File.separator));
    }

    public static String fileExt(String url) {
        int dotIndex = url.lastIndexOf(".");
        String ext = dotIndex != -1 ? url.substring(dotIndex) : "";
        if (ext.contains("?")) {
            ext = ext.substring(0, ext.indexOf("?"));
        }
        if (ext.contains("%")) {
            ext = ext.substring(0, ext.indexOf("%"));
        }
        return ext;
    }

    public static String combine(String path1, String path2) {

        if (!path1.endsWith(File.separator))
            path1 += File.separator;
        return path1 + path2;
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

    public static Boolean mkDirs(String filePath) {
        //int startind=1;
        String dirPath = new File(filePath).getParentFile().getAbsolutePath() + File.separator;

        File dir = new File(dirPath.replace("/", File.separator));
        return dir.exists() || dir.mkdirs();
//        while(true){
//             if(startind>=dirPath.length()||startind==-1)
//                return true;
//            int slashInd=dirPath.indexOf(File.separator,startind);
//            if(slashInd==-1)return true;
//            String subPath=dirPath.substring(0,slashInd);
//            File f=new File(subPath);
//            if(!f.exists()&&!f.mkdir()){
//                return false;
//            }
//            startind=subPath.length()+1;
//
//        }

    }

    static public boolean hasStorage(String dirPath, boolean requireWriteAccess) throws NotReportException {

        String state = Environment.getExternalStorageState();
        // Log.v(TAG, "storage state is " + state);
        if (Environment.MEDIA_REMOVED.equals(state))
            throw new NotReportException("Карта памяти не подключена: " + dirPath);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                //    Log.v(TAG, "storage writable is " + writable);
                return checkFsWritable(dirPath);
            } else {
                return true;
            }
        } else return !requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private static boolean checkFsWritable(String dirPath) {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.

        File directory = new File(dirPath);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
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
