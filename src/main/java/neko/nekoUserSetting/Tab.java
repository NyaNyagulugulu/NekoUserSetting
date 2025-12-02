package neko.nekoUserSetting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class Tab {
    
    private final NekoUserSetting plugin;
    private ProtocolManager protocolManager;
    
    public Tab(NekoUserSetting plugin) {
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
                    
                    boolean headerModified = false;
                    
                    if (headerComponent != null) {
                        String headerText = headerComponent.getJson();
                        
                        // Replace "梦幻次元" with "test" - need to handle complex JSON with formatting
                        if (headerText.contains("梦幻次元")) {
                            String newHeaderText = headerText.replace("梦幻次元", "test");
                            
                            try {
                                WrappedChatComponent newHeaderComponent = WrappedChatComponent.fromJson(newHeaderText);
                                if (newHeaderComponent != null) {
                                    chatComponents.write(0, newHeaderComponent);
                                    headerModified = true;
                                }
                            } catch (Exception parseException) {
                                plugin.getLogger().warning("Failed to parse modified header JSON: " + parseException.getMessage());
                            }
                        }
                    }
                    
                    // If we modified the header, cancel the original packet and send the modified one
                    if (headerModified) {
                        event.setCancelled(true);
                        protocolManager.sendServerPacket(event.getPlayer(), event.getPacket());
                    }
                    
                } catch (Exception e) {
                    // Catch exception to prevent plugin crash
                    plugin.getLogger().warning("Error handling Tab list packet: " + e.getMessage());
                }
            }
        });
    }
    
    public void unregisterTabListener() {
        protocolManager.removePacketListeners(plugin);
    }
}