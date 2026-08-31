package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OfficeRequest {

    @NotNull
    private Long zoneId;

    @NotBlank
    @Size(max = 50)
    private String officeCode;

    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "must be ACTIVE or INACTIVE")
    private String status;

    public OfficeRequest() {
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getOfficeCode() {
        return officeCode;
    }

    public void setOfficeCode(String officeCode) {
        this.officeCode = officeCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}