package ise.roletagger.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.collections4.map.HashedMap;

public class CategoryTrees {

	private static final Map<Category,Map<String,Integer>> trees = new TreeMap<>(); 
		
	public void load(String categoryTreeFolder) {
		try {
			final File[] listOfFiles = new File(categoryTreeFolder).listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				final String file = listOfFiles[i].getName();
				final BufferedReader br = new BufferedReader(new FileReader(categoryTreeFolder+File.separator+file));
				String line;
				final Map<String,Integer> map = new HashedMap<>();
				while ((line = br.readLine()) != null) {
					final String[] split = line.split(";");
					map.put(split[0],Integer.parseInt(split[1]));
				}
				trees.put(Category.resolve(file), map);
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Category existInAnyTree(String query) {
		for(Entry<Category, Map<String, Integer>> entry:trees.entrySet()) {
			if(entry.getValue().containsKey(query)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
