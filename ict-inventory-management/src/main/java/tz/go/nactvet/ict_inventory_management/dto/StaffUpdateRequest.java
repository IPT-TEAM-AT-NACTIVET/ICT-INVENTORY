package tz.go.nactvet.ict_inventory_management.dto;

import jakarta.validation.constraints.Email;

public class StaffUpdateRequest {

    private String fullName;

    @Email
    private String email;

    private String phoneNumber;

    private Long directorateId;

    private Long sectionId;

    private Long unitId;

    private Boolean enabled;

    public StaffUpdateRequest() {
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
