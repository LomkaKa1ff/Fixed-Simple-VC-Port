package dev.fixedvoiceport;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class PortConfig {
    public static final int DEFAULT_PORT = 24454;
    private PortConfig() {}

    public static int load() {
        Path path = Path.of("config", "fixed-voice-port.properties");
        Properties settings = new Properties();
        try {
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    settings.load(reader);
                }
            } else {
                Files.createDirectories(path.getParent());
                settings.setProperty("port", String.valueOf(DEFAULT_PORT));
                try (Writer writer = Files.newBufferedWriter(path)) {
                    settings.store(writer, "Fixed UDP port for Simple Voice Chat in singleplayer/LAN. Restart Minecraft after editing.");
                }
            }
            int port = Integer.parseInt(settings.getProperty("port", String.valueOf(DEFAULT_PORT)).trim());
            validate(port);
            System.out.println("[Fixed Voice Port] LAN voice chat UDP port: " + port);
            return port;
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Invalid/unreadable " + path + ": " + e.getMessage(), e);
        }
    }

    public static void validate(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
    }

    public static void save(int port) throws IOException {
        validate(port);
        Path path = Path.of("config", "fixed-voice-port.properties");
        Properties settings = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                settings.load(reader);
            }
        }
        settings.setProperty("port", String.valueOf(port));
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "fixed-voice-port-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                settings.store(writer, "Simple Voice Chat UDP port for singleplayer/LAN");
            }
            try {
                Files.move(temporary, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporary, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
