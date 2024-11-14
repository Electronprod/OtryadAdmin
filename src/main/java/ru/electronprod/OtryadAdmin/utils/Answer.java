package ru.electronprod.OtryadAdmin.utils;

import org.json.simple.JSONObject;

public class Answer {
	public static String success() {
		return "{\"result\": \"success\"}";
	}

	public static String fail(String message) {
		JSONObject o = new JSONObject();
		o.put("result", "fail");
		o.put("message", message);
		return o.toJSONString();
	}
}
