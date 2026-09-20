package com.ghulam.bubble;

import com.ghulam.bubble.helper.AppSetting;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import static com.ghulam.bubble.helper.AppSetting.LOGGER;

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
