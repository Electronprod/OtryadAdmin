package ru.electronprod.OtryadAdmin.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.electronprod.OtryadAdmin.models.Human;

/**
 * Methods to find objects from Collections
 */
public class SearchUtil {

	private static int findLevenshteinDistance(String str1, String str2) {
		int m = str1.length();
		int n = str2.length();
		int[][] dp = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n; j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
					dp[i][j] = dp[i - 1][j - 1];
				} else {
					dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
				}
			}
		}
		return dp[m][n];
	}

	/**
	 * Finds most similar Human from List given.
	 * 
	 * @param lastname_and_name - Humans's lastname and name in format "lastname
	 *                          name".
	 * @param humanList         - List to find from.
	 * @return Human entity or null if not found
	 */
	public static Human findMostSimilarHuman(String lastname_and_name, List<Human> humanList) {
		Human mostSimilar = null;
		int minLevenshteinDistance = Integer.MAX_VALUE;

		for (Human human : humanList) {
			String fullName = human.getLastname() + " " + human.getName();
			int levenshteinDistance = findLevenshteinDistance(lastname_and_name.toLowerCase(), fullName.toLowerCase());
			if (levenshteinDistance < minLevenshteinDistance) {
				minLevenshteinDistance = levenshteinDistance;
				mostSimilar = human;
			}
		}
		return mostSimilar;
	}

	/**
	 * Recognizes Humans from input String
	 * 
	 * @param input      - String in format "lastname name" and separator "\n"
	 * @param searchFrom - List<Human> where to search from
	 * @return Map<String, Human> where String - original from "input", Human - most
	 *         similar object from searchFrom list
	 */
	public static Map<String, Human> recognizeHumansFromString(String input, List<Human> searchFrom) {
		Map<String, Human> result = new LinkedHashMap<String, Human>();
		for (String FI : input.split("\n")) {
			Human human = findMostSimilarHuman(FI, searchFrom);
			if (human != null)
				result.put(FI, human);
		}
		return result;
	}
}
