package org.softeg.slartus.forpdaplus.classes.common;

/**
 * User: slinkin
 * Date: 30.08.11
 * Time: 16:20
 */

import android.app.ProgressDialog;
import org.softeg.slartus.forpdaplus.common.Log;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {

	File archive;
	String outputDir;

	ProgressDialog myProgress;

	public UnZip(File ziparchive, String directory) {
		archive = ziparchive;
		outputDir = directory;
	}



	public boolean run() {

		try {
			ZipFile zipfile = new ZipFile(archive);

			for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();

				unzipEntry(zipfile, entry, outputDir);

			}
			return true;

		} catch (IOException e1) {
			Log.e(e1.toString());
			return false;
		}

	}

	public void unzipArchive(File archive, String outputDir) {
		try {
			ZipFile zipfile = new ZipFile(archive);
			for (Enumeration<?> e = zipfile.entries(); e.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();

				unzipEntry(zipfile, entry, outputDir);
			}
		} catch (Exception e) {
			Log.e("Error while extracting file " + archive);
		}
	}

	private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir)
			throws IOException {

		if (entry.isDirectory()) {
			createDir(new File(outputDir, entry.getName()));
			return;
		}

		File outputFile = new File(outputDir, entry.getName());
		if (!outputFile.getParentFile().exists()) {
			createDir(outputFile.getParentFile());
		}


		BufferedInputStream inputStream = new BufferedInputStream(
				zipfile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(outputFile));

		try {
			IOUtils.copy(inputStream, outputStream);
		} finally {
			outputStream.close();
			inputStream.close();
		}
	}

	private void createDir(File dir) {

		if (!dir.mkdirs())
			throw new RuntimeException("Can not create dir " + dir);
	}
}

