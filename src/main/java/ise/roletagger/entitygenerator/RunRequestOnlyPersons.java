package ise.roletagger.entitygenerator;

import ise.roletagger.util.Config;
import ise.roletagger.util.FileUtil;

/**
 * This class is responsible just for finding all the persons in each category
 * (President, Monarch, CEO,..)
 * 
 * @author fbm
 *
 */
public class RunRequestOnlyPersons{

	private static final String ADDRESS_OF_WIKIDATA_PERSON_ENTITIES = Config.getString("ADDRESS_OF_WIKIDATA_PERSON_ENTITIES","");

	public static void main(String[] args) {
		final Thread t = new Thread(run());
		t.setDaemon(false);
		t.start();
	}

	public static Runnable run() {
		return () -> {
			
			FileUtil.deleteFolder(ADDRESS_OF_WIKIDATA_PERSON_ENTITIES);
			
			final Request presidentRequest = new Request();
			presidentRequest.setQuery(
					"SELECT%20DISTINCT%20%3FroleLabel%20%3FclassLabel%20%3FroleWikipediaLink%0AWHERE%20%0A%7B%0A%20%20%3Frole%20wdt%3AP31%20wd%3AQ5.%0A%20%20%3Frole%20wdt%3AP39%20%3Fclass%20.%0A%20%20%3Fclass%20wdt%3AP279%2a%20wd%3AQ30461%20.%0A%20%20%0A%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22%5BAUTO_LANGUAGE%5D%2Cen%22.%20%7D%0A%7D");
			presidentRequest.setDataFormat(DataForomat.JSON);
			String result = Caller.run(presidentRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_PERSON_ENTITIES,"president");

			final Request monarchRequestPersons = new Request();
			monarchRequestPersons.setQuery("SELECT%20DISTINCT%20%3FroleLabel%20%3FclassLabel%20%3FroleWikipediaLink%0AWHERE%20%0A%7B%0A%20%20%3Frole%20wdt%3AP31%20wd%3AQ5.%0A%20%20%3Frole%20wdt%3AP39%20%3Fclass%20.%0A%20%20%3Fclass%20wdt%3AP279%2a%20wd%3AQ116%20.%0A%20%20%0A%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22%5BAUTO_LANGUAGE%5D%2Cen%22.%20%7D%0A%20%20%0A%20%20FILTER%20NOT%20EXISTS%20%7B%3Fclass%20wdt%3AP279%2a%20wd%3AQ19546%7D%0A%7D");
			monarchRequestPersons.setDataFormat(DataForomat.JSON);
			result = Caller.run(monarchRequestPersons);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_PERSON_ENTITIES,"king");

			final Request popeRequest = new Request();
			popeRequest.setQuery(
					"SELECT%20DISTINCT%20%3FroleLabel%20%3FclassLabel%20%3FroleWikipediaLink%0AWHERE%20%0A%7B%0A%20%20%3Frole%20wdt%3AP31%20wd%3AQ5.%0A%20%20%3Frole%20wdt%3AP39%20%3Fclass%20.%0A%20%20%0A%20%20FILTER%20%28%3Fclass%3Dwd%3AQ19546%20%29.%0A%20%20%20%20%0A%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22%5BAUTO_LANGUAGE%5D%2Cen%22.%20%7D%0A%7D");
			popeRequest.setDataFormat(DataForomat.JSON);
			result = Caller.run(popeRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_PERSON_ENTITIES,"pope");

			final Request ceoRequest = new Request();
			ceoRequest.setQuery(
					"SELECT%20DISTINCT%20%3FroleLabel%20%3FclassLabel%20%3FroleWikipediaLink%0AWHERE%20%0A%7B%0A%20%20%3Frole%20wdt%3AP31%20wd%3AQ5.%0A%20%20%3Frole%20wdt%3AP39%7Cwdt%3AP106%20%3Fclass%20.%0A%20%20FILTER%28%3Fclass%20%3D%20wd%3AQ484876%29%0A%20%20%0A%20%20%0A%20%20%0A%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22%5BAUTO_LANGUAGE%5D%2Cen%22.%20%7D%0A%7D");
			ceoRequest.setDataFormat(DataForomat.JSON);
			result = Caller.run(ceoRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_PERSON_ENTITIES,"ceo");

			/**
			 * Chancellor result appended to President file
			 */
			final Request chancellorRequest = new Request();
			chancellorRequest.setQuery(
					"SELECT%20DISTINCT%20%3FroleLabel%20%3FclassLabel%20%3FroleWikipediaLink%0AWHERE%20%0A%7B%0A%20%20%3Frole%20wdt%3AP31%20wd%3AQ5.%0A%20%20%3Frole%20wdt%3AP39%20%3Fclass%20.%0A%20%20%3Fclass%20wdt%3AP279%2a%20wd%3AQ373085%20.%0A%20%20%0A%20%20%3FroleWikipediaLink%20schema%3Aabout%20%3Frole%20.%0A%20%20FILTER%20REGEX%28STR%28%3FroleWikipediaLink%29%2C%20%22en.wikipedia.org%2Fwiki%2F%22%29%20.%0A%20%20SERVICE%20wikibase%3Alabel%20%7B%20bd%3AserviceParam%20wikibase%3Alanguage%20%22%5BAUTO_LANGUAGE%5D%2Cen%22.%20%7D%0A%7D");
			chancellorRequest.setDataFormat(DataForomat.JSON);
			result = Caller.run(chancellorRequest);
			JsonParser.parseResultToMap(result);
			JsonParser.aggegateAndPrintAnchorTextData(ADDRESS_OF_WIKIDATA_PERSON_ENTITIES,"president");
		};
	}
}
