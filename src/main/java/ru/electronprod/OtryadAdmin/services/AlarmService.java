package ru.electronprod.OtryadAdmin.services;

import java.util.List;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.telegram.BotService;

@Slf4j
@Component
public class AlarmService implements InitializingBean {
	@Autowired
	private DBService db;
	@Autowired
	private BotService botServ;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (SettingsRepository.getAlarmConfig() == null) {
			log.warn("Alarm config not found. Creating it...");
			SettingsRepository.saveAlarmConfig(SettingsRepository.createAlarmCfg());
			log.warn("Operation completed.");
		}
	}
//
//	@Scheduled(cron = "0 0 0 * * *")
//	public void clearAutoSentList() {
//		log.info("Clearing the list of users sent remind.");
//	}
//
//	@Async
//	@Scheduled(cron = "*/7 * * * * *")
//	public void checkScheludeUpdates() throws InterruptedException {
//		try {
//			predictSquadcommanderEvent(
//					Integer.parseInt(String.valueOf(SettingsRepository.getAlarmConfig().get("prediction_border_num"))));
//		} catch (ParseException e) {
//			log.error("Error parsing config for predictSquadcommanderEvent:", e);
//		}
//	}
//
//	public void predictSquadcommanderEvent(int borderVal) throws ParseException {
//		// Is this feature enabled?
//		if (!Boolean.parseBoolean(String.valueOf(SettingsRepository.getAlarmConfig().get("prediction_enabled"))))
//			return;
//		// Shell we send notifications to Squadcommanders?
//		List<String> marked_authors = db.getStatsRepository().findDistinctAuthorsByDate("ROLE_SQUADCOMMANDER",
//				DBService.getStringDate());
//		if (marked_authors.isEmpty() || marked_authors.size() <= borderVal)
//			return;
//		// Searching for Squadcommanders, who didn't send marks
//		log.info("AUTODETECT EVENT FOR ALL SQUADCOMMANDERS: true");
//		List<User> not_marked_users = db.getUserRepository().findAllByRole("ROLE_SQUADCOMMANDER").stream()
//				.filter(user -> !marked_authors.contains(user.getLogin())).toList();
//		for (User user : not_marked_users) {
//			botServ.sendRemainderAutodetect(null, user.getTelegram());
//		}
//	}
}
