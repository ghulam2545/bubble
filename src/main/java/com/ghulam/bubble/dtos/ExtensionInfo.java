package com.ghulam.bubble.dtos;

import lombok.Builder;

@Builder
public record ExtensionInfo(
        String name,
        String version,
        String defaultVersion,
        String schema,
        boolean relocatable,
        String comment,
        boolean installed) {
}