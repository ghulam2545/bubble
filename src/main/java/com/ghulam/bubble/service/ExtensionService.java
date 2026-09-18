package com.ghulam.bubble.service;

import com.ghulam.bubble.dtos.ExtensionInfo;
import com.ghulam.bubble.repo.ExtensionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExtensionService {

    private final ExtensionRepository extensionRepository;

    public ExtensionService(ExtensionRepository extensionRepository) {
        this.extensionRepository = extensionRepository;
    }

    public List<ExtensionInfo> getInstalledExtensions() {
        return extensionRepository.findInstalled();
    }

    public List<ExtensionInfo> getAvailableExtensions() {
        return extensionRepository.findAvailable();
    }

    public Optional<ExtensionInfo> getExtensionByName(String name) {
        return extensionRepository.findByName(name);
    }
}