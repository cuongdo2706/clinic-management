package cd.beapi.security.service;

import cd.beapi.entity.User;
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
        authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));
        authorities.addAll(user.getRole().getPermissions().stream()
                .map(permission -> permission.getPage().getCode().name() + ":" + permission.getAction().getCode().name())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet()));

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), List.copyOf(authorities));
    }
}
