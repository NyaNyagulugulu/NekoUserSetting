package neko.nekoUserSetting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ScoreboardChang implements Listener {
    
    private final NekoUserSetting plugin;
    private ProtocolManager protocolManager;
    private String currentLanguage;
    private Map<String, String> targetTexts;
    private Map<String, String> replacementTexts;
    private File languagesFolder;
    
    public ScoreboardChang(NekoUserSetting plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.currentLanguage = "default"; // 默认语言
        // 从data.yml加载配置路径来决定语言文件存放位置
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        FileConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        String configPath = dataConfig.getString("config_path", "/NekoServer/config");
        // 提取目录路径
        String directoryPath = configPath.substring(0, configPath.lastIndexOf('/') + 1);
        this.languagesFolder = new File(plugin.getDataFolder(), directoryPath.substring(1) + "languages");
        if (!languagesFolder.exists()) {
            languagesFolder.mkdirs();
        }
        initializeLanguageMaps();
    }
    
    private void initializeLanguageMaps() {
        // 初始化语言映射表
        targetTexts = new HashMap<>();
        replacementTexts = new HashMap<>();
    }
    
    public void setLanguage(String language) {
        this.currentLanguage = language.toLowerCase();
        // 尝试加载对应的语言文件
        loadLanguageFile(currentLanguage);
        plugin.getLogger().info("Language switched to: " + language);
    }
    
    private void loadLanguageFile(String languageCode) {
        File langFile = new File(languagesFolder, languageCode + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().info("Language file for " + languageCode + " not found, using defaults.");
            // 尝试从主配置加载默认值
            FileConfiguration config = plugin.getConfig();
            String target = config.getString("scoreboard.target", "");
            String replacement = config.getString("scoreboard.replacement", "");
            if (!target.isEmpty() && !replacement.isEmpty()) {
                targetTexts.put(languageCode, target);
                replacementTexts.put(languageCode, replacement);
            }
            return;
        }
        
        try {
            FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
            String target = langConfig.getString("scoreboard.target", "");
            String replacement = langConfig.getString("scoreboard.replacement", "");
            if (!target.isEmpty() && !replacement.isEmpty()) {
                targetTexts.put(languageCode, target);
                replacementTexts.put(languageCode, replacement);
            } else {
                plugin.getLogger().info("Language file for " + languageCode + " has no valid settings, using defaults.");
                // 尝试从主配置加载默认值
                FileConfiguration config = plugin.getConfig();
                target = config.getString("scoreboard.target", "");
                replacement = config.getString("scoreboard.replacement", "");
                if (!target.isEmpty() && !replacement.isEmpty()) {
                    targetTexts.put(languageCode, target);
                    replacementTexts.put(languageCode, replacement);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error loading language file " + languageCode + ": " + e.getMessage());
        }
    }
    
    private String getCurrentTargetText() {
        return targetTexts.getOrDefault(currentLanguage, "");
    }
    
    private String getCurrentReplacementText() {
        return replacementTexts.getOrDefault(currentLanguage, "");
    }
    
    public void registerScoreboardListener() {
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registerPacketListener();
        plugin.getLogger().info("Scoreboard event listener registered");
    }
    
    private void registerPacketListener() {
        // 注册协议库监听器来拦截和修改计分板数据包
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, 
            PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
            PacketType.Play.Server.SCOREBOARD_SCORE,
            PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE,
            PacketType.Play.Server.SCOREBOARD_TEAM) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    // 获取当前语言设置
                    String targetText = getCurrentTargetText();
                    String replacementText = getCurrentReplacementText();
                    
                    // 只有在目标文本不为空时才进行替换
                    if (targetText.isEmpty() || replacementText.isEmpty()) {
                        return;
                    }
                    
                    // Debug: 打印数据包类型和内容
                    plugin.getLogger().info("Scoreboard packet detected: " + event.getPacketType().name());
                    
                    // 处理不同类型的计分板数据包
                    if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
                        StructureModifier<String> stringModifiers = event.getPacket().getStrings();
                        StructureModifier<WrappedChatComponent> chatComponents = event.getPacket().getChatComponents();
                        StructureModifier<Integer> intModifiers = event.getPacket().getIntegers();
                        
                        String mode = "UNKNOWN";
                        if (intModifiers.size() > 0) {
                            int modeInt = intModifiers.read(0);
                            switch (modeInt) {
                                case 0: mode = "CREATE"; break;
                                case 1: mode = "REMOVE"; break;
                                case 2: mode = "UPDATE"; break;
                            }
                        }
                        
                        String objectiveName = stringModifiers.size() > 0 ? stringModifiers.read(0) : "UNKNOWN";
                        
                        // 记录目标信息
                        plugin.getLogger().info("Scoreboard objective packet [" + mode + "] - Name: " + objectiveName);
                        
                        // 检查聊天组件中的显示名称
                        if (chatComponents.size() > 0) {
                            WrappedChatComponent component = chatComponents.read(0);
                            if (component != null) {
                                String json = component.getJson();
                                plugin.getLogger().info("Scoreboard objective content JSON: " + json);
                                
                                // 检查目标文本并替换为当前语言的文本
                                if (json.contains(targetText)) {
                                    String newJson = json.replace(targetText, replacementText);
                                    WrappedChatComponent newComponent = WrappedChatComponent.fromJson(newJson);
                                    chatComponents.write(0, newComponent);
                                    plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in objective JSON content");
                                }
                            }
                        }
                        
                        // 检查目标名称中的目标文本
                        if (objectiveName.contains(targetText)) {
                            String newObjectiveName = objectiveName.replace(targetText, replacementText);
                            stringModifiers.write(0, newObjectiveName);
                            plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in objective name: " + newObjectiveName);
                        }
                        
                    } else if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_SCORE) {
                        StructureModifier<String> stringModifiers = event.getPacket().getStrings();
                        StructureModifier<Integer> intModifiers = event.getPacket().getIntegers();
                        
                        String scoreName = stringModifiers.size() > 0 ? stringModifiers.read(0) : "UNKNOWN";
                        String objectiveName = stringModifiers.size() > 1 ? stringModifiers.read(1) : "UNKNOWN";
                        int scoreValue = intModifiers.size() > 0 ? intModifiers.read(0) : 0;
                        
                        plugin.getLogger().info("Scoreboard score packet - Score: " + scoreName + " | Objective: " + objectiveName + " | Value: " + scoreValue);
                        
                        // 检查分数名称中的目标文本
                        if (scoreName.contains(targetText)) {
                            String newScoreName = scoreName.replace(targetText, replacementText);
                            stringModifiers.write(0, newScoreName);
                            plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in score name: " + newScoreName);
                        }
                        
                    } else if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE) {
                        StructureModifier<String> stringModifiers = event.getPacket().getStrings();
                        StructureModifier<Integer> intModifiers = event.getPacket().getIntegers();
                        
                        int position = intModifiers.size() > 0 ? intModifiers.read(0) : -1;
                        String objectiveName = stringModifiers.size() > 0 ? stringModifiers.read(0) : "UNKNOWN";
                        
                        plugin.getLogger().info("Scoreboard display packet - Position: " + position + " | Objective: " + objectiveName);
                        
                        // 检查显示目标名称中的目标文本
                        if (objectiveName.contains(targetText)) {
                            String newObjectiveName = objectiveName.replace(targetText, replacementText);
                            stringModifiers.write(0, newObjectiveName);
                            plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in display objective: " + newObjectiveName);
                        }
                        
                    } else if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_TEAM) {
                        StructureModifier<String> stringModifiers = event.getPacket().getStrings();
                        
                        // 在1.12.2中，团队数据包有多个字符串字段
                        // 通常: 0=名称, 1=友好名称, 2=前缀, 3=后缀, 4=玩家
                        String teamName = stringModifiers.size() > 0 ? stringModifiers.read(0) : null;
                        String friendlyName = stringModifiers.size() > 1 ? stringModifiers.read(1) : null;
                        String prefix = stringModifiers.size() > 2 ? stringModifiers.read(2) : null;
                        String suffix = stringModifiers.size() > 3 ? stringModifiers.read(3) : null;
                        
                        plugin.getLogger().info("Scoreboard team packet - Team: " + teamName + " | Friendly: " + friendlyName + 
                                              " | Prefix: " + prefix + " | Suffix: " + suffix);
                        
                        // 检查团队字段中的目标文本并替换
                        if (teamName != null && teamName.contains(targetText)) {
                            stringModifiers.write(0, teamName.replace(targetText, replacementText));
                            plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in team name");
                        }
                        
                        if (friendlyName != null && friendlyName.contains(targetText)) {
                            stringModifiers.write(1, friendlyName.replace(targetText, replacementText));
                            plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in team friendly name");
                        }
                        
                        if (prefix != null && prefix.contains(targetText)) {
                            stringModifiers.write(2, prefix.replace(targetText, replacementText));
                            plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in team prefix");
                        }
                        
                        if (suffix != null && suffix.contains(targetText)) {
                            stringModifiers.write(3, suffix.replace(targetText, replacementText));
                            plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in team suffix");
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error handling scoreboard packet: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // 玩家加入时，确保他们能看到修改后的计分板
    }
    
    public void unregisterScoreboardListener() {
        protocolManager.removePacketListeners(plugin);
        plugin.getLogger().info("Scoreboard event listener unregistered");
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
    }
}
