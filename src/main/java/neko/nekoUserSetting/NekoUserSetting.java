package neko.nekoUserSetting;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class NekoUserSetting extends JavaPlugin {

    private Tab tabHandler;
    private ScoreboardChang scoreboardChang;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Check if ProtocolLib is installed
        if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib not installed, please install ProtocolLib!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 加载配置文件路径
        loadConfigPath();
        saveDefaultConfig(); // 确保配置被加载到this.getConfig()
        getLogger().info("NekoUserSetting is enabled");
        
        // 从配置中获取默认语言并设置
        String defaultLanguage = getConfig().getString("Default", "en-us");
        getLogger().info("Using default language: " + defaultLanguage);
        
        // Initialize Tab handler
        tabHandler = new Tab(this);
        tabHandler.setLanguage(defaultLanguage);  // 设置默认语言
        tabHandler.registerTabListener();
        
        // Initialize Scoreboard handler
        scoreboardChang = new ScoreboardChang(this);
        scoreboardChang.setLanguage(defaultLanguage);  // 设置默认语言
        scoreboardChang.registerScoreboardListener();
    }

    private void loadConfigPath() {
        // 从data.yml加载配置路径
        File dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            // 如果data.yml不存在，创建默认文件
            saveResource("data.yml", false);
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        String configPath = dataConfig.getString("config_path", "data/config.yml");
        
        // 从指定路径加载具体配置
        File configFile = new File(getDataFolder(), configPath);
        if (!configFile.exists()) {
            // 如果配置文件不存在，创建目录并使用默认配置
            configFile.getParentFile().mkdirs();
            // 从resources复制默认配置文件
            saveResource("config.yml", false);
            // 如果默认配置没找到，创建默认配置内容
            if (!configFile.exists()) {
                createDefaultConfig(configFile);
            }
        }
        // 将配置加载到插件
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        this.getConfig().setDefaults(config);
    }

    private void createDefaultConfig(File configFile) {
        try {
            File parentDir = configFile.getParentFile();
            if (parentDir != null) {
                parentDir.mkdirs();
            }
            configFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            config.set("Default", "en-us");
            config.set("language", java.util.Arrays.asList("zh-cn", "zh-tw", "en-us"));
            config.save(configFile);
        } catch (IOException e) {
            getLogger().severe("无法创建默认配置文件: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("lang")) {
            if (args.length == 1) {
                String language = args[0].toLowerCase();
                // 更新Scoreboard和Tab的语言设置
                scoreboardChang.setLanguage(language);
                tabHandler.setLanguage(language);
                sender.sendMessage("Language set to: " + language);
                return true;
            } else {
                sender.sendMessage("Usage: /lang <language>");
                return false;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (tabHandler != null) {
            tabHandler.unregisterTabListener();
        }
        
        if (scoreboardChang != null) {
            scoreboardChang.unregisterScoreboardListener();
        }
        
        getLogger().info("NekoUserSetting is disabled");
    }
}