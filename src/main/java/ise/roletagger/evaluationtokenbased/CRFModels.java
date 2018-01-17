package ise.roletagger.evaluationtokenbased;

public enum CRFModels {
	ONLY_HEAD_ROLE("onlyHeadRoleFullData"),
	//ONLY_ROLE_PHRASE("onlyRolePhraseFullData"),
	//ONLY_ANCHOR_TEXT("onlyAnchorTextFullData");
	;
	
	private String text;

	CRFModels(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}
	
}
