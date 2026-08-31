package tz.go.nactvet.ict_inventory_management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tz.go.nactvet.ict_inventory_management.dto.DashboardResponse;
import tz.go.nactvet.ict_inventory_management.dto.StaffDashboardResponse;
import tz.go.nactvet.ict_inventory_management.security.CustomUserDetailsService;
import tz.go.nactvet.ict_inventory_management.service.DashboardService;

@RestController
@RequestMapping
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<DashboardResponse> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/staff/dashboard")
    public ResponseEntity<StaffDashboardResponse> getStaffDashboard(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(dashboardService.getStaffDashboard(principal.getId()));
    }
}
