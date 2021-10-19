package org.softeg.slartus.forpdacommon

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlin.Throws
import org.softeg.slartus.forpdacommon.UrlExtensions
import org.softeg.slartus.forpdacommon.NotReportException
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object FileUtils {
    fun createTempFile(context: Context): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        return File.createTempFile(
            "FILE_${timeStamp}_",
            ".tmp",
            context.cacheDir
        )
    }

    fun copyInputStreamToFile(`in`: InputStream, file: File?) {
        var out: OutputStream? = null
        try {
            out = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
        } finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                out?.close()

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                `in`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        if (!contentUri.toString().startsWith("content://")) return contentUri.path

        // can post image
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(
            contentUri,
            filePathColumn,  // Which columns to return
            null,  // WHERE clause; which rows to return (all rows)
            null,  // WHERE clause selection arguments (none)
            null
        )!! // Order-by clause (ascending by name)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    @JvmStatic
    fun readFileText(filePath: String?): String {
        val text = StringBuilder()
        try {
            val br = BufferedReader(FileReader(filePath))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
        } catch (e: IOException) {
            //You'll need to add proper error handling here
        }
        return text.toString()
    }

    @JvmStatic
    fun parseFileSize(sizeStr: String?): Float {
        val m = Pattern.compile("(\\d+(?:(?:\\.|,)\\d+)?)\\s*(\\w+)\\s*").matcher(sizeStr)
        if (m.find()) {
            var k: Long = 1
            when (m.group(2).toUpperCase()) {
                "КБ" -> k = 1024
                "МБ" -> k = (1024 * 1024).toLong()
                "ГБ" -> k = (1024 * 1024 * 1024).toLong()
            }
            return m.group(1).replace(',', '.').toFloat() * k
        }
        return 0f
    }

    @JvmStatic
    fun getFileSizeString(fileSize: Float): String {
        if (fileSize > 1024 * 1024 * 1024) return (Math.round(fileSize / 1024 / 1024 / 1024 * 100) / 100.0).toString() + " ГБ"
        if (fileSize > 1024 * 1024) return (Math.round(fileSize / 1024 / 1024 * 100) / 100.0).toString() + " МБ"
        return if (fileSize > 1024) (Math.round(fileSize / 1024 * 100) / 100.0).toString() + " КБ" else (Math.round(
            fileSize * 100
        ) / 100.0).toString() + " Б"
    }

    @JvmStatic
    fun CopyStream(`is`: InputStream, os: OutputStream) {
        val buffer_size = 1024
        try {
            val bytes = ByteArray(buffer_size)
            while (true) {
                val count = `is`.read(bytes, 0, buffer_size)
                if (count == -1) break
                os.write(bytes, 0, count)
            }
        } catch (ex: Exception) {
        }
    }

    /*
     * Нормализует(уберает иллегальные символы)
     */
    private fun normalize(fileName: String): String {
//        for (char illegalChar : ILLEGAL_CHARACTERS) {
//            fileName = fileName.replace(illegalChar, '_');
//        }
        return fileName.replace("[^а-яА-Яa-zA-z0-9._-]".toRegex(), "_")
    }

    @JvmStatic
    @Throws(UnsupportedEncodingException::class)
    fun getFileNameFromUrl(url: String?): String {
        val decodedUrl = UrlExtensions.decodeUrl(url).toString()
        val index = decodedUrl.lastIndexOf("/")
        return normalize(decodedUrl.substring(index + 1))
    }

    fun getDirPath(filePath: String): String {
        return filePath.substring(0, filePath.lastIndexOf(File.separator))
    }

    fun getUniqueFilePath(dirPath: String, fileName: String): String {
        var dirPath = dirPath
        var name = fileName
        var ext = ""
        val ind = fileName.lastIndexOf(".")
        if (ind != -1) {
            name = fileName.substring(0, ind)
            ext = fileName.substring(ind)
        }
        if (!dirPath.endsWith(File.separator)) dirPath += File.separator
        var suffix = ""
        var c = 0
        while (File(dirPath + name + suffix + ext).exists() || File(dirPath + name + suffix + ext + "_download").exists()) {
            suffix = "($c)"
            c++
        }
        return dirPath + name + suffix + ext
    }

    @Throws(IOException::class)
    fun copy(src: File?, dst: File?) {
        val `in`: InputStream = FileInputStream(src)
        val out: OutputStream = FileOutputStream(dst)

        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        `in`.close()
        out.close()
    }

    @Throws(IOException::class)
    fun checkDirPath(dirPath: String) {
        var dirPath = dirPath
        if (!dirPath.endsWith(File.separator)) dirPath += File.separator
        val dir = File(dirPath)
        val file = File(getUniqueFilePath(dirPath, "4pda.tmp"))
        if (!dir.exists() && !dir.mkdirs()) throw NotReportException("Не могу создать папку по указанному пути!")
        if (!file.createNewFile()) throw NotReportException("Не могу создать файл по указанному пути!")
        file.delete()
    }
}