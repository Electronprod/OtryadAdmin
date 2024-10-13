package ru.electronprod.OtryadAdmin.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.electronprod.OtryadAdmin.data.services.DBService;
import ru.electronprod.OtryadAdmin.models.User;
import ru.electronprod.OtryadAdmin.security.AuthHelper;
import ru.electronprod.OtryadAdmin.services.ExcelGenerationService;

@RestController
public class DownloadController {
	@Autowired
	private ExcelGenerationService genServ;
	@Autowired
	private DBService dbservice;
	@Autowired
	private AuthHelper authHelper;

	@GetMapping("/download/data")
	public ResponseEntity<byte[]> downloadHumansList() throws IOException {
		User user = authHelper.getCurrentUser();
		if (user == null)
			return ResponseEntity.badRequest().build();
		if (user.getRole().equals("ROLE_OBSERVER") || user.getRole().equals("ROLE_ADMIN")
				|| user.getRole().equals("ROLE_COMMANDER")) {
			String sheetName = dbservice.getStringDate() + "-allhumans.xlsx";
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + sheetName)
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(genServ.generateHuman(dbservice.getHumanService().findAll(), sheetName));
		} else if (user.getRole().equals("ROLE_SQUADCOMMANDER")) {
			String sheetName = dbservice.getStringDate() + "-squadhumans.xlsx";
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + sheetName)
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(genServ.generateHuman(
							dbservice.getHumanService().findBySquad(
									dbservice.getUserService().findById(user.getId()).orElseThrow().getSquad()),
							sheetName));
		} else {
			return ResponseEntity.badRequest().build();
		}
	}
}
