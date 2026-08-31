package tz.go.nactvet.ict_inventory_management.service;

import org.springframework.stereotype.Component;

import tz.go.nactvet.ict_inventory_management.entity.Directorate;
import tz.go.nactvet.ict_inventory_management.entity.Section;
import tz.go.nactvet.ict_inventory_management.exception.BadRequestException;

@Component
public class OrganizationalValidator {

    public void validate(Directorate directorate, Section section) {
        if (directorate != null && section != null && !section.getDirectorate().getId().equals(directorate.getId())) {
            throw new BadRequestException(
                    "Section '" + section.getName() + "' does not belong to directorate '" + directorate.getName() + "'");
        }
    }
}
