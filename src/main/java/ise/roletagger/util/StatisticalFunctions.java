package ise.roletagger.util;

public class StatisticalFunctions {

	public static double sigmoid(double x) {
		return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}
}
