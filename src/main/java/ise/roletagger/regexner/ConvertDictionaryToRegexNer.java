package ise.roletagger.regexner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ise.roletagger.model.Category;
import ise.roletagger.model.RoleListProvider;
import ise.roletagger.util.Config;
import ise.roletagger.util.DictionaryRegexPatterns;
import ise.roletagger.util.FileUtil;

public class ConvertDictionaryToRegexNer {
	private static final String REGEXNER_ADDRESS= Config.getString("WHERE_TO_WRITE_REGEXNER", "");
	
	public static void main(String args[]) {
		Thread t = new Thread(run());
		t.setDaemon(false);
		t.start();
	}
	
	public static Runnable run() {
		return () -> {
			final Map<Category, Set<String>> dictionaries = DictionaryRegexPatterns.getCategoryToRoles();
			FileUtil.createFolder(REGEXNER_ADDRESS);
			for(Entry<Category, Set<String>> e:dictionaries.entrySet()) {
				final List<String> result = new ArrayList<>();
				for(String role:e.getValue()) {
					result.add(role.trim()+"\t"+e.getKey()+"\t\t1");
				}
				FileUtil.writeDataToFile(result, REGEXNER_ADDRESS+File.separator+"regexNer"+e.getKey()+".txt", false);
			}
			
			final RoleListProvider roleProvider= DictionaryRegexPatterns.getDictionaries();
			final List<String> result = new ArrayList<>();
			for(final String role:roleProvider.getData().keySet()) {
				result.add(role.trim()+"\tROLE\t\t1");
			}
			
			FileUtil.writeDataToFile(result, REGEXNER_ADDRESS+File.separator+"regexNer.txt", false);
		};
	}

}
