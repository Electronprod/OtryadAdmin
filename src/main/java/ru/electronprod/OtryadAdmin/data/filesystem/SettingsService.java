package ru.electronprod.OtryadAdmin.data.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.models.dto.EventTypeDTO;

@Slf4j
@Repository
public class SettingsService implements InitializingBean {
	@Getter
	private static List<EventTypeDTO> event_types = new ArrayList<EventTypeDTO>();
	@Getter
	private static Map<String, String> reasons_for_absences = new LinkedHashMap<String, String>();
	@Getter
	private static Map<String, String> replacements = new LinkedHashMap<String, String>();
	@Getter
	private static File config = new File("settings.txt");

	@Override
	public void afterPropertiesSet() {
		if (FileOptions.loadFile(config) || FileOptions.getFileLines(config.getPath()).isEmpty()) {
			try {
				writeDefaults(config);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		// Loading data from file
		try {
			loadData(config);
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void loadData(File config) throws ParseException {
		event_types.clear();
		replacements.clear();
		reasons_for_absences.clear();
		JSONObject data = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(config));
		// Adding event types
		JSONArray eventtypes = (JSONArray) data.get("event_types");
		for (Object o : eventtypes) {
			JSONObject obj = (JSONObject) o;
			event_types.add(new EventTypeDTO(String.valueOf(obj.get("event")), String.valueOf(obj.get("name")),
					Boolean.parseBoolean(String.valueOf(obj.get("canSetReason")))));
		}
		// Adding reasons for absences
		JSONArray reasons = (JSONArray) data.get("reasons_for_absences");
		for (Object o : reasons) {
			JSONObject obj = (JSONObject) o;
			reasons_for_absences.put(String.valueOf(obj.get("reason")), String.valueOf(obj.get("name")));
		}
		// Adding replacements
		JSONArray repls = (JSONArray) data.get("replacements");
		for (Object o : repls) {
			JSONObject obj = (JSONObject) o;
			replacements.put(String.valueOf(obj.get("from")), String.valueOf(obj.get("to")));
		}
		log.info("(Re) Loaded data from " + config.getName());
	}

	@SuppressWarnings("unchecked")
	private static void writeDefaults(File config) throws IOException {
		JSONObject data = new JSONObject();
		// Adding event types
		JSONArray eventtypes = new JSONArray();
		eventtypes.add(generateEvent("general", "Посещение общего сбора", true));
		eventtypes.add(generateEvent("duty", "Дежурство (вызвался дежурить в кабинете)", false));
		eventtypes.add(generateEvent("walk", "Прогулка (гулял со звеном)", false));
		data.put("event_types", eventtypes);
		// Adding reasons for absences
		JSONArray reasons = new JSONArray();
		reasons.add(generateReason("ill", "Заболел(а)"));
		reasons.add(generateReason("away", "Уехал(а)"));
		reasons.add(generateReason("study", "Учеба"));
		reasons.add(generateReason("respect", "Уважительная причина"));
		reasons.add(generateReason("disrespect", "Неуважительная причина"));
		data.put("reasons_for_absences", reasons);
		// Adding replacements
		JSONArray repls = new JSONArray();
		repls.add(generateReplacement("error:present", "N/A"));
		repls.add(generateReplacement("error:unsupported_event", "N/S"));
		repls.add(generateReplacement("true", "+"));
		repls.add(generateReplacement("false", "-"));
		data.put("replacements", repls);
		FileOptions.writeFile(data.toJSONString(), config);
		log.info("Wrote defaults to " + config.getName());
	}

	@SuppressWarnings("unchecked")
	public static void addData(String section, JSONObject toWrite) throws ParseException {
		JSONObject main = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get(section);
		sect.add(toWrite);
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	@SuppressWarnings("unchecked")
	public static void removeEvent(String event) throws Exception {
		JSONObject main = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get("event_types");
		if (!sect.removeIf(o -> String.valueOf(JSONObject(o).get("event")).equals(event))) {
			throw new Exception("Can't find event to remove: " + event);
		}
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	@SuppressWarnings("unchecked")
	public static void removeReason(String reason) throws Exception {
		JSONObject main = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get("reasons_for_absences");
		if (!sect.removeIf(o -> String.valueOf(JSONObject(o).get("reason")).equals(reason))) {
			throw new Exception("Can't find reason to remove: " + reason);
		}
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	@SuppressWarnings("unchecked")
	public static void removeReplacement(String replacement) throws Exception {
		JSONObject main = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get("replacements");
		if (!sect.removeIf(o -> String.valueOf(JSONObject(o).get("from")).equals(replacement))) {
			throw new Exception("Can't find replacement to remove: " + replacement);
		}
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	private static JSONObject JSONObject(Object o) {
		return (JSONObject) o;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject generateEvent(String event, String name, boolean mark) {
		JSONObject o = new JSONObject();
		o.put("event", event);
		o.put("name", name);
		o.put("canSetReason", mark);
		return o;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject generateReason(String reason, String name) {
		JSONObject o = new JSONObject();
		o.put("reason", reason);
		o.put("name", name);
		return o;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject generateReplacement(String from, String to) {
		JSONObject o = new JSONObject();
		o.put("from", from);
		o.put("to", to);
		return o;
	}

	public static String getKeyByValue(Map<String, String> map, String value) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static Map<String, String> convertEventTypeDTOs() {
		return convertEventTypeDTOs(event_types);
	}

	public static Map<String, String> convertEventTypeDTOs(List<EventTypeDTO> list) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		list.forEach(obj -> {
			result.put(obj.getEvent(), obj.getName());
		});
		return result;
	}
}
