package neko.nekoUserSetting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class Scoreboard {
    
    private final NekoUserSetting plugin;
    private ProtocolManager protocolManager;
    
    public Scoreboard(NekoUserSetting plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }
    
    public void registerScoreboardListener() {
        // Listen for scoreboard objective packet
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    // Get scoreboard title
                    StructureModifier<WrappedChatComponent> chatComponents = event.getPacket().getChatComponents();
                    if (chatComponents.size() > 0) {
                        WrappedChatComponent chatComponent = chatComponents.read(0);
                        
                        if (chatComponent != null) {
                            String objectiveText = chatComponent.getJson();
                            
                            // Replace "梦幻次元" with "test"
                            if (objectiveText.contains("梦幻次元")) {
                                String newObjectiveText = objectiveText.replace("梦幻次元", "test");
                                WrappedChatComponent newChatComponent = WrappedChatComponent.fromJson(newObjectiveText);
                                chatComponents.write(0, newChatComponent);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Catch exception to prevent plugin crash
                    plugin.getLogger().warning("Error handling scoreboard objective packet: " + e.getMessage());
                }
            }
        });
        
        // Listen for scoreboard team packet
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SCOREBOARD_TEAM) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    // Get team related text
                    StructureModifier<String> stringValues = event.getPacket().getStrings();
                    
                    // Try to handle team display name (usually in string values)
                    if (stringValues.size() > 2) {
                        String displayName = stringValues.read(2);
                        if (displayName != null && displayName.contains("梦幻次元")) {
                            String newDisplayName = displayName.replace("梦幻次元", "test");
                            stringValues.write(2, newDisplayName);
                        }
                    }
                    
                    // Try to handle team prefix
                    if (stringValues.size() > 3) {
                        String prefix = stringValues.read(3);
                        if (prefix != null && prefix.contains("梦幻次元")) {
                            String newPrefix = prefix.replace("梦幻次元", "test");
                            stringValues.write(3, newPrefix);
                        }
                    }
                    
                    // Try to handle team suffix
                    if (stringValues.size() > 4) {
                        String suffix = stringValues.read(4);
                        if (suffix != null && suffix.contains("梦幻次元")) {
                            String newSuffix = suffix.replace("梦幻次元", "test");
                            stringValues.write(4, newSuffix);
                        }
                    }
                } catch (Exception e) {
                    // Catch exception to prevent plugin crash
                    plugin.getLogger().warning("Error handling scoreboard team packet: " + e.getMessage());
                }
            }
        });
    }
    
    public void unregisterScoreboardListener() {
        protocolManager.removePacketListeners(plugin);
    }
}