package ru.electronprod.OtryadAdmin.services;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.models.ActionRecordType;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.StatsRecord;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.MarkDTO;
import ru.electronprod.OtryadAdmin.telegram.BotService;
import ru.electronprod.OtryadAdmin.utils.FileOptions;

@Slf4j
@Service
public class MarkService {
	@Autowired
	private DBService dbservice;
	@Autowired
	private BotService botServ;
	@Autowired
	private RecordService rec;

	@Transactional(readOnly = true)
	private StatsRecord createUnpresentStatsRecord(JSONObject data, String event, String formatted_date, User user,
			int event_id, List<Human> humans, String group) {
		// Finding human to add stats record
		Human human = humans.stream().filter(h -> h.getId() == Integer.parseInt(String.valueOf(data.get("id"))))
				.findFirst().orElseThrow(() -> new IllegalStateException("Human not found: " + data.get("id")));
		// Creating stats record
		StatsRecord stats = new StatsRecord(human);
		stats.setAuthor(user.getLogin());
		stats.setDate(formatted_date);
		stats.setPresent(false);
		stats.setReason(String.valueOf(data.get("reason")));
		stats.setType(event);
		stats.setUser_role(user.getRole());
		stats.setEvent_id(event_id);
		stats.setGroup(group);
		humans.remove(human);
		return stats;
	}

	@SuppressWarnings("unchecked")
	@Transactional()
	public int mark_group(MarkDTO dto, User user, List<Human> humans, String group) throws Exception {
		// Validating input
		String date = DBService.getStringDate(dto.getDate() != null ? dto.getDate() : DBService.getStringDate());
		String event;
		if (dto.getEvent() != null) {
			event = dto.getEvent();
		} else {
			throw new IllegalArgumentException("Event is null");
		}
		// Defining variables
		List<StatsRecord> resultRecords = new ArrayList<StatsRecord>();
		int event_id = dbservice.getStatsRepository().findMaxEventIDValue() + 1;
		// Marking unPresent humans
		for (Object o : dto.getUnpresentPeople()) {
			JSONObject data = (JSONObject) FileOptions.ParseJS(String.valueOf(o));
			resultRecords.add(createUnpresentStatsRecord(data, event, date, user, event_id, humans, group));
		}
		// Marking Present humans
		dto.getPresentPeople().forEach(h1 -> {
			Human human = humans.stream().filter(h -> h.getId() == Integer.parseInt(String.valueOf(h1))).findFirst()
					.orElseThrow(() -> new IllegalStateException("Human not found: " + h1));
			StatsRecord stats = new StatsRecord(human);
			stats.setAuthor(user.getLogin());
			stats.setDate(date);
			stats.setPresent(true);
			stats.setReason("error:present");
			stats.setType(event);
			stats.setUser_role(user.getRole());
			stats.setEvent_id(event_id);
			stats.setGroup(group);
			resultRecords.add(stats);
		});
		// Saving records to database
		if (dbservice.getStatsRepository().saveAll(resultRecords) == null)
			throw new Exception("Error saving stats records to database!");
		log.info("User " + user.getLogin() + " (" + user.getRole() + ") marked " + resultRecords.size()
				+ " people. EventID: " + event_id + " Group: " + group);
		botServ.sendNotification_marked(user, event);
		rec.recordAction(user,
				"EventID %d: marked %d people. Group: %s".formatted(event_id, resultRecords.size(), group),
				ActionRecordType.MARK);
		return event_id;
	}
}
