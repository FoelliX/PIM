package de.foellix.aql.pim.helper;

public class Helper {
	private static final String DEFAULT_SEPARATOR = " ";

	public static String arrayToString(String[] array) {
		return arrayToString(array, DEFAULT_SEPARATOR);
	}

	public static String arrayToString(String[] array, String separator) {
		final StringBuilder sb = new StringBuilder();
		for (final String item : array) {
			sb.append(item + separator);
		}
		return sb.toString();
	}
}
