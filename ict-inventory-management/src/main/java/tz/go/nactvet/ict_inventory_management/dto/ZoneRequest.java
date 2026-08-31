package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ZoneRequest {

    @NotBlank
    private String name;

    private String code;

    private String description;

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "must be ACTIVE or INACTIVE")
    private String status;

    public ZoneRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}