package ise.roletagger.categorytreegeneration;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ise.roletagger.model.Category;
import ise.roletagger.util.Config;
import ise.roletagger.util.FileUtil;

public class CategoryTreePostProccessing {

	private final String CATEGORY_TREE_FOLDER = Config.getString("CATEGORY_TREE_FOLDER","");
	private final String CATEGORY_TREE_FOLDER_CLEAN = Config.getString("CATEGORY_TREE_FOLDER_CLEAN","");

	/**
	 * Used to know mapping between categorytrees
	 * For example, Head_of_sate, Head_of_government,... are under president
	 * currently it should be provide manually
	 */
	private final Map<String, String> cateoryTreesMap = new HashMap<>();

	public CategoryTreePostProccessing() {
		cateoryTreesMap.put("Government",Category.HEAD_OF_STATE_TAG.text());
		cateoryTreesMap.put("Heads_of_government",Category.HEAD_OF_STATE_TAG.text());
		cateoryTreesMap.put("Heads_of_state",Category.HEAD_OF_STATE_TAG.text());
		cateoryTreesMap.put("Chancellors_(government)",Category.HEAD_OF_STATE_TAG.text());

		cateoryTreesMap.put("Bishops",Category.POPE_TAG.text());
		cateoryTreesMap.put("Popes",Category.POPE_TAG.text());
		cateoryTreesMap.put("Dioceses",Category.POPE_TAG.text());

		cateoryTreesMap.put("Chief_executive_officers",Category.CHAIR_PERSON_TAG.text());

		cateoryTreesMap.put("Monarchy",Category.MONARCH_TAG.text());
	}

	public Runnable run() {
		return () -> {
			try {
				final File[] listOfFolders = new File(CATEGORY_TREE_FOLDER).listFiles();
				for (int i = 0; i < listOfFolders.length; i++) {
					final String file = listOfFolders[i].getName();
					final String catFileName = cateoryTreesMap.get(file);
					final List<String> result = new ArrayList<>();
					final List<String> lines = Files.readAllLines(Paths.get(CATEGORY_TREE_FOLDER+File.separator+file), StandardCharsets.UTF_8);
					for(String s:lines) {
						result.add(s);
					}
					FileUtil.createFolder(CATEGORY_TREE_FOLDER_CLEAN);
					FileUtil.writeDataToFile(result, CATEGORY_TREE_FOLDER_CLEAN+File.separator+catFileName, true);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		};
	}
}
