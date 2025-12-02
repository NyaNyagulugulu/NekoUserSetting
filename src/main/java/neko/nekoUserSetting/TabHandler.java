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
                    WrappedChatComponent headerComponent = event.getPacket().getChatComponents().read(0);
                    WrappedChatComponent footerComponent = event.getPacket().getChatComponents().read(1);
                    
                    if (headerComponent != null) {
                        String headerText = headerComponent.getJson();
                        
                        // Replace "梦幻次元" with "test"
                        if (headerText.contains("梦幻次元")) {
                            String newHeaderText = headerText.replace("梦幻次元", "test");
                            WrappedChatComponent newHeaderComponent = WrappedChatComponent.fromJson(newHeaderText);
                            event.getPacket().getChatComponents().write(0, newHeaderComponent);
                        }
                    }
                    
                    if (footerComponent != null) {
                        String footerText = footerComponent.getJson();
                        
                        // Replace "梦幻次元" with "test"
                        if (footerText.contains("梦幻次元")) {
                            String newFooterText = footerText.replace("梦幻次元", "test");
                            WrappedChatComponent newFooterComponent = WrappedChatComponent.fromJson(newFooterText);
                            event.getPacket().getChatComponents().write(1, newFooterComponent);
                        }
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