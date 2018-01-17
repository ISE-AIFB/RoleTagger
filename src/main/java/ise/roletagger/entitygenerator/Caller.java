package ise.roletagger.entitygenerator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Caller {
	private static final String WIKIDATA_ENDPOINT = "https://query.wikidata.org/sparql?query=";

	static {
		System.setProperty("java.net.useSystemProxies", "true");
	}

	public static String run(final Request request) {

		try {
			final URL url = new URL(WIKIDATA_ENDPOINT + request.getQuery());
			final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", request.getDataFormat().text);

			System.err.println("Accessing REST API...");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			System.err.println("Received result from REST API.");
			final BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			final StringBuilder result = new StringBuilder("");
			String output;
			while ((output = br.readLine()) != null) {
				result.append(output);
			}
			conn.disconnect();
			return result.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
