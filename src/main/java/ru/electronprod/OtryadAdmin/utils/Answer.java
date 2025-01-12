package ru.electronprod.OtryadAdmin.utils;

import org.json.simple.JSONObject;

/**
 * This class contains builders for network answers
 */
public class Answer {
	/**
	 * OK
	 * 
	 * @return String answer
	 */
	public static String success() {
		return "{\"result\": \"success\"}";
	}

	/**
	 * FAIL
	 * 
	 * @param message - message to send to client
	 * @return String answer
	 */
	@SuppressWarnings("unchecked")
	public static String fail(String message) {
		JSONObject o = new JSONObject();
		o.put("result", "fail");
		o.put("message", message);
		return o.toJSONString();
	}

	/**
	 * MARKED
	 * 
	 * @param event_id - eventID of the marked event
	 * @return String answer
	 */
	@SuppressWarnings("unchecked")
	public static String marked(int event_id) {
		JSONObject o = new JSONObject();
		o.put("result", "success");
		o.put("event_id", String.valueOf(event_id));
		return o.toJSONString();
	}
}
