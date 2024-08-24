package ru.electronprod.OtryadAdmin.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.electronprod.OtryadAdmin.data.repositories.DateEventRepository;
import ru.electronprod.OtryadAdmin.models.DateEvent;

@Service
public class DateEventService {
	@Autowired
	private DateEventRepository rep;

	@Transactional
	public void addDateEvent(DateEvent e) {
		rep.save(e);
	}

	@Transactional(readOnly = true)
	public JsonArray getDayEvents(String date) {
		JsonArray arr = new JsonArray();
		List<DateEvent> events = rep.findAllByDate(date);
		for (DateEvent event : events) {
			JsonObject obj = new JsonObject();
			obj.addProperty("content", event.getContent());
			obj.addProperty("by", event.getName());
			arr.add(obj);
		}
		if (arr.size() == 0) {
			JsonObject obj = new JsonObject();
			obj.addProperty("by", "Event Calendar");
			obj.addProperty("content", "Пока тут ничего нет :(");
			arr.add(obj);
		}
		return arr;
	}

	@Transactional(readOnly = true)
	public List<DateEvent> getAllData() {
		return rep.findAll(Sort.by(Sort.Direction.ASC, "date"));
	}
}