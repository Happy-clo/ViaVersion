package com.viaversion.viaversion.bukkit.handlers;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LPIsInstalled {

    private final JavaPlugin plugin;
    private LuckPerms luckPerms;

    public LPIsInstalled(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents((Listener) plugin, plugin);

        // 检查LuckPerms插件是否存在
        if (isLuckPermsInstalled()) {
            luckPerms = LuckPermsProvider.get();
        }
    }

    private boolean isLuckPermsInstalled() {
        try {
            RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            return provider != null;
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}
