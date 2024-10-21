package ru.electronprod.OtryadAdmin.utils;

import java.util.Collection;
import java.util.List;

import ru.electronprod.OtryadAdmin.models.Human;

/**
 * Methods to find objects from Collections
 */
public class SearchUtil {

	/**
	 * @author OpenAI ChatGPT 3.5
	 */
	private static int findLCSLength(String str1, String str2) {
		int m = str1.length();
		int n = str2.length();
		int[][] dp = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n; j++) {
				if (i == 0 || j == 0) {
					dp[i][j] = 0;
				} else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
					dp[i][j] = dp[i - 1][j - 1] + 1;
				} else {
					dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
				}
			}
		}
		return dp[m][n];
	}

	/**
	 * Finds Human object from list by lastname and name
	 * 
	 * @param lastname_and_name - in format "lastname name"
	 * @param humanList
	 * @return most similar human object
	 */
	public static Human findMostSimilarHuman(String lastname_and_name, List<Human> humanList) {
		Human mostSimilar = new Human();
		int maxLCSLength = 0;

		for (Human human : humanList) {
			int lcsLength = findLCSLength(lastname_and_name, human.getLastname() + " " + human.getName());
			if (lcsLength > maxLCSLength) {
				maxLCSLength = lcsLength;
				mostSimilar = human;
			}
		}
		return mostSimilar;
	}

	/**
	 * Finds most similar String from String collection
	 * 
	 * @param str
	 * @param list
	 * @return most similar String
	 */
	public static String findMostSimilarFromList(String str, Collection<String> list) {
		String mostSimilar = "";
		int maxLCSLength = 0;
		for (String val : list) {
			int lcsLength = findLCSLength(str, val);
			if (lcsLength > maxLCSLength) {
				maxLCSLength = lcsLength;
				mostSimilar = val;
			}
		}
		return mostSimilar;
	}
}
