package com.peerlock.server.persistence;

public final class UserQueries {

    private UserQueries() {}

    public static final String INSERT_OR_UPDATE = """
        INSERT INTO users (username, password_hash, created_at)
        VALUES (?, ?, ?)
        ON CONFLICT(username) DO UPDATE SET
            password_hash = excluded.password_hash,
            created_at    = excluded.created_at
        """;

    public static final String FIND_BY_USERNAME = """
        SELECT username, password_hash, created_at
        FROM users
        WHERE username = ?
        """;

    public static final String EXISTS_BY_USERNAME = """
        SELECT 1
        FROM users
        WHERE username = ?
        """;
}
