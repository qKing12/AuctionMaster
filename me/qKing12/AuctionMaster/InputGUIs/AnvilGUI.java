package me.qKing12.AuctionMaster.InputGUIs;

import me.qKing12.AuctionMaster.AuctionMaster;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * An anvil gui, used for gathering a user's input
 * @author Wesley Smith
 * @since 1.0
 */
public class AnvilGUI {

    /**
     * The player who has the GUI open
     */
    private final Player holder;
    /**
     * The ItemStack that is in the {@link Slot#INPUT_LEFT} slot.
     */
    private final ItemStack insert;
    /**
     * Called when the player clicks the {@link Slot#OUTPUT} slot
     */
    private final ClickHandler clickHandler;
    
    /**
     * The container id of the inventory, used for Main.anvilHelper methods
     */
    private final int containerId;
    /**
     * The inventory that is used on the Bukkit side of things
     */
    private final Inventory inventory;
    /**
     * The listener holder class
     */
    private final ListenUp listener = new ListenUp();

    /**
     * Represents the state of the inventory being open
     */
    private boolean open;

    public AnvilGUI(Player holder, ItemStack slot, ClickHandler clickHandler) {
        this.holder = holder;
        this.clickHandler = clickHandler;

        this.insert = slot;
        

        AuctionMaster.anvilHelper.handleInventoryCloseEvent(holder);
        AuctionMaster.anvilHelper.setActiveContainerDefault(holder);

        Bukkit.getPluginManager().registerEvents(listener, AuctionMaster.plugin);

        final Object container = AuctionMaster.anvilHelper.newContainerAnvil(holder, "");
        containerId = AuctionMaster.anvilHelper.getNextContainerId(holder, container);

        inventory = AuctionMaster.anvilHelper.toBukkitInventory(container);
        inventory.setItem(Slot.INPUT_LEFT, this.insert);

        AuctionMaster.anvilHelper.sendPacketOpenWindow(holder, containerId, "");
        AuctionMaster.anvilHelper.setActiveContainer(holder, container);
        AuctionMaster.anvilHelper.setActiveContainerId(container, containerId);
        AuctionMaster.anvilHelper.addActiveContainerSlotListener(container, holder);

        open = true;
    }


    /**
     * Simply holds the listeners for the GUI
     */
    private class ListenUp implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if(e.getInventory().equals(inventory)) {
                e.setCancelled(true);
                final Player clicker = (Player) e.getWhoClicked();
                if(e.getRawSlot() == Slot.OUTPUT) {
                    final ItemStack clicked = inventory.getItem(e.getRawSlot());
                    if(clicked == null || clicked.getType() == Material.AIR) return;
                    String reply = clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString();
                    if(reply!=null && reply.startsWith(" "))
                        reply=reply.substring(1);
                    final String ret = clickHandler.onClick(reply);
                    if(ret != null) {
                        final ItemMeta meta = clicked.getItemMeta();
                        meta.setDisplayName(ret);
                        clicked.setItemMeta(meta);
                        inventory.setItem(Slot.OUTPUT, clicked);
                        clicker.updateInventory();
                    }
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if(e.getInventory().equals(inventory)) {
                Player p = (Player)e.getPlayer();
                e.getInventory().clear();
                AuctionMaster.anvilHelper.setActiveContainerDefault(holder);
                AuctionMaster.anvilHelper.sendPacketCloseWindow(holder, containerId);

                HandlerList.unregisterAll(listener);
            }
        }
    }

    /**
     * Handles the click of the output slot
     */
    public interface ClickHandler{

        /**
         * Is called when a {@link Player} clicks on the output in the GUI
         * @param input What the item was renamed to
         * @return What to replace the text with, or null to close the inventory
         */
        String onClick(String input);
    }

    /**
     * Class wrapping the magic constants of slot numbers in an anvil GUI
     */
    public static class Slot {

        /**
         * The slot on the far left, where the first input is inserted. An {@link ItemStack} is always inserted
         * here to be renamed
         */
        public static final int INPUT_LEFT = 0;
        /**
         * Not used, but in a real anvil you are able to put the second item you want to combine here
         */
        public static final int OUTPUT = 2;

    }

}
