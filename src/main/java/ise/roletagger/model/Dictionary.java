package ise.roletagger.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import ise.roletagger.util.FileUtil;
import ise.roletagger.util.MapUtil;
import ise.roletagger.util.MyStanfordCoreNLP;
import ise.roletagger.util.StatisticalFunctions;

public class Dictionary {

	private static final String RESULT_FILE_SEPARATOR = "\t";
	private static final Logger LOG = Logger.getLogger(Dictionary.class.getCanonicalName());
	private final ConcurrentHashMap<AnchorText, Map<String, MapEntity>> dictionary = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<AnchorText, Long> dictionaryKeyFrequency = new ConcurrentHashMap<>();
	private ConcurrentHashMap<AnchorText, Map<String, Double>> dictionaryValueClustringCoefficientMap = new ConcurrentHashMap<>();

	public void merge(final AnchorText anchorText, final Entity entity) {
		if (anchorText == null || entity == null) {
			throw new IllegalArgumentException();
		}

		final MapEntity mapEntity = new MapEntity(entity);

		Map<String, MapEntity> dicElement = dictionary.get(anchorText);
		if (dicElement == null) {
			final Map<String, MapEntity> map = new ConcurrentHashMap<>();
			map.put(mapEntity.getEntity().getUri(), mapEntity);
			dictionary.put(anchorText, map);
			dictionaryKeyFrequency.put(anchorText, anchorText.getFrequency());
		} else {
			MapEntity mapElement = dicElement.get(entity.getUri());
			if (mapElement == null) {
				dicElement.put(entity.getUri(), mapEntity);
			} else {
				mapElement.increment();
			}
		}

		dictionaryKeyFrequency.put(anchorText,
				dictionaryKeyFrequency.containsKey(anchorText)
						? (dictionaryKeyFrequency.get(anchorText).longValue() + 1)
						: 1);

	}

