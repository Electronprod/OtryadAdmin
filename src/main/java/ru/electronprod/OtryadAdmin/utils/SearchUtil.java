package ru.electronprod.OtryadAdmin.utils;

import java.util.Collection;
import java.util.List;

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

	public static String findMostSimilarFromList(String str, Collection<String> list) {
		String mostSimilar = null;
		int minLevenshteinDistance = Integer.MAX_VALUE;

		for (String val : list) {
			int levenshteinDistance = findLevenshteinDistance(str.toLowerCase(), val.toLowerCase());
			if (levenshteinDistance < minLevenshteinDistance) {
				minLevenshteinDistance = levenshteinDistance;
				mostSimilar = val;
			}
		}
		return mostSimilar;
	}

}
