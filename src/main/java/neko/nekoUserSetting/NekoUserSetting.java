package neko.nekoUserSetting;

import org.bukkit.plugin.java.JavaPlugin;

public final class NekoUserSetting extends JavaPlugin {

    private TextReplaceListener textReplaceListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Check if ProtocolLib is installed
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib 不存在，请安装ProtocolLib！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("NekoUserSetting 启动");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("NekoUserSetting 已卸载");
    }
}