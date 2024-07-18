package ru.electronprod.OtryadAdmin.services.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
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
}
