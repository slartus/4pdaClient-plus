package ru.slartus.http.prefs

import java.io.*
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException

object MBFileUtils {

    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    @Throws(IOException::class)
    fun createFile(filePath: String, content: String) {
        File(filePath).createNewFile()
        writeToFile(content, filePath)
    }

    @Throws(IOException::class)
    fun readFile(filePath: String): String {
        val file = File(filePath)
        val length = file.length().toInt()

        val bytes = ByteArray(length)

        val `in` = FileInputStream(file)
        `in`.use {
            it.read(bytes)
        }

        return String(bytes)
    }

    @Throws(IOException::class)
    fun writeToFile(content: String, filePath: String) {
        var writed = false
        do {
            try {
                val stream = FileOutputStream(filePath)
                val lock = stream.channel.lock()
                try {
                    stream.use {
                        it.write(content.toByteArray())
                        writed = true
                    }
                } finally {
                    lock.release()
                }
            }catch (ofle:OverlappingFileLockException){
                try {
                    // Wait a bit
                    Thread.sleep(10)
                } catch (ex: InterruptedException) {
                    throw InterruptedIOException("Interrupted waiting for a file lock.")
                }

            }

        } while (!writed)

    }
}
