package ise.roletagger.model;

public class AnchorText {
	private final String anchorText;
	/**
	 * how many times this anchorText has been seen
	 */
	private long frequency = 0;

	public AnchorText(String anchorText) {
		this.anchorText = anchorText;
	}

	public void increment(){
		frequency++;
	}

	public long getFrequency() {
		return frequency;
	}

	public String getAnchorText() {
		return anchorText;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((anchorText == null) ? 0 : anchorText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnchorText other = (AnchorText) obj;
		if (anchorText == null) {
			if (other.anchorText != null)
				return false;
		} else if (!anchorText.equals(other.anchorText))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnchorText [anchorText=" + anchorText + ", frequency=" + frequency + "]";
	}

}
