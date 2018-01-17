package ise.roletagger.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ise.roletagger.util.Config;
import ise.roletagger.util.FileUtil;
import ise.roletagger.util.MapUtil;

public class CategoryTree {
	private final String root;
	private final Map<String,Integer> subclasses = new HashMap<>();
	private final int treeDepth;

	public CategoryTree(String root, int treeDepth) {
		this.root = root;
		this.treeDepth = treeDepth;
	}

	public void addSubClass(String s,int count) {
		if(!subclasses.containsKey(s)) {
			subclasses.put(s,count);
		}
	}

	public void writeToFile() {
		final String folderName = Config.getString("CATEGORY_TREE_FOLDER", "");
		FileUtil.createFolder(folderName);
		Path path = Paths.get(folderName);
		try {
			Files.createDirectories(path);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		final Map<String, Integer> sortByValueAscending = MapUtil.sortByValueAscending2(subclasses);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(folderName+File.separator+root))) {
			for(Entry<String,Integer> entity:sortByValueAscending.entrySet()) {
				bw.write(entity.getKey()+";"+entity.getValue()+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
