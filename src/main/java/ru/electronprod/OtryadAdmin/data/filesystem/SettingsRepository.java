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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.utils.FileOptions;

/**
 * Manages the functionality configuration file
 */
@Repository
@Slf4j
public class SettingsRepository implements InitializingBean {
	@Getter
	private static File config = new File("settings.txt");
	@Getter
	private static Map<String, Pair<String, Boolean>> event_types = new LinkedHashMap<String, Pair<String, Boolean>>();
	@Getter
	private static List<String> reasons_for_absences = new ArrayList<String>();;
	@Getter
	private static Map<String, String> replacements = new LinkedHashMap<String, String>();
	@Getter
	private static final Map<String, String> roles = Map.of("ROLE_ADMIN", "ADMIN", "ROLE_SQUADCOMMANDER",
			"КОМАНДИР ЗВЕНА", "ROLE_COMMANDER", "ЗАМ. КОМАНДИРА ОТРЯДА", "ROLE_OBSERVER", "НАБЛЮДАТЕЛЬ", "ROLE_HEAD",
			"РУКОВОДИТЕЛЬ ОТРЯДА");
	/** Section name constant **/
	public static final String SECTION_EVENT_TYPES = "event_types";
	/** Section name constant **/
	public static final String SECTION_REASONS = "reasons_for_absences";
	/** Section name constant **/
	public static final String SECTION_REPLACEMENTS = "replacements";

	/**
	 * Checks file existence and reads data to memory at startup. If an exception
	 * occurs in this method, the program will stop.
	 */
	@Override
	public void afterPropertiesSet() {
		// Checking file existence
		try {
			if (config.createNewFile() || FileOptions.getFileLines(config.getPath()).isEmpty()) {
				writeDefaults(config);
			}
		} catch (IOException e) {
			log.error("An exception occurred while preparing the settings file:", e);
			System.exit(1);
		}
		// Loading data from file
		try {
			loadData(config);
		} catch (ParseException e) {
			log.error("An exception occurred while parsing the settings file:", e);
			System.exit(1);
		}
	}

	/**
	 * Loads and saves data from file to memory
	 * 
	 * @param config - File to read from
	 * @throws ParseException
	 */
	private static void loadData(File config) throws ParseException {
		event_types.clear();
		replacements.clear();
		reasons_for_absences.clear();
		JSONObject data = (JSONObject) FileOptions.ParseJS(FileOptions.getFileLine(config));
		// Adding event types
		JSONArray eventtypes = (JSONArray) data.get(SECTION_EVENT_TYPES);
		for (Object o : eventtypes) {
			JSONObject obj = (JSONObject) o;
			event_types.put(String.valueOf(obj.get("event")), Pair.of(String.valueOf(obj.get("groupname")),
					Boolean.parseBoolean(String.valueOf(obj.get("canSetReason")))));
		}
		// Adding reasons for absences
		JSONArray reasons = (JSONArray) data.get(SECTION_REASONS);
		for (Object o : reasons) {
			JSONObject obj = (JSONObject) o;
			reasons_for_absences.add(String.valueOf(obj.get("reason")));
		}
		// Adding replacements
		JSONArray repls = (JSONArray) data.get(SECTION_REPLACEMENTS);
		for (Object o : repls) {
			JSONObject obj = (JSONObject) o;
			replacements.put(String.valueOf(obj.get("from")), String.valueOf(obj.get("to")));
		}
		log.info("Settings loaded successfully!");
	}

