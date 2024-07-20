package ru.electronprod.OtryadAdmin.services.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.Getter;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.auth.AuthService;

@Getter
@Service
public class DBService {
	@Autowired
	private AuthService authService;
	@Autowired
	private SquadService squadService;
	@Autowired
	private HumanService humanService;
	@Autowired
	private StatsService statsService;
	@Autowired
	private NewsService newsService;

	public String getStringDate() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		return dateFormat.format(calendar.getTime());
	}
}
