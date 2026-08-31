package tz.go.nactvet.ict_inventory_management.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import tz.go.nactvet.ict_inventory_management.dto.ProfileResponse;
import tz.go.nactvet.ict_inventory_management.dto.ProfileUpdateRequest;
import tz.go.nactvet.ict_inventory_management.security.CustomUserDetailsService;
import tz.go.nactvet.ict_inventory_management.service.ProfileService;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getProfile(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest request,
                                                           Authentication authentication) {
        CustomUserDetailsService.UserPrincipal principal = (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.updateProfile(principal.getId(), request));
    }
}
