package ru.electronprod.OtryadAdmin.utils;

import org.json.simple.JSONObject;

public class Answer {
	public static String success() {
		return "{\"result\": \"success\"}";
	}

	@SuppressWarnings("unchecked")
	public static String fail(String message) {
		JSONObject o = new JSONObject();
		o.put("result", "fail");
		o.put("message", message);
		return o.toJSONString();
	}

	@SuppressWarnings("unchecked")
	public static String marked(int event_id) {
		JSONObject o = new JSONObject();
		o.put("result", "success");
		o.put("event_id", String.valueOf(event_id));
		return o.toJSONString();
	}
}
