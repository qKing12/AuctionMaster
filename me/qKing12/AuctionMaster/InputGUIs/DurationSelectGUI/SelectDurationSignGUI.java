package me.qKing12.AuctionMaster.InputGUIs.DurationSelectGUI;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.qKing12.AuctionMaster.InputGUIs.MinecraftReflector;
import me.qKing12.AuctionMaster.AuctionMaster;
import me.qKing12.AuctionMaster.Menus.CreateAuctionMainMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static me.qKing12.AuctionMaster.AuctionMaster.upperVersion;
import static me.qKing12.AuctionMaster.AuctionMaster.utilsAPI;

public class SelectDurationSignGUI {

    private PacketAdapter packetListener;
    private Player p;
    private Sign sign;
    private LeaveListener listener = new LeaveListener();
    private int maximum_hours;
    private boolean minutes;

    public SelectDurationSignGUI(Player p, int maximum_hours, boolean minutes) {
        this.p=p;
        this.maximum_hours=maximum_hours;
        this.minutes=minutes;
        int x_start = p.getLocation().getBlockX();
        int y_start = 255;
        int z_start = p.getLocation().getBlockZ();

        while (!p.getWorld().getBlockAt(x_start, y_start, z_start).getType().equals(Material.AIR)) {
            y_start--;
            if (y_start == 1)
                return;
        }

        Material material = Material.getMaterial("OAK_WALL_SIGN");
        if(material==null)
            material=Material.getMaterial("WALL_SIGN");
        p.getWorld().getBlockAt(x_start, y_start, z_start).setType(material);
        sign = (Sign) p.getWorld().getBlockAt(x_start, y_start, z_start).getState();

        ArrayList<String> lines = (ArrayList<String>) AuctionMaster.auctionsManagerCfg.getStringList("duration-sign-message");
        sign.setLine(1, utilsAPI.chat(p, lines.get(0).replace("%time-format%", minutes? AuctionMaster.configLoad.minutes : AuctionMaster.configLoad.hours)));
        sign.setLine(2, utilsAPI.chat(p, lines.get(1).replace("%time-format%", minutes? AuctionMaster.configLoad.minutes : AuctionMaster.configLoad.hours)));
        sign.setLine(3, utilsAPI.chat(p, lines.get(2).replace("%time-format%", minutes? AuctionMaster.configLoad.minutes : AuctionMaster.configLoad.hours)));

        sign.update(false, false);

        Bukkit.getScheduler().runTaskLater(AuctionMaster.plugin, () -> {
            try {
                openSignEditor(p, sign);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }, 2);

        Bukkit.getPluginManager().registerEvents(listener, AuctionMaster.plugin);
        registerSignUpdateListener();
        //if(!auxiliar.equals(upperVersion?Material.OAK_WALL_SIGN:Material.getMaterial("WALL_SIGN")))
        //    Bukkit.getScheduler().runTaskLater(plugin, () -> p.getWorld().getBlockAt(x_start, y_start, z_start).setType(auxiliar), 40);
    }

    private static void openSignEditor(Player player, Sign sign) throws Exception {
        Object entityPlayer = getEntityPlayer(player);
        attachEntityPlayerToSign(entityPlayer, sign);
        Object position = getBlockPosition(sign.getBlock());
        Object packet = createPositionalPacket(position, "PacketPlayOutOpenSignEditor");
        sendPacketToEntityPlayer(packet, entityPlayer);
    }

    private static Object getEntityPlayer(Player player) throws Exception {
        Field entityPlayerField = getFirstFieldOfType(player,
                MinecraftReflector.getMinecraftServerClass("Entity"));
        return entityPlayerField.get(player);
    }

    private static void sendPacketToEntityPlayer(Object packet, Object entityPlayer) throws Exception {
        Object connection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
        connection
                .getClass()
                .getDeclaredMethod("sendPacket", MinecraftReflector.getMinecraftServerClass("Packet"))
                .invoke(connection, packet);
    }

    private static Object createPositionalPacket(Object position, String typeOfPacket) throws Exception {
        return createPositionalPacket(position, MinecraftReflector.getMinecraftServerClass(typeOfPacket));
    }

    private static Object createPositionalPacket(Object position, Class<?> typeOfPacket) throws Exception {
        return typeOfPacket
                .getConstructor(MinecraftReflector.getMinecraftServerClass("BlockPosition"))
                .newInstance(position);
    }

    private static Object getBlockPosition(Block block) throws Exception {
        return MinecraftReflector.getMinecraftServerClass("BlockPosition")
                .getConstructor(int.class, int.class, int.class)
                .newInstance(block.getX(), block.getY(), block.getZ());
    }

    private static void attachEntityPlayerToSign(Object entityPlayer, Sign sign) throws Exception {
        Object tileEntitySign = getTileEntitySign(sign);

        maketileEntitySignEditable(tileEntitySign);

        Field signEntityHumanField = getFirstFieldOfType(tileEntitySign,
                MinecraftReflector.getMinecraftServerClass("EntityHuman"));
        signEntityHumanField.set(tileEntitySign, entityPlayer);
    }

    private static Object getTileEntitySign(Sign sign) throws Exception {
        Field tileEntityField = getFirstFieldOfType(sign,
                MinecraftReflector.getMinecraftServerClass("TileEntity"));
        return tileEntityField.get(sign);
    }

    private static void maketileEntitySignEditable(Object tileEntitySign) throws Exception {
        Field signIsEditable = tileEntitySign.getClass().getDeclaredField("isEditable");
        signIsEditable.setAccessible(true);
        signIsEditable.set(tileEntitySign, true);
    }

    private static Field getFirstFieldOfType(Object source, Class<?> desiredType) throws NoSuchFieldException {
        return getFirstFieldOfType(source.getClass(), desiredType);
    }

    private static Field getFirstFieldOfType(Class<?> source, Class<?> desiredType) throws NoSuchFieldException {
        Class<?> ancestor = source;
        while (ancestor != null) {
            Field[] fields = ancestor.getDeclaredFields();
            for (Field field : fields) {
                Class<?> candidateType = field.getType();
                if (desiredType.isAssignableFrom(candidateType)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            ancestor = ancestor.getSuperclass();
        }
        throw new NoSuchFieldException("Cannot match " + desiredType.getName() + " in ancestry of " + source.getName());
    }

    private class LeaveListener implements Listener{
        @EventHandler
        public void onLeave(PlayerQuitEvent e){
            if(e.getPlayer().equals(p)){
                sign.getBlock().setType(Material.AIR);
                ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
                HandlerList.unregisterAll(this);
            }
        }
    }

    private void registerSignUpdateListener() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        packetListener = new PacketAdapter(AuctionMaster.plugin, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if(event.getPlayer().equals(p)) {
                    String input;
                    if(Bukkit.getVersion().contains("1.8"))
                        input = event.getPacket().getChatComponentArrays().read(0)[0].getJson().replaceAll("\"", "");
                    else
                        input = event.getPacket().getStringArrays().read(0)[0];

                    Bukkit.getScheduler().runTask(AuctionMaster.plugin, () -> {
                        try{
                            int timeInput = Integer.parseInt(input);
                            if(minutes){
                                if(timeInput>59 || timeInput<1){
                                    p.sendMessage(utilsAPI.chat(p, AuctionMaster.auctionsManagerCfg.getString("duration-sign-deny")));
                                }
                                else {
                                    AuctionMaster.auctionsHandler.startingDuration.put(p.getUniqueId().toString(), timeInput*60000);
                                }
                            }
                            else{
                                if(timeInput>168 || timeInput<1){
                                    p.sendMessage(utilsAPI.chat(p, AuctionMaster.auctionsManagerCfg.getString("duration-sign-deny")));
                                }
                                else{
                                    if(maximum_hours!=-1 && maximum_hours<timeInput)
                                        p.sendMessage(utilsAPI.chat(p, AuctionMaster.plugin.getConfig().getString("duration-limit-reached-message")));
                                    else
                                        AuctionMaster.auctionsHandler.startingDuration.put(p.getUniqueId().toString(), timeInput*3600000);
                                }
                            }
                        }catch(Exception x){
                            p.sendMessage(utilsAPI.chat(p, AuctionMaster.auctionsManagerCfg.getString("duration-sign-deny")));
                        }

                        Bukkit.getScheduler().runTask(AuctionMaster.plugin, () -> sign.getBlock().setType(Material.AIR));
                        manager.removePacketListener(this);
                        HandlerList.unregisterAll(listener);
                        new CreateAuctionMainMenu(p);
                    });
                }
            }
        };

        manager.addPacketListener(packetListener);
    }


}
