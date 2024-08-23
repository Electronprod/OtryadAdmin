package ru.electronprod.OtryadAdmin.services;

import java.util.List;

import ru.electronprod.OtryadAdmin.models.Human;

public class SearchService {
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

	public static Human findMostSimilarHuman(String input, List<Human> humanList) {
		Human mostSimilar = new Human();
		int maxLCSLength = 0;

		for (Human human : humanList) {
			int lcsLength = findLCSLength(input, human.getLastname() + " " + human.getName());
			if (lcsLength > maxLCSLength) {
				maxLCSLength = lcsLength;
				mostSimilar = human;
			}
		}
		return mostSimilar;
	}
}
