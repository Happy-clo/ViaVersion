# 保留主类及其主方法
-keep public class com.viaversion.* {
    public static void main(java.lang.String[]);
}

# 保留 Bukkit 和 LuckPerms API 接口，防止被混淆
-keep class org.bukkit.** { *; }
-keep class net.luckperms.api.** { *; }

# 保留事件监听器，防止被混淆
-keepclassmembers class * implements org.bukkit.event.Listener {
    public <init>(...);
    public void *(org.bukkit.event.*);
}

# 混淆所有其他类和方法名
-obfuscate
