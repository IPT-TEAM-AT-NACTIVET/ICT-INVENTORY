package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordChangeRequest {

    @NotBlank(message = "currentPassword: must not be blank")
    private String currentPassword;

    @NotBlank(message = "newPassword: must not be blank")
    @Size(min = 6, message = "newPassword: must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "confirmPassword: must not be blank")
    private String confirmPassword;

    public PasswordChangeRequest() {
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}