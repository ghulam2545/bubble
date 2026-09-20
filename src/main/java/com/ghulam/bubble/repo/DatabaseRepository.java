package com.ghulam.bubble.repo;

import com.ghulam.bubble.dtos.DatabaseInfo;
import com.ghulam.bubble.service.DatabaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collections;

@Repository
@Slf4j
public class DatabaseRepository {

    private final DatabaseClient client;

    public DatabaseRepository(DatabaseClient client) {
        this.client = client;
    }

    public DatabaseInfo getDatabaseInfo() {
        String sql = """
                SELECT current_database() as db_name, 
                       current_user as curr_user, 
                       current_schema() as curr_schema, 
                       version() as version, 
                       pg_postmaster_start_time()::text as start_time, 
                       (now() - pg_postmaster_start_time())::text as uptime, 
                       (SELECT count(*) FROM pg_stat_activity)::int as active_connections, 
                       (SELECT setting::int FROM pg_settings WHERE name='max_connections') as max_connections, 
                       (SELECT setting FROM pg_settings WHERE name='TimeZone') as timezone
                """;

        return client.queryForObject(sql, Collections.emptyMap(), (rs, rowNum) -> DatabaseInfo.builder()
                .databaseName(rs.getString("db_name"))
                .currentUser(rs.getString("curr_user"))
                .currentSchema(rs.getString("curr_schema"))
                .postgresVersion(rs.getString("version"))
                .serverStartTime(rs.getString("start_time"))
                .uptime(rs.getString("uptime"))
                .activeConnections(rs.getInt("active_connections"))
                .maxConnections(rs.getInt("max_connections"))
                .timezone(rs.getString("timezone"))
                .serverVersionNum(getServerVersionNum())
                .build());
    }

    public String getPostgresVersion() {
        return client.queryForObject("SHOW server_version", Collections.emptyMap(), String.class);
    }

    public int getServerVersionNum() {
        String val = client.queryForObject("SHOW server_version_num", Collections.emptyMap(), String.class);
        return val != null ? Integer.parseInt(val) : 0;
    }

    public boolean isConnected() {
        try {
            Integer result = client.queryForObject("SELECT 1", Collections.emptyMap(), Integer.class);
            return result != null && result == 1;
        } catch (Exception e) {
            log.error("Database connection check failed", e);
            return false;
        }
    }
}