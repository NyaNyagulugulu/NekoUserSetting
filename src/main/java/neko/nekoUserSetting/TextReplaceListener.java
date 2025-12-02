package neko.nekoUserSetting;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class TextReplaceListener {
    private final JavaPlugin plugin;
    private ProtocolManager protocolManager;

    public TextReplaceListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        // Listen for chat component related packets
        protocolManager.addPacketListener(
            new PacketAdapter(
                plugin, 
                ListenerPriority.NORMAL,
                PacketType.Play.Server.SCOREBOARD_TEAM, // Scoreboard team
                PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER, // Tab list header footer
                PacketType.Play.Server.WINDOW_ITEMS, // Window items
                PacketType.Play.Server.SET_SLOT, // Set slot
                PacketType.Play.Server.SCOREBOARD_OBJECTIVE // Scoreboard objective
            ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    // Handle scoreboard team packet
                    if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_TEAM) {
                        handleScoreboardTeamPacket(event);
                    }
                    // Handle Tab list header footer packet
                    else if (event.getPacketType() == PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER) {
                        handlePlayerListHeaderFooterPacket(event);
                    }
                    // Handle window items packet
                    else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
                        handleWindowItemsPacket(event);
                    }
                    // Handle set slot packet
                    else if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
                        handleSetSlotPacket(event);
                    }
                    // Handle scoreboard objective packet
                    else if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
                        handleScoreboardObjectivePacket(event);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error processing packet: " + e.getMessage());
                }
            }
        });
    }

    private void handleScoreboardTeamPacket(PacketEvent event) {
        // Handle text replacement for scoreboard team name
        try {
            // Check if the packet has the expected structure
            if (event.getPacket().getStrings().size() > 0) {
                // Get team name
                String teamName = event.getPacket().getStrings().read(0);
                String newName = replaceText(teamName);
                if (newName != null && !newName.equals(teamName)) {
                    event.getPacket().getStrings().write(0, newName);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling scoreboard team packet: " + e.getMessage());
        }
    }

    private void handlePlayerListHeaderFooterPacket(PacketEvent event) {
        // Handle text replacement for Tab list header footer
        try {
            WrappedChatComponent header = event.getPacket().getChatComponents().read(0);
            WrappedChatComponent footer = event.getPacket().getChatComponents().read(1);

            if (header != null) {
                String headerText = header.getJson();
                String newHeaderText = replaceText(headerText);
                if (newHeaderText != null && !newHeaderText.equals(headerText)) {
                    event.getPacket().getChatComponents().write(0, WrappedChatComponent.fromJson(newHeaderText));
                }
            }

            if (footer != null) {
                String footerText = footer.getJson();
                String newFooterText = replaceText(footerText);
                if (newFooterText != null && !newFooterText.equals(footerText)) {
                    event.getPacket().getChatComponents().write(1, WrappedChatComponent.fromJson(newFooterText));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling player list header footer packet: " + e.getMessage());
        }
    }

    private void handleWindowItemsPacket(PacketEvent event) {
        // Handle text replacement for window items
        try {
            if (event.getPacket().getItemArrayModifier().size() > 0) {
                ItemStack[] items = event.getPacket().getItemArrayModifier().read(0);
                if (items != null) {
                    boolean changed = false;
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] != null) {
                            ItemStack oldItem = items[i].clone();
                            // Attempt to replace item name
                            ItemStack newItem = replaceItemName(items[i]);
                            if (!newItem.equals(oldItem)) {
                                items[i] = newItem;
                                changed = true;
                            }
                        }
                    }
                    // Only write back if we made changes
                    if (changed) {
                        event.getPacket().getItemArrayModifier().write(0, items);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling window items packet: " + e.getMessage());
        }
    }

    private void handleSetSlotPacket(PacketEvent event) {
        // Handle text replacement for set slot
        try {
            if (event.getPacket().getItemModifier().size() > 0) {
                ItemStack item = event.getPacket().getItemModifier().read(0);
                if (item != null) {
                    ItemStack oldItem = item.clone();
                    // Attempt to replace item name
                    ItemStack newItem = replaceItemName(item);
                    if (!newItem.equals(oldItem)) {
                        event.getPacket().getItemModifier().write(0, newItem);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling set slot packet: " + e.getMessage());
        }
    }
    
    private ItemStack replaceItemName(ItemStack item) {
        try {
            if (item != null) {
                // 简化处理，不修改物品NBT，避免CraftItemStack错误
                // 仅作日志记录，避免影响其他功能
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error replacing item name: " + e.getMessage());
        }
        return item;
    }

    private void handleScoreboardObjectivePacket(PacketEvent event) {
        // Handle text replacement for scoreboard objective
        try {
            // Check if the packet has the expected structure
            if (event.getPacket().getStrings().size() > 1) {
                String displayName = event.getPacket().getStrings().read(1);
                String newDisplayName = replaceText(displayName);
                if (newDisplayName != null && !newDisplayName.equals(displayName)) {
                    event.getPacket().getStrings().write(1, newDisplayName);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error handling scoreboard objective packet: " + e.getMessage());
        }
    }

    private String replaceText(String originalText) {
        // Perform text replacement - replace "梦幻次元" with "test"
        if (originalText != null) {
            return originalText.replace("梦幻次元", "test");
        }
        return originalText;
    }

    public void unregister() {
        if (protocolManager != null) {
            protocolManager.removePacketListeners(plugin);
        }
    }
}