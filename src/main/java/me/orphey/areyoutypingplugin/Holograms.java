package me.orphey.areyoutypingplugin;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.maximde.hologramapi.HologramAPI;
import com.maximde.hologramapi.hologram.TextAnimation;
import com.maximde.hologramapi.hologram.TextHologram;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Holograms {

    public static void create(Player player) {
        Vector offset = Config.getTranslation();

        TextHologram hologram = new TextHologram(player.getUniqueId().toString())
                .setMiniMessageText(textBuilder(player) + animationFrame(player)[0])
                .setSeeThroughBlocks(Config.isVisibleThroughBlocks())
                .setBillboard(Display.Billboard.CENTER)
                .setShadow(Config.isTextShadow())
                .setViewRange(Config.getViewRange())
                .setBackgroundColor(Color.fromARGB(Config.getBackgroundTransparency(), color('R'), color('G'), color('B')).asARGB())
                .setTranslation((float)offset.getX(), (float)offset.getY(), (float)offset.getZ());

        TextAnimation animation = new TextAnimation()
                .addFrame( textBuilder(player) + animationFrame(player)[1])
                .addFrame( textBuilder(player) + animationFrame(player)[2])
                .addFrame( textBuilder(player) + animationFrame(player)[3])
                .setSpeed(20 / 2);

        HologramAPI.getHologram().spawn(hologram, player.getLocation().add(Config.getLocation()));
        HologramAPI.getHologram().applyAnimation(hologram, animation);
        attach(hologram, player);
    }

    private static String textBuilder(Player player) {
        String playerName = "";
        String indentation = "";
        if (Config.isShowNames()) {
            playerName = "<color:" + Config.getNamesColor() + ">" + player.getName();
            if (Config.isIndentation()) {
                indentation = "\n";
            } else {
                playerName += " ";
            }
        }
        return playerName + indentation;
    }
    private static String[] animationFrame(Player player) {
        String[] strings = new String[4];
        String typingChar = Config.getTypingChar();
        String typingColor = "<color:" + Config.getTypingIconColor() + ">";
        strings[0] = typingColor + "[" + typingChar + "]";
        strings[1] = typingColor + "[" + typingChar + ".]";
        strings[2] = typingColor + "[" + typingChar + "..]";
        strings[3] = typingColor + "[" + typingChar + "...]";
        return strings;
    }
    private static int color(char c) {
        String hex = Config.getBackgroundColor().substring(1);
        if (c == 'R') {
            return Integer.parseInt(hex.substring(0, 2), 16);
        } else if (c == 'G') {
            return Integer.parseInt(hex.substring(2, 4), 16);
        } else if (c == 'B') {
            return Integer.parseInt(hex.substring(4, 6), 16);
        }
        return 0;
    }

    public static void attach(TextHologram hologram, Entity entity) {
        int[] hologramToArray = { hologram.getEntityID() };
        WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(entity.getEntityId(), hologramToArray);
        HologramAPI.getInstance().getServer().getScheduler().runTask(HologramAPI.getInstance(), () -> sendPacket(packet, hologram));
    }
    private static void sendPacket(PacketWrapper<?> packet, TextHologram hologram) {
        List<Player> list = new ArrayList<>(hologram.getViewers());
        for (Player player : list) {
            HologramAPI.getPlayerManager().sendPacket(player, packet);
        }
    }
    public static void remove(Player player) {
        TextHologram hologram = HologramAPI.getHologram().getHologramsMap().get(player.getUniqueId().toString());
        if (hologram != null) {
            HologramAPI.getHologram().cancelAnimation(hologram);
            HologramAPI.getHologram().remove(player.getUniqueId().toString());
        }
    }
}
