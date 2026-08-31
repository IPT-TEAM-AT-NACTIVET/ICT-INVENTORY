package tz.go.nactvet.ict_inventory_management.dto;

import java.time.LocalDateTime;

public class ZoneResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private String status;
    private Long officeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ZoneResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getOfficeCount() {
        return officeCount;
    }

    public void setOfficeCount(Long officeCount) {
        this.officeCount = officeCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}