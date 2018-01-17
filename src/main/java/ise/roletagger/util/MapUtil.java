package ise.roletagger.util;

import java.util.*;

public class MapUtil {

	/**
	 * Sort a Map by value in descending order
	 ** 
	 * @param map
	 * @return a sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sort a Map by key in asscending order
	 ** 
	 * @param roleMap
	 * @return a sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<String, Set<V>> sortByKeyAscending(
			Map<String, Set<V>> roleMap) {
		List<Map.Entry<String, Set<V>>> list = new LinkedList<Map.Entry<String, Set<V>>>(roleMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Set<V>>>() {
			public int compare(Map.Entry<String, Set<V>> o1, Map.Entry<String, Set<V>> o2) {
				return (o1.getKey().length() - o2.getKey().length());
			}
		});

		LinkedHashMap<String, Set<V>> result = new LinkedHashMap<String, Set<V>>();
		for (Map.Entry<String, Set<V>> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sort a Map by key in descending order
	 ** 
	 * @param map
	 * @return a sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<String, Set<V>> sortByKeyDescending(
			Map<String, Set<V>> map) {
		List<Map.Entry<String, Set<V>>> list = new LinkedList<Map.Entry<String, Set<V>>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Set<V>>>() {
			public int compare(Map.Entry<String, Set<V>> o1, Map.Entry<String, Set<V>> o2) {
				return (o2.getKey().length() - o1.getKey().length());
			}
		});

		LinkedHashMap<String, Set<V>> result = new LinkedHashMap<String, Set<V>>();
		for (Map.Entry<String, Set<V>> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Sort a Map by value in ascending order
	 ** 
	 * @param unsortMap
	 * @return a sorted map
	 */
	public static Map<Integer, Float> sortByValueAscending(final Map<Integer, Float> unsortMap) {
		// Convert Map to List
		final List<Map.Entry<Integer, Float>> list = new LinkedList<Map.Entry<Integer, Float>>(unsortMap.entrySet());
		// Sort list with comparator, to compare the Map values
		final Comparator<Map.Entry<Integer, Float>> comparator = new Comparator<Map.Entry<Integer, Float>>() {
			public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
				if (o1.getValue().floatValue() > o2.getValue().floatValue()) {
					return 1;
				} else if (o1.getValue().floatValue() < o2.getValue().floatValue()) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Collections.sort(list, comparator);
		// Convert sorted map back to a Map
		Map<Integer, Float> sortedMap = new LinkedHashMap<Integer, Float>();
		for (Iterator<Map.Entry<Integer, Float>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Integer, Float> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	/**
	 * Sort a Map by key in descending order by considering number of words
	 ** 
	 * @param map
	 * @return a sorted map
	 */
	public static <K, V extends Comparable<? super V>> Map<String, Set<V>> sortByKeyDescendingNumberOfWords(
			Map<String, Set<V>> map) {
		List<Map.Entry<String, Set<V>>> list = new LinkedList<Map.Entry<String, Set<V>>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Set<V>>>() {
			public int compare(Map.Entry<String, Set<V>> o1, Map.Entry<String, Set<V>> o2) {
				final int o2Size = o2.getKey().split(" ").length;
				final int o1Size = o1.getKey().split(" ").length;
				
				return (o2Size - o1Size);
			}
		});

		LinkedHashMap<String, Set<V>> result = new LinkedHashMap<String, Set<V>>();
		for (Map.Entry<String, Set<V>> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	/**
	 * Sort a Map by value in ascending order
	 ** 
	 * @param unsortMap
	 * @return a sorted map
	 */
	public static Map<String, Integer> sortByValueAscending2(final Map<String, Integer> unsortMap) {
		// Convert Map to List
		final List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
		// Sort list with comparator, to compare the Map values
		final Comparator<Map.Entry<String, Integer>> comparator = new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				if (o1.getValue().intValue() > o2.getValue().intValue()) {
					return 1;
				} else if (o1.getValue().intValue() < o2.getValue().intValue()) {
					return -1;
				} else {
					return 0;
				}
			}
		};
		Collections.sort(list, comparator);
		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}