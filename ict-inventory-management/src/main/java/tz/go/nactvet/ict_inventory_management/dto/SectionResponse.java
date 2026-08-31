package tz.go.nactvet.ict_inventory_management.dto;

import java.time.LocalDateTime;

public class SectionResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Long directorateId;
    private String directorateName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SectionResponse() {
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

    public Long getDirectorateId() {
        return directorateId;
    }

    public void setDirectorateId(Long directorateId) {
        this.directorateId = directorateId;
    }

    public String getDirectorateName() {
        return directorateName;
    }

    public void setDirectorateName(String directorateName) {
        this.directorateName = directorateName;
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