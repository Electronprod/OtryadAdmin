package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

	@RequestMapping("/error")
	@ResponseBody
	public String handleError(HttpServletRequest request) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());

			if (statusCode == 404) {
				return "404 - Page Not Found";
			} else if (statusCode == 500) {
				return "500 - Internal Server Error";
			} else if (statusCode == 403) {
				return "403 - Forbidden";
			} else {
				return "Error " + statusCode;
			}
		}
		return "Unknown Error";
	}
}
