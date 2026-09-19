package com.ghulam.bubble.helper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AppSetting {
    public static final String LOG_SEPARATOR = "──────────────────────────────────────────────────────: ";
    public static final String DOCS_URL = "http://localhost:8080/swagger-ui/index.html";

    /**
     * {@code Note:} The method name intentionally uses an unconventional naming style
     * to make this utility method easily identifiable as a logger.
     */
    public static void LOGGER(String message) {
        log.info(LOG_SEPARATOR + "{}", message);
    }
}