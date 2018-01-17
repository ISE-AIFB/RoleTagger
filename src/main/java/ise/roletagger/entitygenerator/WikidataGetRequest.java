package ise.roletagger.entitygenerator;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.TreeMap;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//public class WikidataGetRequest {
//
//	/**
//	 * Mapping between main class (role class) and the roles
//	 */
//	private static Map<String,Set<String>> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
//	static {
//		System.setProperty("java.net.useSystemProxies", "true");
//	}
//
//	public static void main(String[] args) {
//
//		try {
//			//final String query = "https://query.wikidata.org/sparql?query=SELECT%20%20DISTINCT%20%3FtruePosition%20%3FtruePositionLabel%20%20%3FclassLabel%20%3FaltLabel%20%3Fclass%20WHERE%20%7B%20%20%20%20%0A%20%20%3FtruePosition%20wdt%3AP31%2a%7Cwdt%3AP279%2a%20wd%3AQ2462658%20.%0A%20%20%3FtruePosition%20rdfs%3Alabel%20%3FtruePositionLabel.%0A%20%20%0A%20%20%3FtruePosition%20wdt%3AP279%20%3Fclass%20.%0A%20%20%3Fclass%20rdfs%3Alabel%20%3FclassLabel%20.%0A%20%20%0A%20%20%3Fclass%20skos%3AaltLabel%20%3FaltLabel%20.%0A%20%20%0A%20%20filter%20langMatches%28lang%28%3FtruePositionLabel%29%2C%22en%22%29%0A%20%20filter%20langMatches%28lang%28%3FclassLabel%29%2C%22en%22%29%0A%20%20filter%20langMatches%28lang%28%3FaltLabel%29%2C%22en%22%29%0A%20%20%0A%7D%20ORDER%20BY%20%3FtruePositionLabel";
//	        final String query = "https://query.wikidata.org/sparql?query=SELECT%20%20DISTINCT%20%3FtruePosition%20%3FtruePositionLabel%20%3FaltLabel%20%3FclassLabel%20WHERE%20%7B%20%20%0A%20%20%0A%20%20%3FtruePosition%20wdt%3AP31%2a%7Cwdt%3AP279%2a%20wd%3AQ214339%20.%0A%20%20%0A%20%20%3FtruePosition%20wdt%3AP279%20%3Fclass%20.%0A%20%20%3Fclass%20rdfs%3Alabel%20%3FclassLabel%20.%0A%20%20%0A%20%20OPTIONAL%7B%3FtruePosition%20skos%3AaltLabel%2a%20%3FaltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FaltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20%0A%20%20%3FtruePosition%20rdfs%3Alabel%20%3FtruePositionLabel%20.%0A%20%20filter%20langMatches%28lang%28%3FtruePositionLabel%29%2C%22en%22%29%20%20%0A%20%20filter%20langMatches%28lang%28%3FclassLabel%29%2C%22en%22%29%20%20%0A%7D%0A%23GROUP%20BY%20%3FtruePosition%20%3FtruePositionLabel%20%3FclassLabel%0A%23LIMIT%20100";
//			
//
//			
//			System.err.println("Concatenated");
//			final JSONObject obj = new JSONObject(result.toString());
//			final JSONObject pageName = obj.getJSONObject("results");
//
//			final JSONArray arr = pageName.getJSONArray("bindings");
//			for (int i = 0; i < arr.length(); i++) {
//				final String role = arr.getJSONObject(i).getJSONObject("truePositionLabel").getString("value");
//				final String classLabel = arr.getJSONObject(i).getJSONObject("classLabel").getString("value");
//				final JSONObject jsonObject = arr.getJSONObject(i).getJSONObject("altLabel");
//				String altLabel ="";
//				if(jsonObject!=null){
//					altLabel = jsonObject.getString("value");
//				}
//				//System.err.println(role + "\t" + classLabel + "\t" + altLabel + "\t");
//				
//				addToMap(role,classLabel,altLabel);
//			}
//			conn.disconnect();
//			printMapStatistic();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static void printMapStatistic() {
//		System.err.println("numerb of category = "+map.size());
//		for(Entry<String, Set<String>> a:map.entrySet()){
//			try{
//			    PrintWriter writer = new PrintWriter("data2/"+a.getKey(), "UTF-8");
//			    for(String s:a.getValue()){
//			    	writer.println(s);
//			    }
//			    writer.close();
//			} catch (IOException e) {
//			   // do something
//			}
//			
//		}
//	}
//	
//	private static void addToMap(String role, String classLabel, String altLabel) {
//		final Set<String> value = map.get(classLabel);
//		if(value==null){
//			Set<String> set = new HashSet<>();
//			set.add(role);
//			set.add(altLabel);
//			map.put(classLabel, set);
//		}else{
//			Set<String> set = new HashSet<>(value);
//			set.add(role);
//			set.add(altLabel);
//			map.put(classLabel, set);
//		}
//	}
//}