package ise.roletagger.categorytreegeneration;

import java.util.ArrayList;
import java.util.List;

public abstract class CategorySeedloader {
	private final List<String> seeds = new ArrayList<>();
	
	public void loadSeeds() {
	};
	
	public List<String> getSeeds(){
		return seeds;
	}
}
