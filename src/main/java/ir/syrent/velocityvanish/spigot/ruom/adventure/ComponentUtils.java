package ir.syrent.velocityvanish.spigot.ruom.adventure;

import ir.syrent.velocityvanish.spigot.ruom.Ruom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class ComponentUtils {

    static {
        Ruom.initializeAdventure();
    }

    public static void send(Player player, Component component) {
        ir.syrent.velocityvanish.spigot.ruom.adventure.AdventureApi.get().player(player).sendMessage(component);
    }

    public static void send(Component component, Player... players) {
        for (Player player : players) {
            send(player, component);
        }
    }

    public static Component parse(String string) {
        return MiniMessage.miniMessage().deserialize(parseComponentColors(string));
    }

    public static String parseComponentColors(String msg) {
        return msg.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "dark_blue")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&n", "<underlined>")
                .replace("&m", "<strikethrough>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>")
                .replace("&k", "<obfuscated>");
    }

}