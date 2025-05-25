package ru.electronprod.OtryadAdmin.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO used for saving settings in
 * {@link ru.electronprod.OtryadAdmin.data.filesystem.SettingsRepository}
 */
@Data
@AllArgsConstructor
public class EventTypeDTO {
	private String event;
	private String name;
	private boolean canSetReason;
}
