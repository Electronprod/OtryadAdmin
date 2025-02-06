package ru.electronprod.OtryadAdmin.data.filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class TelegramLanguage {
	@Value("classpath:telegram_language.txt")
	private Resource lang_file;

	private List<String> getFileLines() throws IOException {
		InputStream inputStream = lang_file.getInputStream();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			List<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	public String get(String key) {
		try {
			List<String> lines = getFileLines();
			for (String line : lines) {
				if (line.startsWith(key + ":>>")) {
					return line.replace(key + ":>>", "").replace("<br>", "\n");
				}
			}
			return "Error: Key not found. Please, contact administrator.";
		} catch (IOException e) {
			e.printStackTrace();
			return "Error reading tg language file. Trace: " + e.getMessage();
		}
	}
}
