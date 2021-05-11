package io.github.arleycht.SMP.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    public static YamlConfiguration getFileConfig(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }
}
