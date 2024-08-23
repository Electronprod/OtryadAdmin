package ru.electronprod.OtryadAdmin.models.helpers;

import lombok.*;
import java.util.Map;

@Getter
@Setter
public class StatsFormHelper {
    private Map<Integer, String> details;

    // Getters and Setters
    public Map<Integer, String> getDetails() {
        return details;
    }

    public void setDetails(Map<Integer, String> details) {
        this.details = details;
    }
}
