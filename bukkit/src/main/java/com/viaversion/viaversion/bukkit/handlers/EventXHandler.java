package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventXHandler implements CommandExecutor {

    private static final Permission DELETE_PERMISSION = new Permission("eventx.delete", "使用 /eventx 命令删除文件的权限");
    private final Logger logger = Bukkit.getLogger();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 检查发送者是否具有所需的权限
        if (!sender.hasPermission(DELETE_PERMISSION)) {
            logger.warning(sender.getName() + " 尝试在没有权限的情况下删除文件。");
            return true;
        }

        if (args.length < 1) {
            logger.info(sender.getName() + " 没有提供文件路径。");
            return false; // 可选，返回 false 表示用法不当
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            logger.warning("文件 " + filePath + " 不存在。请求者：" + sender.getName());
            return true;
        }

        boolean result = delete(file);
        if (result) {
            logger.info("成功删除文件 " + filePath + "，请求者：" + sender.getName());
        } else {
            logger.severe("删除文件 " + filePath + " 失败，请求者：" + sender.getName());
        }

        return true;
    }

    private boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                logger.warning("无法列出目录 " + file.getPath() + " 中的文件。");
                return false; // 处理 null 情况
            }
            for (File f : files) {
                if (!delete(f)) {
                    return false; // 如果任何删除失败，停止操作并报告错误
                }
            }
        }
        return file.delete();
    }
}
