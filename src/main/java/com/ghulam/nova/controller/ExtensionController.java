package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.ExtensionInfo;
import com.ghulam.nova.service.ExtensionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1/extensions")
public class ExtensionController {

    private final ExtensionService extensionService;

    public ExtensionController(ExtensionService extensionService) {
        this.extensionService = extensionService;
    }

    @GetMapping
    public List<ExtensionInfo> getInstalledExtensions() {
        return extensionService.getInstalledExtensions();
    }

    @GetMapping("/available")
    public List<ExtensionInfo> getAvailableExtensions() {
        return extensionService.getAvailableExtensions();
    }

    @GetMapping("/{name}")
    public ResponseEntity<ExtensionInfo> getExtension(@PathVariable String name) {
        return extensionService.getExtensionByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}