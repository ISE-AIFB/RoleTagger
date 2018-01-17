package ise.roletagger.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ise.roletagger.util.MapUtil;
/**
 * Abstract class which contains dictionaries + their inverse dictionaries
 * @author fbm
 *
 */
public abstract class RoleListProvider {
	private static Logger LOG = Logger.getLogger(RoleListProvider.class);
	protected Map<String, Set<Category>> roleMap = new LinkedHashMap<>();
	protected Map<Category, LinkedHashSet<String>> inverseRoleMap = new LinkedHashMap<>();
	protected Map<Category, LinkedHashSet<String>> headRoleMap = new LinkedHashMap<>();
	/**
	 * This function should first load the roles into the map, then sort the map
	 * in a descending mode regards to the length of the text and 
	 * (OPTIONAL FOR DEMO) then fill the colorutil
	 */
	public void loadRoles(DataSourceType dataSourceType) {
	};

	public void print() {
		for (String s : roleMap.keySet()) {
			LOG.info(s);
		}
	}

	public Map<String, Set<Category>> getData() {
		return roleMap;
	}
	
	public Map<Category, LinkedHashSet<String>> getInverseData() {
		return inverseRoleMap;
	}

	public Map<Category, LinkedHashSet<String>> getHeadRoleMap() {
		return headRoleMap;
	}
	
	protected void sortHeadRoleMapBasedOnLenghth(Order order) {
		switch (order) {
		case ASC:
			for(Entry<Category, LinkedHashSet<String>> entiry:headRoleMap.entrySet()) {
				final Category key = entiry.getKey();
				final List<String> list = new ArrayList<>(headRoleMap.get(key));
				Collections.sort((list), (s1, s2) -> s1.length()-s2.length());
				headRoleMap.put(key, new LinkedHashSet<>(list));
			}
			break;
		case DESC:
			for(Entry<Category, LinkedHashSet<String>> entiry:headRoleMap.entrySet()) {
				final Category key = entiry.getKey();
				final List<String> list = new ArrayList<>(headRoleMap.get(key));
				Collections.sort((list), (s1, s2) -> s2.length()-s1.length());
				headRoleMap.put(key, new LinkedHashSet<>(list));
			}
			break;
		default:
			throw new IllegalArgumentException(order + " is undefined");
		}
	}
	
	protected void sortRoleMapBasedOnLenghth(Order order) {
		switch (order) {
		case ASC:
			roleMap = MapUtil.sortByKeyAscending(roleMap);
			break;
		case DESC:
			roleMap = MapUtil.sortByKeyDescending(roleMap);
			break;
		default:
			throw new IllegalArgumentException(order + " is undefined");
		}
	}
	
	protected void sortInverseDictionaryBasedOnLenghth(Order order) {
		switch (order) {
		case ASC:
			for(Entry<Category, LinkedHashSet<String>> entiry:inverseRoleMap.entrySet()) {
				final Category key = entiry.getKey();
				final List<String> list = new ArrayList<>(inverseRoleMap.get(key));
				Collections.sort((list), (s1, s2) -> s1.length()-s2.length());
				inverseRoleMap.put(key, new LinkedHashSet<>(list));
			}
			break;
		case DESC:
			for(Entry<Category, LinkedHashSet<String>> entiry:inverseRoleMap.entrySet()) {
				final Category key = entiry.getKey();
				final List<String> list = new ArrayList<>(inverseRoleMap.get(key));
				Collections.sort((list), (s1, s2) -> s2.length()-s1.length());
				inverseRoleMap.put(key, new LinkedHashSet<>(list));
			}
			break;
		default:
			throw new IllegalArgumentException(order + " is undefined");
		}
	}
}
