package ise.roletagger.categorytreegeneration;

import java.util.Arrays;
import java.util.List;

public class CategorySeedLoaderDummy extends CategorySeedloader {

	public CategorySeedLoaderDummy() {

	}

	@Override
	public void loadSeeds() {
		final List<String> dummySeeds = Arrays.asList("Chief_executive_officers", "Heads_of_state",
				"Chancellors_(government)", "Popes", "Monarchy");
//		final List<String> dummySeeds = Arrays.asList("Monarchy");
		for (String s : dummySeeds) {
			getSeeds().add(s);
		}
	}
}
