package ru.electronprod.OtryadAdmin.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import lombok.Getter;
import ru.electronprod.OtryadAdmin.data.filesystem.FileOptions;

@Service
public class OptionService implements InitializingBean {
	@Getter
	private Map<String, String> event_types = new LinkedHashMap<String, String>();
	@Getter
	private Map<String, String> reasons_for_absences = new LinkedHashMap<String, String>();
	@Getter
	private Map<String, String> replacements = new LinkedHashMap<String, String>();

	@Override
	public void afterPropertiesSet() {
		File config = new File("data_config.txt");
		if (FileOptions.loadFile(config) || FileOptions.getFileLines(config.getPath()).isEmpty()) {
			try {
				writeDefaults(config);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		// Loading data from file
		List<String> data = FileOptions.getFileLines(config.getPath());
		String lastType = "-1";
		for (String line : data) {
			if (line.startsWith("!")) {
				lastType = line;
				continue;
			}
			if (lastType.equalsIgnoreCase("!event_types")) {
				if (!line.contains(":")) {
					System.err.println("[OptionService]: can't load line: " + line);
				}
				event_types.put(line.split(":")[0], line.split(":")[1]);
			} else if (lastType.equalsIgnoreCase("!reasons_for_absences")) {
				if (!line.contains(":")) {
					System.err.println("[OptionService]: can't load line: " + line);
				}
				reasons_for_absences.put(line.split(":")[0], line.split(":")[1]);
			} else if (lastType.equalsIgnoreCase("!replacements")) {
				if (!line.contains(":")) {
					System.err.println("[OptionService]: can't load line: " + line);
				}
				replacements.put(line.split(":")[0], line.split(":")[1]);
			} else {
				System.err.println(
						"[OptionService]: error parsing config! Operation was stopped. Caused by line: " + line);
				return;
			}
		}
		System.out.println("[OptionService]: loaded data from file.");
	}

	private void writeDefaults(File config) throws IOException {
		List<String> data = new ArrayList<String>();
		data.add("!event_types");
		data.add("general:Посещение общего сбора");
		data.add("duty:Дежурство (вызвался{ась} дежурить в кабинете)");
		data.add("walk:Прогулка (гулял{а} со звеном)");
		data.add("other1:Другое #1 (используйте эту категорию по своему усмотрению)");
		data.add("other2:Другое #2 (используйте эту категорию по своему усмотрению)");

		data.add("!reasons_for_absences");
		data.add("ill:Заболел(а)");
		data.add("away:Уехал(а)");
		data.add("study:Учеба");
		data.add("respect:Уважительная причина");
		data.add("disrespect:Неуважительная причина");

		data.add("!replacements");
		data.add("true:+");
		data.add("false:-");
		FileOptions.writeLinesToFile(data, config.getPath());
		System.out.println("[OptionService]: created and wrote defaults to file.");
	}

}
