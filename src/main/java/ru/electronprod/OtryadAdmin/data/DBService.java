package ru.electronprod.OtryadAdmin.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;

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
	private SquadStatsRepository statsRepository;
	@Getter
	@Autowired
	private UserRepository userRepository;
	@Getter
	@Autowired
	private GroupRepository groupRepository;

	public static String getStringDate() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
		return dateFormat.format(calendar.getTime());
	}
}
