package ru.electronprod.OtryadAdmin.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public String handleException(Exception e, Model model) {
		model.addAttribute("errorMessage", e.getMessage());
		log.error("Error caught: ", e);
		return "public/error";
	}
}
