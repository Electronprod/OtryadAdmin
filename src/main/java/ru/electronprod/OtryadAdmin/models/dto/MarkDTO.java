package ru.electronprod.OtryadAdmin.models.dto;

import org.json.simple.JSONArray;

import lombok.Data;

@Data
public class MarkDTO {
	private String event;
	private String date;
	private JSONArray unpresentPeople;
	private JSONArray presentPeople;
}
