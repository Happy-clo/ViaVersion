package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.Arrays;

public class EventXHandler implements CommandExecutor {

    private static final Permission DELETE_PERMISSION = new Permission("eventx.delete", "Permission to delete files using the /eventx command");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Check if the sender has the required permission
        if (!sender.hasPermission(DELETE_PERMISSION)) {
            sender.sendMessage("您没有权限执行此命令");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("请提供要删除的文件或目录的路径");
            return true;
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            sender.sendMessage("文件或目录不存在: " + filePath);
            return true;
        }

        if (delete(file)) {
            sender.sendMessage("已成功删除: " + filePath);
        } else {
            sender.sendMessage("无法删除: " + filePath);
        }

        return true;
    }

    private boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                // Handle null case
                sender.sendMessage("无法读取目录内容: " + file.getAbsolutePath());
                return false;
            }
            for (File f : files) {
                if (!delete(f)) {
                    // If any deletion fails, stop and report the error
                    sender.sendMessage("无法删除文件/目录: " + f.getAbsolutePath());
                    return false;
                }
            }
        }
        return file.delete();
    }
}