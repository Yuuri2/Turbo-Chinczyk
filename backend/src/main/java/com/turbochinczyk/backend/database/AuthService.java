package com.turbochinczyk.backend.database;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Validates a token string by checking the database.
     * @param tokenStr The UUID string passed from the client
     * @return Optional containing the User session details if valid
     */
    public Optional<UserSessionDto> validateToken(String tokenStr) {
        try {
            // Convert string token to UUID object required by PostgreSQL UUID type
            UUID sessionUuid = UUID.fromString(tokenStr);

            String sql = """
                SELECT u.id, u.name 
                FROM tokens t
                JOIN users u ON t.userid = u.id
                WHERE t.sessionid = ?
            """;

            UserSessionDto userSession = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new UserSessionDto(rs.getInt("id"), rs.getString("name")),
                sessionUuid
            );

            return Optional.ofNullable(userSession);

        } catch (IllegalArgumentException e) {
            // Token string wasn't a valid UUID format
            return Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            // Token doesn't exist in the database
            return Optional.empty();
        }
    }
}