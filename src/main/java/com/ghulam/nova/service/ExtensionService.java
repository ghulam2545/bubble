package com.ghulam.nova.service;

import com.ghulam.nova.dtos.ExtensionInfo;
import com.ghulam.nova.repo.ExtensionRepository;
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