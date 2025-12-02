package neko.nekoUserSetting;

import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public final class NekoUserSetting extends JavaPlugin {

    private ProtocolManager protocolManager;

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
        
        // 初始化ProtocolLib
        initProtocolLib();
    }

    private void initProtocolLib() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        // 注册Tab列表数据包监听器
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // 获取header和footer的WrappedChatComponent
                WrappedChatComponent headerComponent = event.getPacket().getChatComponents().read(0);
                WrappedChatComponent footerComponent = event.getPacket().getChatComponents().read(1);
                
                if (headerComponent != null) {
                    String headerText = headerComponent.getJson();
                    
                    // 直接替换"梦幻次元"为"test"
                    if (headerText.contains("梦幻次元")) {
                        String newHeaderText = headerText.replace("梦幻次元", "test");
                        WrappedChatComponent newHeaderComponent = WrappedChatComponent.fromJson(newHeaderText);
                        event.getPacket().getChatComponents().write(0, newHeaderComponent);
                    }
                }
                
                if (footerComponent != null) {
                    String footerText = footerComponent.getJson();
                    
                    // 直接替换"梦幻次元"为"test"
                    if (footerText.contains("梦幻次元")) {
                        String newFooterText = footerText.replace("梦幻次元", "test");
                        WrappedChatComponent newFooterComponent = WrappedChatComponent.fromJson(newFooterText);
                        event.getPacket().getChatComponents().write(1, newFooterComponent);
                    }
                }
            }
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (protocolManager != null) {
            protocolManager.removePacketListeners(this);
        }
        getLogger().info("NekoUserSetting 已卸载");
    }
}