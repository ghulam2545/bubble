package com.ghulam.nova;

import com.ghulam.nova.helper.AppSetting;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import static com.ghulam.nova.helper.AppSetting.LOGGER;

@SpringBootApplication
public class NovaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        LOGGER(String.format("Swagger docs is up at: %s", AppSetting.DOCS_URL));
    }
}
