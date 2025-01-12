package ru.electronprod.OtryadAdmin.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO used for saving settings in text file
 */
@Data
@AllArgsConstructor
public class EventTypeDTO {
	private String event;
	private String name;
	private boolean canSetReason;
}
