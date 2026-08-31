package tz.go.nactvet.ict_inventory_management.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tz.go.nactvet.ict_inventory_management.dto.DeviceTypeResponse;
import tz.go.nactvet.ict_inventory_management.dto.DirectorateResponse;
import tz.go.nactvet.ict_inventory_management.dto.OfficeResponse;
import tz.go.nactvet.ict_inventory_management.dto.SectionResponse;
import tz.go.nactvet.ict_inventory_management.dto.UnitResponse;
import tz.go.nactvet.ict_inventory_management.dto.ZoneResponse;
import tz.go.nactvet.ict_inventory_management.service.DeviceTypeService;
import tz.go.nactvet.ict_inventory_management.service.DirectorateService;
import tz.go.nactvet.ict_inventory_management.service.OfficeService;
import tz.go.nactvet.ict_inventory_management.service.SectionService;
import tz.go.nactvet.ict_inventory_management.service.UnitService;
import tz.go.nactvet.ict_inventory_management.service.ZoneService;

@RestController
@RequestMapping("/reference")
public class ReferenceDataController {

    private final DirectorateService directorateService;
    private final SectionService sectionService;
    private final UnitService unitService;
    private final ZoneService zoneService;
    private final OfficeService officeService;
    private final DeviceTypeService deviceTypeService;

    public ReferenceDataController(DirectorateService directorateService,
                                   SectionService sectionService,
                                   UnitService unitService,
                                   ZoneService zoneService,
                                   OfficeService officeService,
                                   DeviceTypeService deviceTypeService) {
        this.directorateService = directorateService;
        this.sectionService = sectionService;
        this.unitService = unitService;
        this.zoneService = zoneService;
        this.officeService = officeService;
        this.deviceTypeService = deviceTypeService;
    }

    @GetMapping("/directorates")
    public ResponseEntity<List<DirectorateResponse>> getDirectorates() {
        return ResponseEntity.ok(directorateService.findAll());
    }

    @GetMapping("/sections")
    public ResponseEntity<List<SectionResponse>> getSections(
            @RequestParam(required = false) Long directorateId) {
        if (directorateId != null) {
            return ResponseEntity.ok(sectionService.findByDirectorateId(directorateId));
        }
        return ResponseEntity.ok(sectionService.findAll());
    }

    @GetMapping("/units")
    public ResponseEntity<List<UnitResponse>> getUnits() {
        return ResponseEntity.ok(unitService.findAll());
    }

    @GetMapping("/zones")
    public ResponseEntity<List<ZoneResponse>> getZones() {
        return ResponseEntity.ok(zoneService.findAll());
    }

    @GetMapping("/offices")
    public ResponseEntity<List<OfficeResponse>> getOffices(
            @RequestParam(required = false) Long zoneId) {
        if (zoneId != null) {
            return ResponseEntity.ok(officeService.findByZoneId(zoneId));
        }
        return ResponseEntity.ok(officeService.findAll());
    }

    @GetMapping("/device-types")
    public ResponseEntity<List<DeviceTypeResponse>> getDeviceTypes() {
        return ResponseEntity.ok(deviceTypeService.findAll());
    }
}