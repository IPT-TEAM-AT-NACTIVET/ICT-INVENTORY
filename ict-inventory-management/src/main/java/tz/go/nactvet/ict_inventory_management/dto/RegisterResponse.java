package tz.go.nactvet.ict_inventory_management.dto;

import tz.go.nactvet.ict_inventory_management.enums.Role;

public class RegisterResponse {

    private String message;
    private String employeeId;
    private String email;
    private Role role;
    private String status;

    public RegisterResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
