package ru.electronprod.OtryadAdmin.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventTypeDTO {
	private String event;
	private String name;
	private boolean canSetReason;
}
