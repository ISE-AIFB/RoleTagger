package ise.roletagger.categorytreegeneration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CategorySeedLoaderFileBased extends CategorySeedloader{

	private final String SEED_FILE;
	public CategorySeedLoaderFileBased(final String seedFile) {
		SEED_FILE = seedFile;
	}

	@Override
	public void loadSeeds() {
		try (BufferedReader br = new BufferedReader(new FileReader(SEED_FILE ))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				getSeeds().add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
