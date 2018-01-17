package ise.roletagger.model;

public class Tuple {
	public String a;
	public String b;

	public Tuple(String w, String b) {
		this.a = w;
		this.b = b;
	}

	@Override
	public String toString() {
		return "Tuple [a=" + a + ", b=" + b + "]";
	}

}