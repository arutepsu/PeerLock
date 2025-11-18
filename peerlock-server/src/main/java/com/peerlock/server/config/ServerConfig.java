package com.peerlock.server.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.peerlock.server.persistence.JdbcUserRepository;
import com.peerlock.server.repository.InMemoryPeerRegistry;
import com.peerlock.server.repository.PeerRegistry;
import com.peerlock.server.repository.SessionTokenStore;
import com.peerlock.server.repository.UserRepository;
import com.peerlock.server.security.TokenAuthenticationFilter;
import com.peerlock.server.service.AuthService;

@Configuration
public class ServerConfig {

    @Bean
    public PeerRegistry peerRegistry() {
        return new InMemoryPeerRegistry();
    }
    @Bean
    public UserRepository userRepository(DataSource dataSource) {
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    public SessionTokenStore sessionTokenStore() {
        return new SessionTokenStore();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(AuthService authService) {
        return new TokenAuthenticationFilter(authService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenAuthenticationFilter tokenFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DataSource dataSource() {
        String userHome = System.getProperty("user.home");
        Path dbDir = Paths.get(userHome, ".peerlock", "server");
        try {
            Files.createDirectories(dbDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create DB directory: " + dbDir, e);
        }

        Path dbFile = dbDir.resolve("peerlock.db");
        String url = "jdbc:sqlite:" + dbFile.toAbsolutePath();

        org.sqlite.SQLiteDataSource ds = new org.sqlite.SQLiteDataSource();
        ds.setUrl(url);
        return ds;
    }
}
