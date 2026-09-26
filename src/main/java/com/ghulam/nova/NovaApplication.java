package com.ghulam.nova;

import com.ghulam.nova.dtos.DatabaseConfig;
import com.ghulam.nova.helper.AppSetting;
import com.ghulam.nova.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import static com.ghulam.nova.helper.AppSetting.LOGGER;

@SpringBootApplication
@RequiredArgsConstructor
public class NovaApplication {

    private final ConnectionService connectionService;

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    public static void main(String[] args) {
        SpringApplication.run(NovaApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        LOGGER(String.format("Swagger docs is up at: %s", AppSetting.DOCS_URL));
        connect();
    }

    // Bootstrap a starter DB from application.properties info
    public void connect() {
        try {
            String[] parts = dbUrl.replace("jdbc:postgresql://", "").split("[/:]");

            DatabaseConfig config = new DatabaseConfig(
                    parts[0],
                    Integer.parseInt(parts[1]),
                    parts[2],
                    username,
                    password
            );
            connectionService.connect(config);

            LOGGER("Starter database is up.");
        } catch (Exception e) {
            LOGGER(String.format("Could not connect to local database: %s", e.getMessage()));
        }
    }
}
