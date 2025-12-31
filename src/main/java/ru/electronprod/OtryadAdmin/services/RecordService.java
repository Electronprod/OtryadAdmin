package ru.electronprod.OtryadAdmin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.ActionRecordRepository;
import ru.electronprod.OtryadAdmin.models.ActionRecord;
import ru.electronprod.OtryadAdmin.models.ActionRecordType;
import ru.electronprod.OtryadAdmin.models.User;

@Slf4j
@Service
public class RecordService {
	@Getter
	@Autowired
	private ActionRecordRepository actionLog;

	public void recordAction(User user, String message, ActionRecordType type) {
		recordAction(user.getLogin(), user.getRole(), message, type);
	}

	@Async
	public void recordAction(String login, String role, String message, ActionRecordType type) {
		try {
			actionLog.save(new ActionRecord(login, role, message, type));
		} catch (Exception e) {
			log.error("Error logging action to database", e);
		}
	}
}
