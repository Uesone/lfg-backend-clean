package com.lfg.lfg_backend.ServiceImpl;


import com.lfg.lfg_backend.model.User;
import com.lfg.lfg_backend.repository.UserRepository;
import com.lfg.lfg_backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato: " + usernameOrEmail));

        return new UserDetailsImpl(user);
    }

}
