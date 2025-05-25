package ru.electronprod.OtryadAdmin.data;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

	/**
	 * @return Current date in format "yyyy.MM.dd"
	 */
	public static String getStringDate() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		return dateFormat.format(calendar.getTime());
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
	public static String getStringDate(String unknownDate) throws Exception {
		String[] possiblePatterns = { "yyyy-MM-dd", "dd-MM-yyyy", "yyyy.MM.dd", "MM/dd/yyyy" };
		LocalDate parsedDate = null;
		for (String pattern : possiblePatterns) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
				parsedDate = LocalDate.parse(unknownDate, formatter);
				break;
			} catch (DateTimeParseException ignored) {
			}
		}
		if (parsedDate != null) {
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
			return parsedDate.format(outputFormatter);
		} else {
			log.warn("Unknown date recognition error: " + unknownDate);
			throw new Exception("Unknown date recognition error: " + unknownDate);
		}
	}
}
