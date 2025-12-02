package neko.nekoUserSetting;

import org.bukkit.plugin.java.JavaPlugin;

public final class NekoUserSetting extends JavaPlugin {

    private TabHandler tabHandler;
    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Check if ProtocolLib is installed
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib is not installed, please install ProtocolLib!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("NekoUserSetting is enabled");
        
        // Initialize Tab handler
        tabHandler = new TabHandler(this);
        tabHandler.registerTabListener();
        
        // Initialize Scoreboard handler
        scoreboard = new Scoreboard(this);
        scoreboard.registerScoreboardListener();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (tabHandler != null) {
            tabHandler.unregisterTabListener();
        }
        
        if (scoreboard != null) {
            scoreboard.unregisterScoreboardListener();
        }
        
        getLogger().info("NekoUserSetting is disabled");
    }
}