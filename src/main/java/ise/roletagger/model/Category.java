package ise.roletagger.model;

public enum Category {
	HEAD_OF_STATE_TAG("president"),
	POPE_TAG("pope"), 
	MONARCH_TAG("king"), 
	CHAIR_PERSON_TAG("ceo"),
	/**
	 * I add this because I need it in the class MyStandfordCoreNLPRegex
	 * when I run "run" function 
	 */
	ROLE("ROLE");

	private String text;

	Category(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}
	
	public static Category resolve(String text){
		for(Category cat: Category.values()){
			if(cat.text().equals(text.toLowerCase())){
				return cat;
			}
		}
		System.err.println("*******************************************************");
		System.exit(1);
		return null;
	}

	public static Category resolveWithCategoryName(String text) {
		for(Category cat: Category.values()){
			if(cat.name().equals(text)){
				return cat;
			}
		}
		return null;
	}
}