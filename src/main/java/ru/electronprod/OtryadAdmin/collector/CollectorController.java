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
import ru.electronprod.OtryadAdmin.utils.Answer;

@Controller
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_HEAD')")
class CollectorController {
	@Autowired
	private CandidateRepository rep;

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
		return ResponseEntity.accepted().body(Answer.success(candidate.toString()));
	}

	@PostMapping("/head/collector/remove")
	public ResponseEntity<String> collector_remove(@RequestBody int id) {
		rep.deleteById(id);
		return ResponseEntity.accepted().body(Answer.success());
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/head/collector/export")
	public ResponseEntity<String> collector_export() {
		JSONArray result = new JSONArray();
		rep.findAll().forEach(candidate -> {
			result.add(candidate.getJSON());
		});
		return ResponseEntity.ok().body(result.toJSONString());
	}
}
