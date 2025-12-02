package neko.nekoUserSetting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Tab {
    
    private final NekoUserSetting plugin;
    private ProtocolManager protocolManager;
    private String currentLanguage;
    private Map<String, String> targetTexts;
    private Map<String, String> replacementTexts;
    private File languagesFolder;
    
    public Tab(NekoUserSetting plugin) {
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
        plugin.getLogger().info("Tab language switched to: " + language);
    }
    
    private void loadLanguageFile(String languageCode) {
        File langFile = new File(languagesFolder, languageCode + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().info("Language file for " + languageCode + " not found, using defaults.");
            // 尝试从主配置加载默认值
            FileConfiguration config = plugin.getConfig();
            String target = config.getString("tab.target", "");
            String replacement = config.getString("tab.replacement", "");
            if (!target.isEmpty() && !replacement.isEmpty()) {
                targetTexts.put(languageCode, target);
                replacementTexts.put(languageCode, replacement);
            }
            return;
        }
        
        try {
            FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
            String target = langConfig.getString("tab.target", "");
            String replacement = langConfig.getString("tab.replacement", "");
            if (!target.isEmpty() && !replacement.isEmpty()) {
                targetTexts.put(languageCode, target);
                replacementTexts.put(languageCode, replacement);
            } else {
                plugin.getLogger().info("Language file for " + languageCode + " has no valid settings, using defaults.");
                // 尝试从主配置加载默认值
                FileConfiguration config = plugin.getConfig();
                target = config.getString("tab.target", "");
                replacement = config.getString("tab.replacement", "");
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
    
    public void registerTabListener() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER) {
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
                    
                    // Get header and footer WrappedChatComponent
                    StructureModifier<WrappedChatComponent> chatComponents = event.getPacket().getChatComponents();
                    WrappedChatComponent headerComponent = chatComponents.read(0);
                    
                    boolean headerModified = false;
                    
                    if (headerComponent != null) {
                        String headerText = headerComponent.getJson();
                        
                        // Replace target text with replacement text based on current language
                        if (headerText.contains(targetText)) {
                            String newHeaderText = headerText.replace(targetText, replacementText);
                            
                            try {
                                WrappedChatComponent newHeaderComponent = WrappedChatComponent.fromJson(newHeaderText);
                                if (newHeaderComponent != null) {
                                    chatComponents.write(0, newHeaderComponent);
                                    headerModified = true;
                                    plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in tab header");
                                }
                            } catch (Exception parseException) {
                                plugin.getLogger().warning("Failed to parse modified header JSON: " + parseException.getMessage());
                            }
                        }
                    }
                    
                    // Also check footer component (index 1)
                    WrappedChatComponent footerComponent = chatComponents.read(1);
                    boolean footerModified = false;
                    
                    if (footerComponent != null) {
                        String footerText = footerComponent.getJson();
                        
                        // Replace target text with replacement text in footer based on current language
                        if (footerText.contains(targetText)) {
                            String newFooterText = footerText.replace(targetText, replacementText);
                            
                            try {
                                WrappedChatComponent newFooterComponent = WrappedChatComponent.fromJson(newFooterText);
                                if (newFooterComponent != null) {
                                    chatComponents.write(1, newFooterComponent);
                                    footerModified = true;
                                    plugin.getLogger().info("Replaced " + targetText + " with " + replacementText + " in tab footer");
                                }
                            } catch (Exception parseException) {
                                plugin.getLogger().warning("Failed to parse modified footer JSON: " + parseException.getMessage());
                            }
                        }
                    }
                    
                    // If we modified either header or footer, cancel the original packet and send the modified one
                    if (headerModified || footerModified) {
                        event.setCancelled(true);
                        protocolManager.sendServerPacket(event.getPlayer(), event.getPacket());
                    }
                    
                } catch (Exception e) {
                    // Catch exception to prevent plugin crash
                    plugin.getLogger().warning("Error handling Tab list packet: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void unregisterTabListener() {
        protocolManager.removePacketListeners(plugin);
    }
}