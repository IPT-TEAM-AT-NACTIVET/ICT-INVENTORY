package tz.go.nactvet.ict_inventory_management.dto;

import tz.go.nactvet.ict_inventory_management.enums.Role;

public class UserResponse {

    private Long id;
    private String employeeId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean setupCompleted;
    private Role role;

    public UserResponse() {
    }

    public UserResponse(Long id, String employeeId, String fullName,
                        String email, String phoneNumber, boolean setupCompleted, Role role) {
        this.id = id;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.setupCompleted = setupCompleted;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
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

    public boolean isSetupCompleted() {
        return setupCompleted;
    }

    public void setSetupCompleted(boolean setupCompleted) {
        this.setupCompleted = setupCompleted;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}