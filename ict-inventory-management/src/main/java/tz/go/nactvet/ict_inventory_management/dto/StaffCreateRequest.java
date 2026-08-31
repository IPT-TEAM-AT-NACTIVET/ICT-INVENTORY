package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.NotBlank;

public class StaffCreateRequest {

    @NotBlank
    private String fullName;

    public StaffCreateRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}