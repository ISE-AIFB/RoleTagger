package ise.roletagger.categorytreegeneration;

import java.util.concurrent.TimeUnit;

import ise.roletagger.model.ListOfSubjectObject;
import ise.roletagger.util.Config;

public class CategoryTreeMainGenerator {

	private static int TREE_DEPTH = Config.getInt("TREE_DEPTH", 0);
	private static String CATEGORY_FILE = Config.getString("SKOS_CATEGORY_FILE", "");
	/**
	 * This directory contain a file which contains the seeds(roots) for category
	 * tree generation. Each line contains one seed
	 */
	private static String SEED_FILE = Config.getString("ADDRESS_OF_CATEGORY_SEEDS", "");

	public static void main(String[] args) {
		final Thread t = new Thread(run());
		t.setDaemon(false);
		t.start();
	}

	public static Runnable run() {
		return () -> {
			final CategorySeedloader seedLoader = new CategorySeedLoaderFileBased(SEED_FILE);
			seedLoader.loadSeeds();

			final CategoryFileParser fileParser = new CategoryFileParser(CATEGORY_FILE);
			long now = System.currentTimeMillis();
			fileParser.parse();
			System.err.println(
					"Reading " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now) + " seconds");

			now = System.currentTimeMillis();
			new FastLookUpSubjectObject(ListOfSubjectObject.getListOfSubjectObjects());
			System.err.println(
					"Speedup " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now) + " seconds");

			now = System.currentTimeMillis();
			new CategoryTreeGenerator(seedLoader.getSeeds(), TREE_DEPTH).printTreesToFile();
			System.err.println("Generating Trees " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - now)
					+ " seconds");
		};
	}

}
