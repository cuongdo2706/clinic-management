package cd.beapi.security.service;

import cd.beapi.entity.User;
import cd.beapi.enumerate.PageType;
import cd.beapi.repository.jpa.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRolePermissions(username)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with username: " + username));

        Set<GrantedAuthority> authorities = new HashSet<>();
        if (user.getRole() != null) {
            if (user.getRole().getCode() != null) {
                authorities.add(new SimpleGrantedAuthority(user.getRole().getCode()));
            }
            if (user.getRole().getName() != null) {
                authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));
            }
        }
        if (user.getRole() != null) {
            authorities.addAll(user.getRole().getPermissions().stream()
                    .filter(permission -> isSupportedPage(permission.getPage().getCode()))
                    .map(permission -> permission.getPage().getCode() + ":" + permission.getAction().getCode().name())
                    .filter(Objects::nonNull)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getIsActive()),
                true,
                true,
                !Boolean.TRUE.equals(user.getLocked()),
                List.copyOf(authorities)
        );
    }

    private static boolean isSupportedPage(String pageCode) {
        try {
            PageType.valueOf(pageCode);
            return true;
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return false;
        }
    }
}