	/**
	 * print the dictionary to a log file
	 * the format:
	 * - Anchor Text
	 * - Frequency of anchor text
	 * - Number of entities this ancho text refers to
	 * - list of entities this anchor text refers to
	 */
	public void printResult() {
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			StringBuilder result = new StringBuilder();
			result.append(entry.getKey().getAnchorText()).append(RESULT_FILE_SEPARATOR)
					.append(dictionaryKeyFrequency.get(entry.getKey())).append(RESULT_FILE_SEPARATOR);
			result.append(entry.getValue().size()).append(RESULT_FILE_SEPARATOR);
			for (MapEntity mapEntity : entry.getValue().values()) {
				result.append(mapEntity.getEntity().getEntityName()).append(RESULT_FILE_SEPARATOR)
						.append(mapEntity.getFrequency()).append(RESULT_FILE_SEPARATOR);
			}
			LOG.info(result.toString());
		}
	}

	/**
	 * Print dictionary to log file
	 * format:
	 * - Anchor Text
	 * - Category
	 */
	public void printResultByCategory() {
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			final List<Category> alreadySeen = new ArrayList<>();
			for (MapEntity mapEntity : entry.getValue().values()) {
				StringBuilder result = new StringBuilder();
				if (!alreadySeen.contains(mapEntity.getEntity().getCategoryFolder())) {
					alreadySeen.add(mapEntity.getEntity().getCategoryFolder());
					result.append(entry.getKey().getAnchorText()).append(RESULT_FILE_SEPARATOR)
							.append(mapEntity.getEntity().getCategoryFolder());
					LOG.info(result.toString());
				}
			}
		}
	}

	public void printResultWithoutEntitesWithClustringCoefficient() {
		dictionaryValueClustringCoefficientMap = calculateClustringCoefficient();
		LOG.info(
				"entity;frequency;size;hueristic(size*frequency*clusting coefficient);cluster;frequency;cluster;frequency;cluster;frequency");
		double heuristicValue = 0;
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			StringBuilder result = new StringBuilder();
			result.append(entry.getKey().getAnchorText()).append(RESULT_FILE_SEPARATOR)
					.append(dictionaryKeyFrequency.get(entry.getKey())).append(RESULT_FILE_SEPARATOR);
			final Map<String, Double> map = dictionaryValueClustringCoefficientMap.get(entry.getKey());
			heuristicValue = dictionaryKeyFrequency.get(entry.getKey())
					* StatisticalFunctions.sigmoid(map.values().stream().findFirst().get()) * entry.getValue().size();

			result.append(entry.getValue().size()).append(RESULT_FILE_SEPARATOR).append(heuristicValue)
					.append(RESULT_FILE_SEPARATOR);

			for (Entry<String, Double> coefficientEntry : map.entrySet()) {
				result.append(coefficientEntry.getKey()).append(RESULT_FILE_SEPARATOR)
						.append(coefficientEntry.getValue()).append(RESULT_FILE_SEPARATOR);

			}
			LOG.info(result.toString());
		}
	}

	public void printToXLS() {
		final Workbook wb = new HSSFWorkbook();
		final Sheet sheet = wb.createSheet("new sheet");
		int startRow = 0;
		int endRow = 0;
		int columnNumber = 0;
		int rowNumber = 0;
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			final Row row = sheet.createRow((short) rowNumber);
			Cell cell = row.createCell((short) columnNumber++);
			cell.setCellValue(entry.getKey().getAnchorText());

			cell = row.createCell((short) columnNumber++);
			cell.setCellValue(dictionaryKeyFrequency.get(entry.getKey()));

			cell = row.createCell((short) columnNumber++);
			cell.setCellValue(entry.getValue().size());

			int innerRowNumer = new Integer(rowNumber).intValue() + 1;
			startRow = innerRowNumer - 1;
			endRow = startRow;

			boolean firstTime = true;
			for (final Entry<String, MapEntity> mapEntity : entry.getValue().entrySet()) {
				if (firstTime) {
					row.createCell((short) columnNumber).setCellValue(mapEntity.getValue().getEntity().getEntityName());
					row.createCell((short) columnNumber + 1).setCellValue(mapEntity.getValue().getFrequency());
					row.createCell((short) columnNumber + 2)
							.setCellValue(mapEntity.getValue().getEntity().getCategoryFolder().text());
					firstTime = false;
				} else {
					int innerColumnNumber = new Integer(columnNumber).intValue();
					final Row innerRow = sheet.createRow((short) innerRowNumer++);
					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getValue().getEntity().getEntityName());

					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getValue().getFrequency());

					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getValue().getEntity().getCategoryFolder().text());
					endRow++;
				}
			}
			if (startRow < endRow) {
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0, 0));
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 1, 1));
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 2, 2));
			}
			rowNumber = endRow + 1;
			startRow = endRow + 1;
			columnNumber = 0;
		}

		// Write the output to a file
		final FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("./log/workbook.xls");
			wb.write(fileOut);
			fileOut.close();
			wb.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printResultLineByLineByMerge(final boolean removeNerTags) {
		final Map<String, Set<String>> newPrintMapForSperataeFiles = new HashMap<>();
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			boolean firstline = true;
			StringBuilder result = new StringBuilder();
			for (Entry<String, MapEntity> mapEntity : entry.getValue().entrySet()) {
				if (firstline) {
					result.append(entry.getKey().getAnchorText()).append(RESULT_FILE_SEPARATOR)
							.append(dictionaryKeyFrequency.get(entry.getKey())).append(RESULT_FILE_SEPARATOR)
							.append(entry.getValue().size()).append(RESULT_FILE_SEPARATOR);
					firstline = false;
				} else {
					result.append(";;;");
				}
				// result.append(URLUTF8Encoder.unescape(mapEntity.getEntity().getUri())).append(";").append(mapEntity.getFrequency());
				result.append(mapEntity.getValue().getEntity().getEntityName()).append(RESULT_FILE_SEPARATOR)
						.append(mapEntity.getValue().getFrequency()).append(RESULT_FILE_SEPARATOR);
				result.append(mapEntity.getValue().getEntity().getCategoryFolder());
				addToMap(newPrintMapForSperataeFiles, mapEntity.getValue().getEntity().getCategoryFolder().text(),
						entry.getKey().getAnchorText());
				LOG.info(result.toString());
				result = new StringBuilder();
			}
		}
		if (removeNerTags) {
			for (Entry<String, Set<String>> a : newPrintMapForSperataeFiles.entrySet()) {
				try {
					PrintWriter writer = new PrintWriter("log" + File.separator + a.getKey(), "UTF-8");
					for (String s : a.getValue()) {
						if (s.isEmpty()) {
							continue;
						}
						final String ner = MyStanfordCoreNLP.runNerTaggerString(s);
						if (ner.charAt(0) == '<' && ner.charAt(ner.length() - 1) == '>') {
							continue;
						}
						writer.println(s);
					}
					writer.close();
				} catch (IOException e) {
				}
			}
		} else {
			for (Entry<String, Set<String>> a : newPrintMapForSperataeFiles.entrySet()) {
				try {
					PrintWriter writer = new PrintWriter("log" + File.separator + a.getKey(), "UTF-8");
					for (String s : a.getValue()) {
						if (s.isEmpty()) {
							continue;
						}
						writer.println(s);
					}
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void addToMap(Map<String, Set<String>> map, String categoryFolder, String anchorText) {
		final Set<String> value = map.get(categoryFolder);
		if (value == null) {
			final Set<String> set = new HashSet<>();
			set.add(anchorText);
			map.put(categoryFolder, set);
		} else {
			final Set<String> set = new HashSet<>(value);
			set.add(anchorText);
			map.put(categoryFolder, set);
		}
	}

	public void printResultLineByLine() {
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			StringBuilder result = new StringBuilder();
			for (Entry<String, MapEntity> mapEntity : entry.getValue().entrySet()) {
				result.append(entry.getKey().getAnchorText()).append(RESULT_FILE_SEPARATOR)
						.append(dictionaryKeyFrequency.get(entry.getKey())).append(RESULT_FILE_SEPARATOR)
						.append(entry.getValue().size()).append(RESULT_FILE_SEPARATOR);
				result.append(mapEntity.getValue().getEntity().getUri()).append(RESULT_FILE_SEPARATOR)
						.append(mapEntity.getValue().getFrequency()).append(RESULT_FILE_SEPARATOR);
				LOG.info(result.toString());
				result = new StringBuilder();
			}
		}
	}

	@Override
	public String toString() {
		return "Dictionary [dic=" + dictionary + "]";
	}

	public int size() {
		return dictionary.size();
	}

	private ConcurrentHashMap<AnchorText, Map<String, Double>> calculateClustringCoefficient() {
		final ConcurrentHashMap<AnchorText, Map<String, Double>> clustringCoefficientMap = new ConcurrentHashMap<>();
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			final AnchorText anchorText = entry.getKey();
			final Map<String, Double> coefficientsMap = calculateCoefficient(
					entry.getValue().values().stream().map(p -> p.getEntity()).collect(Collectors.toList()));
			clustringCoefficientMap.put(anchorText, coefficientsMap);
		}
		return clustringCoefficientMap;
	}

	private Map<String, Double> calculateCoefficient(List<Entity> entities) {
		Map<String, Double> coefficientsMap = new ConcurrentHashMap<>();
		final int size = entities.size();
		for (Entity entity : entities) {
			final String key = entity.getCategoryFolder().text();
			final Double coefficient = coefficientsMap.get(key);
			if (coefficient == null) {
				coefficientsMap.put(key, 1.);
			} else {
				coefficientsMap.put(key, coefficient.doubleValue() + 1);
			}
		}
		// Normalization section
		coefficientsMap = coefficientsMap.entrySet().stream()
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue() / size));
		return MapUtil.sortByValueDescending(coefficientsMap);
	}
	
	/**
	 * Writes dictionary content to separate files
	 * These files can be used for the other part of the code (if we have pipe line)
	 * These files will be the final version of the automatic dictionary
	 * However, these files need manual cleaning
	 */
	public void printDictioanryToFilesBasedOnCategories(String path) {
		FileUtil.createFolder(path);
		final Map<Category,Set<String>> dic = new HashMap<>();
		
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dictionary.entrySet()) {
			final List<Category> alreadySeen = new ArrayList<>();
			for (MapEntity mapEntity : entry.getValue().values()) {
				final Category categoryFolder = mapEntity.getEntity().getCategoryFolder();
				if (!alreadySeen.contains(categoryFolder)) {
					alreadySeen.add(categoryFolder);
					final String entityName = mapEntity.getEntity().getEntityName();
					final String anchorText = entry.getKey().getAnchorText();
					
					final Set<String> set = dic.get(categoryFolder);
					if(set==null) {
						final Set<String> newSet = new HashSet<>();
						newSet.add(anchorText);
						newSet.add(entityName.replace("_"," "));
						dic.put(categoryFolder,newSet);
					}else {
						set.add(anchorText);
						set.add(entityName.replace("_"," "));
						dic.put(categoryFolder,set);
					}
				}
			}
		}
		
		for(Entry<Category, Set<String>> entry:dic.entrySet()) {
			FileUtil.writeDataToFile(new ArrayList<String>(entry.getValue()), path+File.separator+entry.getKey().text(), false);
		}
	}
}
