package ru.electronprod.OtryadAdmin.data.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.Getter;
import ru.electronprod.OtryadAdmin.models.User;

@Service
public class DBService {
	@Autowired
	@Getter
	private UserService userService;
	@Autowired
	@Getter
	private SquadService squadService;
	@Autowired
	@Getter
	private HumanService humanService;
	@Autowired
	private SquadStatsService statsService;

	public static String getStringDate() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		return dateFormat.format(calendar.getTime());
	}

	public SquadStatsService getSquadStatsService() {
		return statsService;
	}
}
