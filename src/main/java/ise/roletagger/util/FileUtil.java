package ise.roletagger.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import ise.roletagger.model.Tuple;

public class FileUtil {

	public static void writeDataToFile(List<String> data,final String fileName,final boolean append) {
		try {
			final FileWriter fw = new FileWriter(fileName, append);
			for (String s : data) {
				fw.write(s+"\n");
			}
			fw.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeDataToFile(List<String> data,final File file) {
		try {
			final FileWriter fw = new FileWriter(file);
			for(String s:data) {
				fw.write(s+"\n");
			}
			fw.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean createFolder(String folderName) {
		Path path = Paths.get(folderName);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
				return true;
			} catch (IOException e) {
				return false;
			}
		}else{
			return true;
		}

	}

	public static void deleteFolder(String folder) {
		File index = new File(folder);
		final String[] entries = index.list();
		if(entries!=null) {
			for(String s: entries){
				File currentFile = new File(index.getPath(),s);
				currentFile.delete();
			}
			final File currentFile = new File(folder);
			currentFile.delete();
		}
	}
	
	public static void writeToFile(List<Tuple> result, String fileName,String Splitter,boolean append) {
		try {
			final FileWriter fw = new FileWriter(fileName, append);
			for (Tuple t : result) {
				fw.write(t.a + Splitter + t.b+"\n");
			}
			fw.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
