package com.peerlock.server.service;

import com.peerlock.common.dto.AuthRequest;
import com.peerlock.common.dto.AuthResponse;
import com.peerlock.server.domain.UserAccount;
import com.peerlock.server.repository.SessionTokenStore;
import com.peerlock.server.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Handles user registration, login, and token handling.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final SessionTokenStore tokenStore;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       SessionTokenStore tokenStore,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        String hash = passwordEncoder.encode(request.password());
        UserAccount account = new UserAccount(
                request.username(),
                hash,
                Instant.now()
        );
        userRepository.save(account);

        String token = issueTokenForUser(account.username());
        return new AuthResponse(account.username(), token);
    }

    public AuthResponse login(AuthRequest request) {
        var user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = issueTokenForUser(user.username());
        return new AuthResponse(user.username(), token);
    }

    /**
     * Resolve username from an access token.
     */
    public String getUsernameFromToken(String token) {
        return tokenStore.findUsernameByToken(token)
                .orElseThrow(InvalidTokenException::new);
    }

    private String issueTokenForUser(String username) {
        String token = UUID.randomUUID().toString();
        tokenStore.store(token, username);
        return token;
    }
}
