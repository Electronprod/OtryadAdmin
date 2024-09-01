package ru.electronprod.OtryadAdmin.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.electronprod.OtryadAdmin.data.filesystem.OptionService;
import ru.electronprod.OtryadAdmin.models.Human;

@Service
public class SearchService {
	@Autowired
	private OptionService optionServ;

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

	@Transactional
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

	public String findMostSimilarEvent(String event) {
		String mostSimilar = "";
		int maxLCSLength = 0;
		for (String val : optionServ.getEvent_types().values()) {
			int lcsLength = findLCSLength(event, val);
			if (lcsLength > maxLCSLength) {
				maxLCSLength = lcsLength;
				mostSimilar = val;
			}
		}
		return mostSimilar;
	}

	public String findMostSimilarReason(String reason) {
		String mostSimilar = "";
		int maxLCSLength = 0;
		for (String val : optionServ.getReasons_for_absences().values()) {
			int lcsLength = findLCSLength(reason, val);
			if (lcsLength > maxLCSLength) {
				maxLCSLength = lcsLength;
				mostSimilar = val;
			}
		}
		return mostSimilar;
	}
}
