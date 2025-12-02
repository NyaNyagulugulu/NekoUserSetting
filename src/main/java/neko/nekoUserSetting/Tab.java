package neko.nekoUserSetting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class TabHandler {
    
    private final NekoUserSetting plugin;
    private ProtocolManager protocolManager;
    
    public TabHandler(NekoUserSetting plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }
    
    public void registerTabListener() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    // Get header and footer WrappedChatComponent
                    StructureModifier<WrappedChatComponent> chatComponents = event.getPacket().getChatComponents();
                    WrappedChatComponent headerComponent = chatComponents.read(0);
                    WrappedChatComponent footerComponent = chatComponents.read(1);
                    
                    boolean headerModified = false;
                    boolean footerModified = false;
                    
                    if (headerComponent != null) {
                        String headerText = headerComponent.getJson();
                        plugin.getLogger().info("[DEBUG-TAB] PLAYER_LIST_HEADER: " + headerText);
                        
                        // Replace "梦幻次元" with "test" - need to handle complex JSON with formatting
                        if (headerText.contains("梦幻次元")) {
                            // More sophisticated replacement for complex JSON components
                            String newHeaderText = headerText;
                            // Replace 梦幻次元 with test, considering it might be in a formatted text component
                            if (headerText.contains("\"text\":\"梦幻次元\"")) {
                                newHeaderText = headerText.replace("\"text\":\"梦幻次元\"", "\"text\":\"test\"");
                            } else if (headerText.contains("梦幻次元 ✦")) {
                                // Handle the specific case where it's "梦幻次元 ✦"
                                newHeaderText = headerText.replace("梦幻次元 ✦", "test ✦");
                            } else {
                                // General replacement
                                newHeaderText = headerText.replace("梦幻次元", "test");
                            }
                            
                            plugin.getLogger().info("[DEBUG-TAB] Replacing 梦幻次元 with test in PLAYER_LIST_HEADER");
                            plugin.getLogger().info("[DEBUG-TAB] Original: " + headerText);
                            plugin.getLogger().info("[DEBUG-TAB] New: " + newHeaderText);
                            
                            try {
                                WrappedChatComponent newHeaderComponent = WrappedChatComponent.fromJson(newHeaderText);
                                if (newHeaderComponent != null) {
                                    chatComponents.write(0, newHeaderComponent);
                                    headerModified = true;
                                    plugin.getLogger().info("[DEBUG-TAB] Header successfully modified and written back");
                                } else {
                                    plugin.getLogger().warning("[DEBUG-TAB] Failed to create new header component from JSON");
                                }
                            } catch (Exception parseException) {
                                plugin.getLogger().warning("Failed to parse modified header JSON: " + parseException.getMessage());
                            }
                        }
                    }
                    
                    if (footerComponent != null) {
                        String footerText = footerComponent.getJson();
                        plugin.getLogger().info("[DEBUG-TAB] PLAYER_LIST_FOOTER: " + footerText);
                        
                        // Replace "梦幻次元" with "test"
                        if (footerText.contains("梦幻次元")) {
                            String newFooterText = footerText.replace("梦幻次元", "test");
                            plugin.getLogger().info("[DEBUG-TAB] Replacing 梦幻次元 with test in PLAYER_LIST_FOOTER");
                            plugin.getLogger().info("[DEBUG-TAB] Original: " + footerText);
                            plugin.getLogger().info("[DEBUG-TAB] New: " + newFooterText);
                            
                            try {
                                WrappedChatComponent newFooterComponent = WrappedChatComponent.fromJson(newFooterText);
                                if (newFooterComponent != null) {
                                    chatComponents.write(1, newFooterComponent);
                                    footerModified = true;
                                    plugin.getLogger().info("[DEBUG-TAB] Footer successfully modified and written back");
                                } else {
                                    plugin.getLogger().warning("[DEBUG-TAB] Failed to create new footer component from JSON");
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