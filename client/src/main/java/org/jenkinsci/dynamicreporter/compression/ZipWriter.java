package org.jenkinsci.dynamicreporter.compression;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriter {

	private List<String> filesListInDir = Lists.newArrayList();
	private String targetZipFileName;
	private String sourceDirectory;

	public ZipWriter(String targetZipFileName, String sourceDirectory) {
		this.targetZipFileName = targetZipFileName;
		this.sourceDirectory = sourceDirectory;
	}

	public void zipDirectory() throws IOException {
		File dir = new File(sourceDirectory);
		populateFilesList(dir);
		//now zip files one by one
		//create ZipOutputStream to write to the zip file
		FileOutputStream fos = new FileOutputStream(targetZipFileName);
		ZipOutputStream zos = new ZipOutputStream(fos);
		for(String filePath : filesListInDir){
			//for ZipEntry we need to keep only relative file path, so we used substring on absolute path
			ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length()));
			zos.putNextEntry(ze);
			//read the file and write to ZipOutputStream
			FileInputStream fis = new FileInputStream(filePath);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			zos.closeEntry();
			fis.close();
		}
		zos.close();
		fos.close();
	}

	private void populateFilesList(File directory) throws IOException {
		File[] files = directory.listFiles();
		for (File file : files){
			if (file.isFile()) {
				filesListInDir.add(file.getAbsolutePath());
			} else {
				populateFilesList(file);
			}
		}
	}

}