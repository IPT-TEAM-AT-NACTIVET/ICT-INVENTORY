package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserManagementCreateRequest {

    @NotBlank(message = "fullName: must not be blank")
    private String fullName;

    @NotBlank(message = "email: must not be blank")
    @Email(message = "email: must be a valid email address")
    private String email;

    @NotBlank(message = "phoneNumber: must not be blank")
    @Size(max = 30, message = "phoneNumber: must not exceed 30 characters")
    private String phoneNumber;

    @NotBlank(message = "password: must not be blank")
    @Size(min = 6, message = "password: must be at least 6 characters")
    private String password;

    private Long directorateId;

    private Long sectionId;

    private Long unitId;

    public UserManagementCreateRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getDirectorateId() {
        return directorateId;
    }

    public void setDirectorateId(Long directorateId) {
        this.directorateId = directorateId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }
}
