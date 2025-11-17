package com.peerlock.server.config;

import com.peerlock.server.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ServerConfig {

    @Bean
    public PeerRegistry peerRegistry() {
        return new InMemoryPeerRegistry();
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    public SessionTokenStore sessionTokenStore() {
        return new SessionTokenStore();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
