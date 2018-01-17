package ise.roletagger.entitygenerator;

import ise.roletagger.dictionarygenerator.DictionaryGenerator;
import ise.roletagger.util.Config;
import ise.roletagger.util.FileUtil;

/**
 * This class generates entities which can be used by {@link DictionaryGenerator} for generating dictionaries
 * Based on slide 3 of "RoleTagger Meeting 06.10.2017" google presentation, we only need "list of persons" and "list of titles" from
 * wikidata
 * 
 * Moreover, this class generate Wikidata Label dictionary
 *  
 * @author fbm
 *
 */
public class RunRequest {

	private static final String ADDRESS_OF_WIKIDATA_TITLE_ENTITIES = Config.getString("ADDRESS_OF_WIKIDATA_TITLE_ENTITIES","");
	private static final String ADDRESS_OF_DICTIONARY_FOLDER = Config.getString("ADDRESS_OF_DICTIONARY_FOLDER","")+"wikidatalabels";
			
	public static void main(String[] args) {
		Thread t = new Thread(run());
		t.setDaemon(false);
		t.start();
	}

	public static Runnable run() {
		return () -> {
			
			FileUtil.deleteFolder(ADDRESS_OF_WIKIDATA_TITLE_ENTITIES);
			FileUtil.deleteFolder(ADDRESS_OF_DICTIONARY_FOLDER);
			
			final Request managerRequest = new Request();
			managerRequest.setQuery(
					"SELECT%20%20DISTINCT%20%3FroleLabel%20%3FroleAltLabel%20%20%3FclassLabel%20%3FclassAltLabel%20%3FroleWikipediaLink%20WHERE%20%7B%20%20%20%20%0A%20%0A%20%20%3Frole%20wdt%3AP31%2a%7Cwdt%3AP279%2a%20wd%3AQ25713832%20.%0A%20%20wd%3AQ25713832%20rdfs%3Alabel%20%3FLEADER_OF_ORGANIZATION%20.%0A%20%20%3Frole%20rdfs%3Alabel%20%3FroleLabel.%0A%20%20%0A%20%20%3Frole%20wdt%3AP279%20%3Fclass%20.%0A%20%20%3Fclass%20rdfs%3Alabel%20%3FclassLabel%20.%0A%20%20%0A%20%20OPTIONAL%7B%3Frole%20skos%3AaltLabel%2a%20%3FroleAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FroleAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20OPTIONAL%7B%3Fclass%20skos%3AaltLabel%2a%20%3FclassAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FclassAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%20%20%0A%20%0A%20%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20FILTER%20%28%3FclassLabel%3D%3FLEADER_OF_ORGANIZATION%29%0A%20%20filter%20langMatches%28lang%28%3FroleLabel%29%2C%22en%22%29%0A%20%20filter%20langMatches%28lang%28%3FclassLabel%29%2C%22en%22%29%0A%20%20filter%20langMatches%28lang%28%3FclassAltLabel%29%2C%22en%22%29%0A%20%20%0A%7D%20ORDER%20BY%20%3FroleLabel");
			managerRequest.setDataFormat(DataForomat.JSON);
			String result = Caller.run(managerRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintMapStatistic(ADDRESS_OF_DICTIONARY_FOLDER,"ceo");
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_TITLE_ENTITIES,"ceo");

			
			final Request presidentRequest = new Request();
			presidentRequest.setQuery(
					"SELECT%20%20DISTINCT%20%3FroleLabel%20%3FroleAltLabel%20%3FclassLabel%20%3FclassAltLabel%20%3FroleWikipediaLink%20WHERE%20%7B%0A%20%20%3Frole%20wdt%3AP31%2a%7Cwdt%3AP279%2a%20wd%3AQ30461%20.%0A%20%20%0A%20%20%3Frole%20wdt%3AP279%20%3Fclass%20.%0A%20%20%3Fclass%20rdfs%3Alabel%20%3FclassLabel%20.%0A%20%20%0A%20%20OPTIONAL%7B%3Frole%20skos%3AaltLabel%20%3FroleAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FroleAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20OPTIONAL%7B%3Fclass%20skos%3AaltLabel%20%3FclassAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FclassAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20%0A%20%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20%0A%20%20%3Frole%20rdfs%3Alabel%20%3FroleLabel%20.%0A%20%20filter%20langMatches%28lang%28%3FroleLabel%29%2C%22en%22%29%20%20%0A%20%20filter%20langMatches%28lang%28%3FclassLabel%29%2C%22en%22%29%20%20%0A%7D");
			presidentRequest.setDataFormat(DataForomat.JSON);
			result = Caller.run(presidentRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintMapStatistic(ADDRESS_OF_DICTIONARY_FOLDER,"president");
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_TITLE_ENTITIES,"president");
			
			
			/**
			 * Chancellor result appended to President file
			 */
			final Request chancellorRequest = new Request();
			chancellorRequest.setQuery(
					"SELECT%20%20DISTINCT%20%3FroleLabel%20%3FroleAltLabel%20%3FclassLabel%20%3FclassAltLabel%20%3FroleWikipediaLink%20WHERE%20%7B%0A%20%20%3Frole%20wdt%3AP31%2a%7Cwdt%3AP279%2a%20wd%3AQ373085%20.%0A%20%20%0A%20%20%3Frole%20wdt%3AP279%20%3Fclass%20.%0A%20%20%3Fclass%20rdfs%3Alabel%20%3FclassLabel%20.%0A%20%20%0A%20%20OPTIONAL%7B%3Frole%20skos%3AaltLabel%20%3FroleAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FroleAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20OPTIONAL%7B%3Fclass%20skos%3AaltLabel%20%3FclassAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FclassAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20%0A%20%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20%0A%20%20%3Frole%20rdfs%3Alabel%20%3FroleLabel%20.%0A%20%20filter%20langMatches%28lang%28%3FroleLabel%29%2C%22en%22%29%20%20%0A%20%20filter%20langMatches%28lang%28%3FclassLabel%29%2C%22en%22%29%20%20%0A%7D");
			chancellorRequest.setDataFormat(DataForomat.JSON);
			result = Caller.run(chancellorRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintMapStatistic(ADDRESS_OF_DICTIONARY_FOLDER,"president");
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_TITLE_ENTITIES,"president");
			
			final Request popeRequest = new Request();
			popeRequest.setQuery(
					"SELECT%20%20DISTINCT%20%3FroleLabel%20%3FroleAltLabel%20%3FclassLabel%20%3FclassAltLabel%20%3FroleWikipediaLink%20WHERE%20%7B%0A%20%20%3Frole%20wdt%3AP31%2a%7Cwdt%3AP279%2a%20wd%3AQ19546%20.%0A%20%20%0A%20%20%3Frole%20wdt%3AP279%20%3Fclass%20.%0A%20%20%3Fclass%20rdfs%3Alabel%20%3FclassLabel%20.%0A%20%20%0A%20%20OPTIONAL%7B%3Frole%20skos%3AaltLabel%20%3FroleAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FroleAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20OPTIONAL%7B%3Fclass%20skos%3AaltLabel%20%3FclassAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FclassAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20%0A%20%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20%0A%20%20%3Frole%20rdfs%3Alabel%20%3FroleLabel%20.%0A%20%20filter%20langMatches%28lang%28%3FroleLabel%29%2C%22en%22%29%20%20%0A%20%20filter%20langMatches%28lang%28%3FclassLabel%29%2C%22en%22%29%20%20%0A%7D");
			popeRequest.setDataFormat(DataForomat.JSON);
			result = Caller.run(popeRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintMapStatistic(ADDRESS_OF_DICTIONARY_FOLDER,"pope");
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_TITLE_ENTITIES,"pope");
			
			final Request kingRequest = new Request();
			kingRequest.setQuery(
					"SELECT%20%20DISTINCT%20%3FroleLabel%20%3FroleAltLabel%20%3FclassLabel%20%3FclassAltLabel%20%3FroleWikipediaLink%20WHERE%20%7B%0A%20%20%3Frole%20wdt%3AP31%2a%7Cwdt%3AP279%2a%20wd%3AQ116%20.%0A%20%20%0A%20%20%3Frole%20wdt%3AP279%20%3Fclass%20.%0A%20%20%3Fclass%20rdfs%3Alabel%20%3FclassLabel%20.%0A%20%20%0A%20%20OPTIONAL%7B%3Frole%20skos%3AaltLabel%20%3FroleAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FroleAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20OPTIONAL%7B%3Fclass%20skos%3AaltLabel%20%3FclassAltLabel%0A%20%20%20%20%20%20%20%20%20%20FILTER%20%28LANG%28%3FclassAltLabel%29%3D%22en%22%29%20.%0A%20%20%20%20%20%20%20%20%20%20%7D%20%23position%20has%20an%20alias%0A%20%20%0A%20%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20%20FILTER%20%28STR%28%3FroleWikipediaLink%29%21%3D%22https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FPope%22%29%0A%20%20%0A%20%20%0A%20%20%3Frole%20rdfs%3Alabel%20%3FroleLabel%20.%0A%20%20filter%20langMatches%28lang%28%3FroleLabel%29%2C%22en%22%29%20%20%0A%20%20filter%20langMatches%28lang%28%3FclassLabel%29%2C%22en%22%29%20%20%0A%7D");
			kingRequest.setDataFormat(DataForomat.JSON);
			result = Caller.run(kingRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintMapStatistic(ADDRESS_OF_DICTIONARY_FOLDER,"king");
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_TITLE_ENTITIES,"king");
		};
	}
}