	/**
	 * Writes default settings to file
	 * 
	 * @param config - File to write to
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static void writeDefaults(File config) throws IOException {
		JSONObject data = new JSONObject();
		// Adding event types
		JSONArray eventtypes = new JSONArray();
		eventtypes.add(generateEvent("Общий сбор", true));
		eventtypes.add(generateEvent("Дежурство", false));
		eventtypes.add(generateEvent("Звеньевое событие", false));
		data.put(SECTION_EVENT_TYPES, eventtypes);
		// Adding reasons for absences
		JSONArray reasons = new JSONArray();
		reasons.add(generateReason("Неизвестно"));
		reasons.add(generateReason("Заболел(а)"));
		reasons.add(generateReason("Уехал(а)"));
		reasons.add(generateReason("Учеба"));
		reasons.add(generateReason("Уважительная причина"));
		reasons.add(generateReason("Неуважительная причина"));
		data.put(SECTION_REASONS, reasons);
		// Adding replacements
		JSONArray repls = new JSONArray();
		repls.add(generateReplacement("error:present", "N/A"));
		repls.add(generateReplacement("error:unsupported_event", "N/S"));
		repls.add(generateReplacement("true", "+"));
		repls.add(generateReplacement("false", "-"));
		data.put(SECTION_REPLACEMENTS, repls);
		FileOptions.writeFile(data.toJSONString(), config);
		log.info("Wrote defaults to " + config.getName());
	}

	/**
	 * Adds JSONObject to selected section
	 * 
	 * @param section - section where to write down. They are declared in this class
	 *                as final variables.
	 * @param toWrite - JSONObject to write
	 * @throws ParseException - if section not found
	 */
	@SuppressWarnings("unchecked")
	public static void addData(String section, JSONObject toWrite) throws ParseException {
		JSONObject main = (JSONObject) FileOptions.ParseJS(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get(section);
		sect.add(toWrite);
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	/**
	 * @param event - event to remove
	 * @throws Exception if event not found or config has syntax problems
	 */
	@SuppressWarnings("unchecked")
	public static void removeEvent(String event) throws Exception {
		JSONObject main = (JSONObject) FileOptions.ParseJS(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get(SECTION_EVENT_TYPES);
		if (!sect.removeIf(o -> String.valueOf(JSONObject(o).get("event")).equals(event))) {
			throw new Exception("Can't find event to remove: " + event);
		}
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	/**
	 * @param reason - reason to remove
	 * @throws Exception if reason not found or config has syntax problems
	 */
	@SuppressWarnings("unchecked")
	public static void removeReason(String reason) throws Exception {
		JSONObject main = (JSONObject) FileOptions.ParseJS(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get(SECTION_REASONS);
		if (!sect.removeIf(o -> String.valueOf(JSONObject(o).get("reason")).equals(reason))) {
			throw new Exception("Can't find reason to remove: " + reason);
		}
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	/**
	 * @param replacement - replacement to remove
	 * @throws Exception if replacement not found or config has syntax problems
	 */
	@SuppressWarnings("unchecked")
	public static void removeReplacement(String replacement) throws Exception {
		JSONObject main = (JSONObject) FileOptions.ParseJS(FileOptions.getFileLine(config));
		JSONArray sect = (JSONArray) main.get(SECTION_REPLACEMENTS);
		if (!sect.removeIf(o -> String.valueOf(JSONObject(o).get("from")).equals(replacement))) {
			throw new Exception("Can't find replacement to remove: " + replacement);
		}
		FileOptions.writeFile(main.toJSONString(), config);
		loadData(config);
	}

	private static JSONObject JSONObject(Object o) {
		return (JSONObject) o;
	}

	/**
	 * Generates event's JSONObject
	 * 
	 * @param event - event appearance in the database
	 * @param mark  - Can a commander indicate reasons for absence? (true - yes,
	 *              false - no)
	 * @return JSONObject
	 */
	public static JSONObject generateEvent(String event, boolean mark) {
		return generateEventWithGroup(event, mark, null);
	}

	/**
	 * Generates event's JSONObject
	 * 
	 * @param event     - event appearance in the database
	 * @param mark      - Can a commander indicate reasons for absence? (true - yes,
	 *                  false - no)
	 * @param groupname - name of the group to which the event must be added
	 * @return JSONObject
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject generateEventWithGroup(String event, boolean mark, String groupname) {
		JSONObject o = new JSONObject();
		o.put("event", event);
		o.put("canSetReason", mark);
		o.put("groupname", groupname);
		return o;
	}

	/**
	 * Generates reason's JSONObject
	 * 
	 * @param reason - reason appearance in the database
	 * @return JSONObject
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject generateReason(String reason) {
		JSONObject o = new JSONObject();
		o.put("reason", reason);
		return o;
	}

	/**
	 * Generates replacement's JSONObject
	 * 
	 * @param from - what to replace
	 * @param to   - what to replace with
	 * @return JSONObject
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject generateReplacement(String from, String to) {
		JSONObject o = new JSONObject();
		o.put("from", from);
		o.put("to", to);
		return o;
	}

	/**
	 * Returns key by value from Map(String,String)
	 * 
	 * @param map   - map to work with
	 * @param value - value to search by
	 * @return key or null if not found
	 */
	public static String getKeyByValue(Map<String, String> map, String value) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue().equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static boolean containsGroup(String group) {
		if (event_types.containsValue(Pair.of(group, false)) || event_types.containsValue(Pair.of(group, true))) {
			return true;
		}
		return false;
	}
}
