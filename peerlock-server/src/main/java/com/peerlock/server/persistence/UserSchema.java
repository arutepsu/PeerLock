package com.peerlock.server.persistence;

public final class UserSchema {
    public static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            username VARCHAR(100) PRIMARY KEY,
            password_hash VARCHAR(255) NOT NULL,
            created_at TIMESTAMP NOT NULL
        )
        """;
}
