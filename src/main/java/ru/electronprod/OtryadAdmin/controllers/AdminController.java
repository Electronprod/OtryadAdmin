package ru.electronprod.OtryadAdmin.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.NonNull;
import ru.electronprod.OtryadAdmin.data.repositories.HumanRepository;
import ru.electronprod.OtryadAdmin.data.repositories.SquadRepository;
import ru.electronprod.OtryadAdmin.data.repositories.UserRepository;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.services.AdminService;
import ru.electronprod.OtryadAdmin.services.auth.AuthService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
	@Autowired
	private AuthService authservice;
	@Autowired
	private HumanRepository humanrep;
	@Autowired
	private SquadRepository squadrep;
	@Autowired
	private UserRepository userrep;
	@Autowired
	private AdminService adminservice;

	/*
	 * User manager
	 */
	@GetMapping("/usermgr")
	public String userManager(Model model) {
		List<User> users = userrep.findAll();
		model.addAttribute("users", users);
		return "admin/usermanager";
	}

	@GetMapping("/usermgr/add")
	public String userManager_add(Model model) {
		User user = new User();
		model.addAttribute("user", user);
		return "admin/usermanager_add";
	}

	@PostMapping("/usermgr/add")
	public String userManager_addAction(@ModelAttribute("user") User user) {
		authservice.register(user);
		return "redirect:/admin/usermgr?saved";
	}

	@GetMapping("/usermgr/edit")
	public String userManager_edit(@RequestParam int id, Model model) {
		Optional<User> user = userrep.findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		if (adminservice.isNativeAdmin(user.get())) {
			return "redirect:/admin/usermgr?error_protected";
		}
		model.addAttribute("user", user);
		return "admin/usermanager_edit";
	}

	@PostMapping("/usermgr/edit")
	public String userManager_editAction(@ModelAttribute("user") User user) {
		// user.setSquad(userrep.findById(user.getId()).get().getSquad());
		authservice.register(user);
		return "redirect:/admin/usermgr?edited";
	}

	@GetMapping("/usermgr/delete")
	public String userManager_delete(@RequestParam() int id) {
		Optional<User> user = userrep.findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		if (adminservice.isNativeAdmin(user.get())) {
			return "redirect:/admin/usermgr?error_protected";
		}
		userrep.deleteById(id);
		return "redirect:/admin/usermgr?deleted";
	}

	/*
	 * Squad manager
	 */
	@GetMapping("/squadmgr")
	public String squadManager(Model model) {
		List<Squad> squads = squadrep.findAll();
		model.addAttribute("squads", squads);
		return "admin/squadmanager";
	}

	/**
	 * @param id    - commander user_id
	 * @param model
	 * @return
	 */
	@GetMapping("/squadmgr/add")
	public String squadManager_add(@RequestParam() int id, Model model) {
		Squad squad = new Squad();
		Optional<User> usr = userrep.findById(id);
		if (usr.isEmpty() || !usr.get().getRole().equals("ROLE_SQUADCOMMANDER")) {
			return "redirect:/admin/squadmgr?error";
		}
		squad.setCommander(id);
		model.addAttribute("squad", squad);
		return "admin/squadmanager_add";
	}

	@PostMapping("/squadmgr/add")
	public String squadManager_addAction(@ModelAttribute("squad") Squad squad) {
		squadrep.save(squad);
		return "redirect:/admin/squadmgr?saved";
	}

	@GetMapping("/squadmgr/edit")
	public String squadManager_edit(@RequestParam() int id, Model model) {
		Optional<Squad> squad = squadrep.findById(id);
		if (squad.isEmpty())
			return "redirect:/admin/squadmgr?error";
		model.addAttribute("squad", squad);
		return "admin/squadmanager_edit";
	}

	@PostMapping("/squadmgr/edit")
	public String squadManager_edit(@ModelAttribute("squad") Squad squad) {
		squadrep.save(squad);
		return "redirect:/admin/squadmgr?edited";
	}

	@GetMapping("/squadmgr/delete")
	public String squadManager_delete(@RequestParam() int id) {
		if (squadrep.existsById(id) == false) {
			return "redirect:/admin/squadmgr?error";
		}
		squadrep.deleteById(id);
		return "redirect:/admin/squadmgr?deleted";
	}

	/*
	 * Human manager
	 */
	@GetMapping("/humanmgr")
	public String humanManager(Model model) {
		List<Human> humans = humanrep.findAll(Sort.by(Sort.Direction.ASC, "lastname"));
		model.addAttribute("humans", humans);
		return "admin/humanmanager";
	}

	@GetMapping("/humanmgr/delete")
	public String humanManager_delete(@RequestParam() int id) {
		if (humanrep.existsById(id) == false) {
			return "redirect:/admin/humanmgr?error";
		}
		humanrep.deleteById(id);
		return "redirect:/admin/humanmgr?deleted";
	}

	@GetMapping("/humanmgr/add")
	public String humanManager_add(Model model) {
		Human human = new Human();
		human.setYear_of_admission(2024);
		human.setClassnum(6);
		model.addAttribute("human", human);
		return "admin/humanmanager_add";
	}

	@PostMapping("/humanmgr/add")
	public String humanManager_addAction(@ModelAttribute("human") Human human) {
		if (!squadrep.existsById(Integer.parseInt(human.getSquadid()))) {
			return "redirect:/admin/humanmgr?errorsquad";
		}
		humanrep.save(human);
		return "redirect:/admin/humanmgr?added";
	}

	@GetMapping("/humanmgr/edit")
	public String humanManager_edit(@RequestParam int id, Model model) {
		Optional<Human> human = humanrep.findById(id);
		if (human.isEmpty()) {
			return "redirect:/admin/humanmgr?error";
		}
		model.addAttribute("human", human);
		return "admin/humanmanager_edit";
	}

	@PostMapping("/humanmgr/edit")
	public String humanManager_editAction(@ModelAttribute("human") Human human) {
		if (!squadrep.existsById(Integer.parseInt(human.getSquadid()))) {
			return "redirect:/admin/humanmgr?errorsquad";
		}
		humanrep.save(human);
		return "redirect:/admin/humanmgr?edited";
	}
}
