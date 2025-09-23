package ru.electronprod.OtryadAdmin.models.dto;

import org.json.simple.JSONArray;
import lombok.Data;

/**
 * DTO that is used process "mark" requests
 */
@Data
public class MarkDTO {
	private String event;
	private String date;
	private JSONArray unpresentPeople;
	private JSONArray presentPeople;
	private int groupID;
}