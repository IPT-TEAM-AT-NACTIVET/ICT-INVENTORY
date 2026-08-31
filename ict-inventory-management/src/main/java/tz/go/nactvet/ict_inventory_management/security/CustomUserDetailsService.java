package tz.go.nactvet.ict_inventory_management.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import tz.go.nactvet.ict_inventory_management.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException {
        tz.go.nactvet.ict_inventory_management.entity.User appUser = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + principal)));

        return new UserPrincipal(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getPassword(),
                appUser.isEnabled(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
        );
    }

    public static class UserPrincipal implements UserDetails {

        private final Long id;
        private final String username;
        private final String password;
        private final boolean enabled;
        private final Collection<? extends GrantedAuthority> authorities;

        public UserPrincipal(Long id, String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
            this.id = id;
            this.username = username;
            this.password = password;
            this.enabled = enabled;
            this.authorities = authorities;
        }

        public Long getId() {
            return id;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
