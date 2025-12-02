package neko.nekoUserSetting;

import org.bukkit.plugin.java.JavaPlugin;

public final class NekoUserSetting extends JavaPlugin {

    private TextReplaceListener textReplaceListener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Check if ProtocolLib is installed
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib not found! This plugin depends on ProtocolLib.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        textReplaceListener = new TextReplaceListener(this);
        textReplaceListener.register();
        getLogger().info("NekoUserSetting has been enabled! Text replacement is active.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (textReplaceListener != null) {
            textReplaceListener.unregister();
        }
        getLogger().info("NekoUserSetting has been disabled!");
    }
}