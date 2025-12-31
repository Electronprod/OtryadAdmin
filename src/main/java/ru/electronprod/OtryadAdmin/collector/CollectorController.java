package ru.electronprod.OtryadAdmin.collector;

import java.util.Optional;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.electronprod.OtryadAdmin.models.ActionRecordType;
import ru.electronprod.OtryadAdmin.services.AuthHelper;
import ru.electronprod.OtryadAdmin.services.RecordService;
import ru.electronprod.OtryadAdmin.utils.Answer;

@Controller
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_HEAD')")
class CollectorController {
	@Autowired
	private CandidateRepository rep;
	@Autowired
	private RecordService rec;
	@Autowired
	private AuthHelper auth;

	@GetMapping("/head/collector")
	public String collector() {
		return "public/collector";
	}

	@GetMapping("/head/collector/data")
	public String collector_table(Model model) {
		model.addAttribute("candidates", rep.findAll());
		return "public/collector_table";
	}

	@PostMapping("/head/collector/profile")
	public ResponseEntity<String> collector_profile(@RequestBody int id) {
		Optional<Candidate> c1 = rep.findById(id);
		if (c1.isEmpty())
			return ResponseEntity.status(404).body(Answer.fail("The candidate not found"));
		return ResponseEntity.ok().body(c1.get().getJSON().toJSONString());
	}

	@PostMapping("/head/collector/add")
	public ResponseEntity<String> collector_add(@RequestBody Candidate candidate) {
		rep.save(candidate);
		rec.recordAction(auth.getCurrentUser(),
				"Added a new candidate (%s %s)".formatted(candidate.getFirstname(), candidate.getSurname()),
				ActionRecordType.COLLECTOR);
		return ResponseEntity.accepted().body(Answer.success(candidate.toString()));
	}

	@PostMapping("/head/collector/remove")
	public ResponseEntity<String> collector_remove(@RequestBody int id) {
		rep.deleteById(id);
		rec.recordAction(auth.getCurrentUser(), "Removed candidate ID: %d".formatted(id), ActionRecordType.COLLECTOR);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/head/collector/export")
	public ResponseEntity<String> collector_export() {
		JSONArray result = new JSONArray();
		rep.findAll().forEach(candidate -> {
			result.add(candidate.getJSON());
		});
		rec.recordAction(auth.getCurrentUser(), "Exported the candidates table.", ActionRecordType.COLLECTOR);
		return ResponseEntity.ok().body(result.toJSONString());
	}
}
