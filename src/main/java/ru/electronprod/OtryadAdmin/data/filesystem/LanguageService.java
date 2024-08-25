package ru.electronprod.OtryadAdmin.data.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.telegram.TelegramBot;

@Slf4j
@Service
public class LanguageService implements InitializingBean {
	@Getter
	private Map<String, String> language = new HashMap<String, String>();

	@Override
	public void afterPropertiesSet() throws Exception {
		File config = new File("language.txt");
//		if (FileOptions.loadFile(config) || FileOptions.getFileLines(config.getPath()).isEmpty()) {
//			try {
//				writeDefaults(config);
//			} catch (IOException e) {
//				e.printStackTrace();
//				System.exit(1);
//			}
//		}
		writeDefaults(config);
		// Loading data from file
		List<String> data = FileOptions.getFileLines(config.getPath());
		for (String line : data) {
			if (!line.contains(":>")) {
				log.error("Can't load data from line: " + line);
			} else {
				language.put(line.split(":>")[0], line.split(":>")[1].replaceAll("<br>", "\n"));
			}
		}
		log.info("Loaded language data from file.");
	}

	private void writeDefaults(File config) throws IOException {
		List<String> data = new ArrayList<String>();
		data.add("tg_unautorized:>Вас нет в белом списке. Если вы член бюро, обратитесь к администратору.");
		data.add(
				"tg_squadcommander_mark:><b>Чтобы отметить ребят вам необходимо прислать сюда сообщение в <i>ОПРЕДЕЛЕННОМ ФОРМАТЕ:</i></b><pre language=\"Пример\">report<br>!Посещение общего сбора<br>Иван Кораблев : заболел<br>Павел Дуров : уважительная причина</pre><br><br><u><b>Пояснения:</b></u><br> <code>report</code> - слово <b>ДОЛЖНО БЫТЬ</b> в начале сообщения<br><br><code>!Посещение общего сбора</code> - событие указывается после восклицательного знака. <b><i>Список возможных событий ограничен! Его можно просмотреть с помощью команды </i>/events</b><br><br><code>Иван Кораблев : заболел</code> - ФИ и причина пропуска пишутся через двоеточие. <b><i>Список возможных причин пропусков ограничен! Его можно просмотреть с помощью команды </i>/reasons</b>");
		data.add("tg_squadcommander_events:><b>Список доступных событий:</b><br>");
		data.add("tg_squadcommander_reasons:><b>Список доступных причин пропусков:</b><br>");
		FileOptions.writeLinesToFile(data, config.getPath());
		log.info("Created and wrote defaults to file.");
	}
}
