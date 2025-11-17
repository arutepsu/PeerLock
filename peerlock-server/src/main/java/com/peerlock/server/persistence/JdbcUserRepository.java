package com.peerlock.server.persistence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

import javax.sql.DataSource;

import com.peerlock.server.domain.UserAccount;
import static com.peerlock.server.persistence.UserQueries.EXISTS_BY_USERNAME;
import static com.peerlock.server.persistence.UserQueries.FIND_BY_USERNAME;
import static com.peerlock.server.persistence.UserQueries.INSERT_OR_UPDATE;
import com.peerlock.server.repository.UserRepository;

public class JdbcUserRepository implements UserRepository {

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(UserAccount user) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_OR_UPDATE)) {

            ps.setString(1, user.username());
            ps.setString(2, user.passwordHash());
            ps.setTimestamp(3, Timestamp.from(user.createdAt()));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user " + user.username(), e);
        }
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UserAccount(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getTimestamp("created_at").toInstant()
                    ));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user " + username, e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_BY_USERNAME)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check existence of user " + username, e);
        }
    }
}
