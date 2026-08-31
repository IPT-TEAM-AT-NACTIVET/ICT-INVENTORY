package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.NotBlank;

public class RejectRequest {

    @NotBlank(message = "rejectionReason: must not be blank")
    private String rejectionReason;

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
