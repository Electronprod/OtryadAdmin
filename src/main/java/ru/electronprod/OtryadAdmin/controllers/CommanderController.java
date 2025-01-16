package ru.electronprod.OtryadAdmin.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.DBService;
import ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository;
import ru.electronprod.OtryadAdmin.models.Group;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.dto.MarkDTO;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.StatsWorker;
import ru.electronprod.OtryadAdmin.utils.Answer;
import ru.electronprod.OtryadAdmin.utils.SearchUtil;

@Slf4j
@Controller
@RequestMapping("/commander")
@PreAuthorize("hasAuthority('ROLE_COMMANDER')")
public class CommanderController {
	@Autowired
	private StatsWorker statsWorker;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;

	@GetMapping("")
	public String overview(Model model) {
		return "forward:/commander/mark";
	}

	@GetMapping("/mark")
	public String mark(Model model) {
		User user = dbservice.getUserRepository().findById(authHelper.getCurrentUser().getId()).orElseThrow();
		model.addAttribute("user", user);
		model.addAttribute("groups", user.getGroups().stream().filter(gr -> gr.isEditable()).toList());
		model.addAttribute("humanList",
				dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "commander/mark";
	}

	@PostMapping("/mark")
	public ResponseEntity<String> mark(@RequestBody MarkDTO dto) {
		try {
			int event_id = statsWorker.mark_only_present(dto, authHelper.getCurrentUser());
			return ResponseEntity.accepted().body(Answer.marked(event_id));
		} catch (Exception e) {
			log.error("Mark error (commander.mark):", e);
			return ResponseEntity.internalServerError().body(Answer.fail(e.getMessage()));
		}
	}

	@GetMapping("/markgroup")
	public String markGroup(Model model, @RequestParam int id) {
		Optional<Group> group1 = dbservice.getGroupRepository().findById(id);
		if (group1.isEmpty())
			return "forward:/commander/mark";
		model.addAttribute("humansList", group1.get().getHumans().stream()
				.sorted(Comparator.comparing(Human::getLastname)).collect(Collectors.toList()));
		model.addAttribute("group", group1.get());
		model.addAttribute("reasons", SettingsRepository.getReasons_for_absences());
		return "commander/mark_group";
	}

	@PostMapping("/mark_group")
	public ResponseEntity<String> markGroup(@RequestBody MarkDTO dto) {
		try {
			User user = authHelper.getCurrentUser();
			Group group = dbservice.getGroupRepository().findById(dto.getGroupID()).orElseThrow();
			int event_id = statsWorker.mark_group(dto, user,
					group.getHumans().stream().collect(Collectors.toCollection(ArrayList::new)), group.getName());
			return ResponseEntity.accepted().body(Answer.marked(event_id));
		} catch (Exception e) {
			log.error("Mark error (commander.mark_group):", e);
			return ResponseEntity.internalServerError().body(Answer.fail(e.getMessage()));
		}
	}

	@GetMapping("/stats")
	public String stats_overview() {
		return "commander/stats_overview";
	}

	@GetMapping("/stats/date")
	public String stats_byDateTable(@RequestParam String date, Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("statss", dbservice.getStatsRepository().findByDateAndAuthor(date.replaceAll("-", "."),
				user.getLogin(), Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/alltable")
	public String stats_allTable(Model model) {
		User user = authHelper.getCurrentUser();
		model.addAttribute("statss",
				dbservice.getStatsRepository().findByAuthor(user.getLogin(), Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/stats/personal")
	public String personalStatsTable(@RequestParam String name, Model model) {
		User user = authHelper.getCurrentUser();
		Human human = SearchUtil.findMostSimilarHuman(name, dbservice.getHumanRepository().findAll());
		if (human == null || human.getStats() == null) {
			return "redirect:/commander/stats?error_notfound";
		}
		model.addAttribute("statss", dbservice.getStatsRepository().findByHumanAndAuthor(human, user.getLogin(),
				Sort.by(Sort.Direction.DESC, "id")));
		return "public/statsview_rawtable";
	}

	@GetMapping("/humans")
	public String getHumansData(Model model) {
		model.addAttribute("humans", dbservice.getHumanRepository().findAll(Sort.by(Sort.Direction.ASC, "lastname")));
		return "public/humans_rawtable";
	}
}
