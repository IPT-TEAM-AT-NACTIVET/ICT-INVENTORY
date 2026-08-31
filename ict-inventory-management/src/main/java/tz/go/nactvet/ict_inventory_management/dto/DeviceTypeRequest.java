package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceTypeRequest {

    @NotBlank(message = "name: must not be blank")
    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
