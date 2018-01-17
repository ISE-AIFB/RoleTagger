package ise.roletagger.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import ise.roletagger.model.Category;
import ise.roletagger.model.DataSourceType;
import ise.roletagger.model.Entity;


public class EntityFileLoader {
	private static final String ENTITY_FOLDER_NAME = Config.getString("ADDRESS_OF_ENTITIES","");

	/**
	 * 
	 * @param dataSourceType
	 * @param entityDataSourceCategory
	 *            should be null if we want to read all the files
	 * @return
	 */
	public static Map<String, Entity> loadData(DataSourceType dataSourceType, Category entityDataSourceCategory) {
		String dataSubFolder;
		final Map<String, Entity> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		switch (dataSourceType) {
		case WIKIDATA_LIST_OF_PRESON:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + DataSourceType.WIKIDATA_LIST_OF_PRESON.getText();
			break;
		case WIKIDATA_LABEL:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + DataSourceType.WIKIDATA_LABEL.getText();
			break;
		case WIKIPEDIA_LIST_OF_PERSON_MANUAL:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator
					+ DataSourceType.WIKIPEDIA_LIST_OF_PERSON_MANUAL.getText();
			break;
		case WIKIPEDIA_LIST_OF_TILTES:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + DataSourceType.WIKIPEDIA_LIST_OF_TILTES.getText();
			break;
		case ALL:
			dataSubFolder = ENTITY_FOLDER_NAME + File.separator + DataSourceType.ALL.getText();
			break;
		default:
			throw new IllegalArgumentException("At least one data source should be seletced");
		}

		if (entityDataSourceCategory == null) {
			final File[] listOfFiles = new File(dataSubFolder).listFiles();

			try {
				for (int i = 0; i < listOfFiles.length; i++) {
					final String fileName = listOfFiles[i].getName();
					final BufferedReader br = new BufferedReader(
							new FileReader(dataSubFolder + File.separator + fileName));
					String entityName;

					while ((entityName = br.readLine()) != null) {
						if (entityName == null || entityName.isEmpty()) {
							continue;
						}
						final String[] data = entityName.split(";");
						map.put(data[2],new Entity(data[0], data[1], data[2], Category.resolve(fileName)));
					}
					br.close();
				}
			} catch (final IOException exception) {
				exception.printStackTrace(); 
			}
			return map;
		} else {
			dataSubFolder = dataSubFolder + File.separator + entityDataSourceCategory.text();
			final String fileName = dataSubFolder;
			Category resolve = Category.resolve(entityDataSourceCategory.text());
			try {
				final BufferedReader br = new BufferedReader(new FileReader(fileName));
				String entityName;

				while ((entityName = br.readLine()) != null) {
					if (entityName == null || entityName.isEmpty()) {
						continue;
					}
					final String[] data = entityName.split(";");
					map.put(data[2], new Entity(data[0], data[1], data[2], resolve));
				}
				br.close();
			} catch (final IOException exception) {
				exception.printStackTrace();
			}
			return map;
		}
	}
}
