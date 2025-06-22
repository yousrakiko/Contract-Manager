package contract.management.bea.CONTRACT_MANAGER.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import contract.management.bea.CONTRACT_MANAGER.model.User;
import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;

@Service
public class UserDetailsServiceimpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceimpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
  @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByMatricule(username);
    if (user == null) {
        throw new UsernameNotFoundException("User not found");
    }
    if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new DisabledException("Account is not active");
        }

    String roleName = user.getRole().getRoleName().toUpperCase();

    return org.springframework.security.core.userdetails.User.builder()
            .username(user.getMatricule())
            .password(user.getPassword())
            .roles(roleName)  
            .build();
}

    
}