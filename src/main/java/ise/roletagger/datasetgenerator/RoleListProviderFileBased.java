package ise.roletagger.datasetgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import ise.roletagger.model.Category;
import ise.roletagger.model.DataSourceType;
import ise.roletagger.model.Order;
import ise.roletagger.model.RoleListProvider;

public class RoleListProviderFileBased extends RoleListProvider {

	/**
	 * This is the dictionary which already exist.
	 * It is used for selecting negative examples for dataset in {@link DatasetGenerator}
	 * 
	 */
	private static final String DATA_FOLDER = "./data/dictionary/";

	/**
	 * 
	 * @param normal which dictionary to use
	 * the normal dictionary or pure anchortext dictionary
	 * true means normal
	 * false means anchortext
	 */
	public RoleListProviderFileBased() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see main.RoleListProvider#loadRoles()
	 */
	@Override
	public void loadRoles(DataSourceType dataSourceType) {
		String dataSubFolder;
			switch (dataSourceType) {
			case WIKIDATA_LIST_OF_PRESON:
				dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIDATA_LIST_OF_PRESON.getText();
				break;
			case WIKIDATA_LABEL:
				dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIDATA_LABEL.getText();;
				break;
			case WIKIPEDIA_LIST_OF_PERSON_MANUAL:
				dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL.getText();;
				break;
			case WIKIPEDIA_LIST_OF_TILTES:
				dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.WIKIPEDIA_LIST_OF_TILTES.getText();;
				break;
			case ALL:
				dataSubFolder = DATA_FOLDER+File.separator + DataSourceType.ALL.getText();
				break;
			default:
				dataSubFolder = DATA_FOLDER+File.separator + "wikidataListOfMonarchs";
				break;
			}
		try {
			final File[] listOfFiles = new File(dataSubFolder).listFiles();
			for (int j = 0; j < listOfFiles.length; j++) {
				final String file = listOfFiles[j].getName();
				BufferedReader br = new BufferedReader(new FileReader(dataSubFolder + File.separator + file));
				String line;
				while ((line = br.readLine()) != null) {
					/**
					 * This part is just for test
					 */
					line = line.toLowerCase();
					/***/
					final Set<Category> categorySet = roleMap.get(line);
					final Category cat = Category.resolve(file);
					if (categorySet == null || categorySet.isEmpty()) {
						final Set<Category> catSet = new HashSet<>();
						catSet.add(cat);
						roleMap.put(line, catSet);
					} else {
						categorySet.add(cat);
						roleMap.put(line, categorySet);
					}
					
					final LinkedHashSet<String> set = inverseRoleMap.get(cat);
					if(set==null || set.isEmpty()) {
						final LinkedHashSet<String> s = new LinkedHashSet<>();
						s.add(line);
						inverseRoleMap.put(cat,s);
					}else {
						set.add(line);
						inverseRoleMap.put(cat,set);
					}
					
					if(line.split(" ").length==1) {
						final LinkedHashSet<String> set2 = headRoleMap.get(cat);
						if(set2==null || set2.isEmpty()) {
							final LinkedHashSet<String> s = new LinkedHashSet<>();
							s.add(line.trim());
							headRoleMap.put(cat,s);
						}else {
							set2.add(line.trim());
							headRoleMap.put(cat,set2);
						}
					}
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sortRoleMapBasedOnLenghth(Order.DESC);
		sortInverseDictionaryBasedOnLenghth(Order.DESC);
		sortHeadRoleMapBasedOnLenghth(Order.DESC);
	}
}
