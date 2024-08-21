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

import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.Human;
import ru.electronprod.OtryadAdmin.models.Squad;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.models.helpers.HumanHelper;
import ru.electronprod.OtryadAdmin.models.helpers.SquadHelper;
import ru.electronprod.OtryadAdmin.services.AdminService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
	@Autowired
	private DBService dbservice;
	@Autowired
	private AdminService adminService;

	/*
	 * Main page
	 */
	@GetMapping("")
	public String dash() {
		return "admin/dashboard";
	}

	/*
	 * User manager
	 */
	@GetMapping("/usermgr")
	public String usermgr(Model model) {
		model.addAttribute("users", dbservice.getUserService().findAll());
		return "admin/usermgr/usermgr";
	}

	@GetMapping("/usermgr/add")
	public String userManager_add(Model model) {
		model.addAttribute("user", new User());
		return "admin/usermgr/usermgr_add";
	}

	@PostMapping("/usermgr/add")
	public String userManager_addAction(@ModelAttribute("user") User user) {
		dbservice.getUserService().register(user);
		return "redirect:/admin/usermgr?saved";
	}

	@GetMapping("/usermgr/edit")
	public String userManager_edit(@RequestParam int id, Model model) {
		Optional<User> user = dbservice.getUserService().findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		if (adminService.isNativeAdmin(user.get())) {
			return "redirect:/admin/usermgr?error_protected";
		}
		model.addAttribute("user", user);
		return "admin/usermgr/usermgr_edit";
	}

	@PostMapping("/usermgr/edit")
	public String userManager_editAction(@ModelAttribute("user") User user) {
		dbservice.getUserService().register(user);
		return "redirect:/admin/usermgr?edited";
	}

	@GetMapping("/usermgr/delete")
	public String userManager_delete(@RequestParam() int id) {
		Optional<User> user = dbservice.getUserService().findById(id);
		if (user.isEmpty()) {
			return "redirect:/admin/usermgr?error";
		}
		if (adminService.isNativeAdmin(user.get())) {
			return "redirect:/admin/usermgr?error_protected";
		}
		dbservice.getUserService().deleteById(id);
		return "redirect:/admin/usermgr?deleted";
	}

	/*
	 * Squad manager
	 */
	@GetMapping("/squadmgr")
	public String squadManager(Model model) {
		model.addAttribute("squads", dbservice.getSquadService().findAll());
		return "admin/squadmgr/squadmgr";
	}

	// id - from usermgr
	@GetMapping("/squadmgr/add")
	public String squadManager_add(@RequestParam() int id, Model model) {
		Optional<User> usr = dbservice.getUserService().findById(id);
		if (usr.isEmpty() || !usr.get().getRole().equals("ROLE_SQUADCOMMANDER")) {
			return "redirect:/admin/squadmgr?error";
		}
		if (usr.get().getSquad() != null) {
			return "redirect:/admin/squadmgr?error_hasvalue";
		}
		SquadHelper squadHelper = new SquadHelper();
		squadHelper.setCommander_id(id);
		squadHelper.setCommanderName(usr.get().getLogin());
		model.addAttribute("squadHelper", squadHelper);
		return "admin/squadmgr/squadmgr_add";
	}

	@PostMapping("/squadmgr/add")
	public String squadManager_addAction(@ModelAttribute("squadHelper") SquadHelper squadHelper) {
		Squad squad = new Squad();
		squad.setSquadName(squadHelper.getSquadName());
		squad.setCommanderName(squadHelper.getCommanderName());
		squad.setCommander(dbservice.getUserService().findById(squadHelper.getCommander_id()).orElseThrow());
		dbservice.getSquadService().save(squad);
		return "redirect:/admin/squadmgr?saved";
	}

	@GetMapping("/squadmgr/edit")
	public String squadManager_edit(@RequestParam() int id, Model model) {
		Optional<Squad> squad = dbservice.getSquadService().findById(id);
		if (squad.isEmpty())
			return "redirect:/admin/squadmgr?error";
		SquadHelper squadHelper = new SquadHelper();
		squadHelper.setCommanderName(squad.get().getCommanderName());
		squadHelper.setCommander_id(squad.get().getCommander().getId());
		squadHelper.setSquadName(squad.get().getSquadName());
		squadHelper.setId(id);
		model.addAttribute("squadHelper", squadHelper);
		return "admin/squadmgr/squadmgr_edit";
	}

	@PostMapping("/squadmgr/edit")
	public String squadManager_edit(@ModelAttribute("squad") SquadHelper squadHelper) {
		Squad squad = dbservice.getSquadService().findById(squadHelper.getId()).orElseThrow();
		squad.setCommanderName(squadHelper.getCommanderName());
		squad.setSquadName(squadHelper.getSquadName());
		Optional<User> usr = dbservice.getUserService().findById(squadHelper.getCommander_id());
		if (usr.isEmpty() || !usr.get().getRole().equals("ROLE_SQUADCOMMANDER")) {
			return "redirect:/admin/squadmgr?error";
		}
		if (usr.get().getSquad() != null) {
			return "redirect:/admin/squadmgr?error_hasvalue";
		}
		squad.setCommander(usr.get());
		dbservice.getSquadService().save(squad);
		return "redirect:/admin/squadmgr?edited";
	}

	@GetMapping("/squadmgr/delete")
	public String squadManager_delete(@RequestParam() int id) {
		if (dbservice.getSquadService().findById(id).isEmpty()) {
			return "redirect:/admin/squadmgr?error";
		}
		dbservice.getSquadService().deleteById(id);
		return "redirect:/admin/squadmgr?deleted";
	}

	/*
	 * Human manager
	 */
	@GetMapping("/humanmgr")
	public String humanManager(Model model) {
		List<Human> humans = dbservice.getHumanService().findAll(Sort.by(Sort.Direction.ASC, "lastname"));
		model.addAttribute("humans", humans);
		return "admin/humanmgr/humanmgr";
	}

	// id - squad_id
	@GetMapping("/humanmgr/add")
	public String humanManager_add(@RequestParam() int id, Model model) {
		// Checking existence
		if (dbservice.getSquadService().findById(id).isEmpty())
			return "redirect:/admin/humanmgr?errorsquad";

		HumanHelper helper = new HumanHelper();
		helper.setSquad_id(id);
		// Setting default values
		helper.setClassnum(6);
		helper.setClasschar("А");
		helper.setDedicated(false);
		model.addAttribute("humanHelper", helper);
		return "admin/humanmgr/humanmgr_add";
	}

	@PostMapping("/humanmgr/add")
	public String humanManager_addAction(@ModelAttribute("humanHelper") HumanHelper helper) {
		Optional<Squad> squad = dbservice.getSquadService().findById(helper.getSquad_id());
		// Checking existence
		if (squad.isEmpty())
			return "redirect:/admin/humanmgr?errorsquad";

		// Generating human object
		Human human = HumanHelper.fillDefaultValues(helper);
		helper = null; // helping cleaner
		human.setSquad(squad.get());
		// Saving to DB
		dbservice.getHumanService().save(human);
		return "redirect:/admin/humanmgr?saved";
	}

	@GetMapping("/humanmgr/delete")
	public String humanManager_delete(@RequestParam() int id) {
		if (dbservice.getHumanService().findById(id).isEmpty()) {
			return "redirect:/admin/humanmgr?error_notfound";
		}
		dbservice.getHumanService().deleteById(id);
		return "redirect:/admin/humanmgr?deleted";
	}

	@GetMapping("/humanmgr/edit")
	public String humanManager_edit(@RequestParam int id, Model model) {
		Optional<Human> human = dbservice.getHumanService().findById(id);
		// Checking existence (Human)
		if (human.isEmpty())
			return "redirect:/admin/humanmgr?error_notfound";
		HumanHelper helper = HumanHelper.fillDefaultValues(human.get());
		helper.setSquad_id(human.get().getSquad().getId());
		model.addAttribute("humanHelper", helper);
		return "admin/humanmgr/humanmgr_edit";
	}

	@PostMapping("/humanmgr/edit")
	public String humanManager_editAction(@ModelAttribute("humanHelper") HumanHelper helper) {
		Optional<Human> human1 = dbservice.getHumanService().findById(helper.getId());
		// Checking existence (Human)
		if (human1.isEmpty())
			return "redirect:/admin/humanmgr?error_notfound";
		Human human = human1.get();
		// If Squad changed
		if (human.getSquad().getId() != helper.getSquad_id()) {
			// Checking existence (new Squad)
			Optional<Squad> squad = dbservice.getSquadService().findById(helper.getSquad_id());
			if (squad.isEmpty())
				return "redirect:/admin/humanmgr?errorsquad";
			// Deleting human from old squad
			Squad oldsquad = dbservice.getSquadService().findById(human.getSquad().getId()).orElseThrow();
			oldsquad.getHumans().remove(human);
			dbservice.getSquadService().save(oldsquad);
			// Adding to new squad
			Squad newSquad = squad.get();
			human.setSquad(newSquad);
			newSquad.getHumans().add(human);
			dbservice.getSquadService().save(newSquad);
		}
		// Updating default values
		human = HumanHelper.fillDefaultValues(helper, human);
		dbservice.getHumanService().save(human);
		return "redirect:/admin/humanmgr?edited";
	}
}
