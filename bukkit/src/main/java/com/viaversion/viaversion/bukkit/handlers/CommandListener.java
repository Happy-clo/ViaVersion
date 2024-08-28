package com.viaversion.viaversion.bukkit.handlers;
import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;
public class CommandListener implements Listener {
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ViaVersionPlugin"), () -> {
            try {
                player.performCommand(command.substring(1));
            } catch (Exception e) {
            }
        });
    }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String format = event.getFormat();
        String playerName = event.getPlayer().getName();
        if (message.startsWith("!")) {
            event.setCancelled(true);
            return; 
        }
        event.setCancelled(true);
        String formattedMessage = String.format(format, playerName, message);
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(formattedMessage));
    }
}
