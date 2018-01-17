package ise.roletagger.entitygenerator;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ise.roletagger.util.FileUtil;

public class JsonParser {

	private static Map<String,Set<String>> labelMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static Map<String,Map<String,String>> linkMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public static void parseResultToMap(final String result){
		labelMap.clear();
		linkMap.clear();
		final JSONObject obj = new JSONObject(result);
		final JSONObject pageName = obj.getJSONObject("results");

		final JSONArray arr = pageName.getJSONArray("bindings");

		for (int i = 0; i < arr.length(); i++) {
			final String roleLabel = arr.getJSONObject(i).getJSONObject("roleLabel").getString("value");		
			final String classLabel = arr.getJSONObject(i).getJSONObject("classLabel").getString("value");
			final String roleWikipediaLink =  arr.getJSONObject(i).getJSONObject("roleWikipediaLink").getString("value");;

			String classAltLabel = "";
			String roleAltLabel = "";

			try{
				classAltLabel = arr.getJSONObject(i).getJSONObject("classAltLabel").getString("value");
				roleAltLabel = arr.getJSONObject(i).getJSONObject("roleAltLabel").getString("value");
			}catch (JSONException e) {				
			}

			addToRoleMap(roleLabel,classLabel,roleAltLabel,classAltLabel);
			addToLinkMap(roleLabel,classLabel,roleWikipediaLink);
		}

	}

	private static void addToLinkMap(String roleLabel, String classLabel, String roleWikipediaLink) {
		if(classLabel==null || classLabel.isEmpty() ||roleWikipediaLink==null || roleWikipediaLink.isEmpty() ){
			return;
		}
		final Map<String,String> value = linkMap.get(classLabel);
		if(value==null){
			final Map<String,String> map = new HashMap<>();
			map.put(roleLabel,roleWikipediaLink);
			linkMap.put(classLabel, map);
		}else{
			final Map<String,String> map = new HashMap<>(value);
			map.put(roleLabel,roleWikipediaLink);
			linkMap.put(classLabel, map);
		}
	}

	public static void printMapStatistic(final String folderName,final String fileName) {
		if(!FileUtil.createFolder(folderName)){
			System.err.println("Can not create folder or already exist "+folderName);
		}
		for(Entry<String, Set<String>> a:labelMap.entrySet()){
			FileUtil.writeDataToFile(new ArrayList<String>(a.getValue()), folderName+File.separator+fileName, true);
		}
	}
	
	/**
	 * Put all the roles for each specific topic in just one file
	 * @param folderName
	 */
	public static void aggegateAndPrintMapStatistic(String folderName,String fileName) {
		if(!FileUtil.createFolder(folderName)){
			System.err.println("Can not create folder or already exist "+folderName);
		}
		final Set<String> aggregationSet = new HashSet<>();
		for(Entry<String, Set<String>> a:labelMap.entrySet()){
			for(String s:a.getValue()){
				if(!s.isEmpty()){
					aggregationSet.add(s);
				}
			}			    
		}
		FileUtil.writeDataToFile(new ArrayList<String>(aggregationSet), folderName+File.separator+fileName, true);
	}

	private static void addToRoleMap(String roleLabel, String classLabel, String roleAltLabel, String classAltLabel) {
		if(classLabel==null || classLabel.isEmpty()){
			return;
		}
		final Set<String> value = labelMap.get(classLabel);
		if(value==null){
			final Set<String> set = new HashSet<>();
			set.add(roleLabel);
			set.add(roleAltLabel);
			//set.add(classAltLabel);
			labelMap.put(classLabel, set);
		}else{
			final Set<String> set = new HashSet<>(value);
			set.add(roleLabel);
			set.add(roleAltLabel);
			//set.add(classAltLabel);
			labelMap.put(classLabel, set);
		}
	}

	public static void printAnchorTextData(String folderName) {
		for(Entry<String, Map<String, String>> a:linkMap.entrySet()){
			try{
				PrintWriter writer = new PrintWriter(folderName+File.separator+a.getKey(), "UTF-8");
				for(Entry<String, String> s:a.getValue().entrySet()){
					writer.println(s.getKey()+";"+s.getValue()+";"+s.getKey().replaceAll(" ", "_"));
				}
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Put all the roles for each specific topic in just one file
	 * @param folderName
	 */
	public static void aggegateAndPrintAnchorTextData(String folderName,String fileName) {
		if(!FileUtil.createFolder(folderName)) {
			System.err.println("Can not create folder or already exist "+folderName);
		}
		final Set<String> aggregationSet = new HashSet<>();
		for(Entry<String, Map<String, String>> a:linkMap.entrySet()){
			for(Entry<String, String> s:a.getValue().entrySet()){
				final StringBuilder result = new StringBuilder();
				String replaceAll = s.getValue().substring(s.getValue().lastIndexOf("/")+1);
				result.append(s.getKey()).append(";").append(s.getValue()).append(";").append(replaceAll);
				aggregationSet.add(result.toString());
			}			    
		}
		
		FileUtil.writeDataToFile(new ArrayList<String>(aggregationSet), folderName+File.separator+fileName, true);
	}

}
