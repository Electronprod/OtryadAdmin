package ru.electronprod.OtryadAdmin.services;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
public class OptionService implements InitializingBean {
	@Getter
	private Map<String, String> event_types = new LinkedHashMap<String, String>();
	@Getter
	private Map<String, String> reasons_for_absences = new LinkedHashMap<String, String>();

	@Override
	public void afterPropertiesSet() {
		event_types.put("general", "Посещение общего сбора");
		event_types.put("duty", "Дежурство (вызвался(ась) дежурить в кабинете)");
		event_types.put("walk", "Прогулка (гулял(а) со звеном)");
		event_types.put("other1", "Другое #1 (используйте эту категорию по своему усмотрению)");
		event_types.put("other2", "Другое #2 (используйте эту категорию по своему усмотрению)");

		reasons_for_absences.put("ill", "Заболел(а)");
		reasons_for_absences.put("away", "Уехал(а)");
		reasons_for_absences.put("study", "Учеба");
		reasons_for_absences.put("respect", "Уважительная причина");
		reasons_for_absences.put("disrespect", "Неуважительная причина");

	}
}
