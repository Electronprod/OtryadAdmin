package ru.electronprod.OtryadAdmin.data;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.models.ActionRecordType;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.RecordService;

/**
 * Class provides access to master JPA repositories
 */
@Slf4j
@Service
public class DBService {
	@Getter
	@Autowired
	private HumanRepository humanRepository;
	@Getter
	@Autowired
	private SquadRepository squadRepository;
	@Getter
	@Autowired
	private StatsRecordRepository statsRepository;
	@Getter
	@Autowired
	private UserRepository userRepository;
	@Getter
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private RecordService rec;
	private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private static ZoneId zone;

	@Value("${app.timezone}")
	private void setTimeZone(String timezone) {
		DBService.zone = ZoneId.of(timezone);
	}

	/**
	 * @return Current date in format "yyyy.MM.dd"
	 */
	public static String getStringDate() {
		return LocalDate.now(zone).format(OUTPUT_FORMATTER);
	}

	/**
	 * Parses and converts date in other format to "yyyy.MM.dd"
	 * 
	 * @param unknownDate - date to parse and convert
	 * @return date in format "yyyy.MM.dd"
	 * @throws Exception recognition error
	 * @apiNote Possible input date formats: "yyyy-MM-dd", "dd-MM-yyyy",
	 *          "yyyy.MM.dd", "MM/dd/yyyy"
	 */
	private static final DateTimeFormatter[] INPUT_FORMATTERS = { DateTimeFormatter.ofPattern("yyyy-MM-dd"),
			DateTimeFormatter.ofPattern("dd-MM-yyyy"), DateTimeFormatter.ofPattern("yyyy.MM.dd"),
			DateTimeFormatter.ofPattern("MM/dd/yyyy") };

	public static String getStringDate(String unknownDate) throws IllegalArgumentException {
		for (DateTimeFormatter fmt : INPUT_FORMATTERS) {
			try {
				LocalDate parsed = LocalDate.parse(unknownDate, fmt);
				return parsed.format(OUTPUT_FORMATTER);
			} catch (DateTimeParseException ignored) {
				// try next one
			}
		}
		log.warn("Date recognition error " + unknownDate);
		throw new IllegalArgumentException("Date recognition error: " + unknownDate);
	}

	public void recordAction(User user, String message, ActionRecordType type) {
		rec.recordAction(user.getLogin(), user.getRole(), message, type);
	}

	public void recordAction(String login, String role, String message, ActionRecordType type) {
		rec.recordAction(login, role, message, type);
	}
}