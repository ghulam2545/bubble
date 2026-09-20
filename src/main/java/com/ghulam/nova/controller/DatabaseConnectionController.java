package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.DatabaseConfig;
import com.ghulam.nova.service.ConnectionService;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DatabaseConnectionController {

    private final DatabaseClient db;
    private final ConnectionService conn;

    public DatabaseConnectionController(DatabaseClient db, ConnectionService conn) {
        this.db = db;
        this.conn = conn;
    }

    @PostMapping("/connect")
    public String connect(@RequestBody DatabaseConfig config) {

        conn.connect(config);

        List<Integer> result = db.queryForList(
                "SELECT 1",
                new MapSqlParameterSource(),
                Integer.class
        );

        return "Connected: " + result;
    }

}
