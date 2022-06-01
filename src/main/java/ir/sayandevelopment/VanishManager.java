package ir.sayandevelopment;

import ir.sayandevelopment.spigot.SpigotMain;
import me.mohamad82.ruom.Ruom;
import me.mohamad82.ruom.adventure.text.minimessage.MiniMessage;
import me.mohamad82.ruom.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import me.mohamad82.ruom.misc.CharAnimation;
import me.mohamad82.ruom.utils.PlayerUtils;
import me.mohamad82.ruom.utils.StringUtils;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.stream.Collectors;

public class VanishManager {

    private static VanishManager instance;
    public static VanishManager getInstance() {
        return instance;
    }

    public VanishManager() {
        instance = this;
    }

    /*
    VANISH SECTION
     */

    public void sendLeaveMessage(String userName) {
        PlayerUtils.sendMessage(StringUtils.colorize(SpigotMain.getInstance().configYML.getConfig().getString("leave")
                        .replace("%player_name%", userName))
                , Ruom.getOnlinePlayers().toArray(new Player[0]));
    }

    public void setVanishMeta(Player player, boolean meta) {
            player.setMetadata("vanished", new FixedMetadataValue(Ruom.getPlugin(), meta));
    }

    public boolean isInvisible(Player player) {
        return SpigotMain.vanishedPlayers.contains(player.getName());
    }

    public void vanishPlayer(Player player) {
        try {
            setVanishMeta(player, true);

            for (Player onlinePlayer : Ruom.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("sayanvanish.seevanished"))
                    onlinePlayer.hidePlayer(Ruom.getPlugin(), player);
            }

            player.setAllowFlight(true);

            sendVanishActionBar(player);

            player.setSleepingIgnored(true);

            try {
                player.setCollidable(false);
            } catch (NoClassDefFoundError | NoSuchMethodError e) {
                try {
                    player.spigot().setCollidesWithEntities(false);
                } catch (NoClassDefFoundError | NoSuchMethodError e1) {
                    e.printStackTrace();
                }
            }

            player.getWorld().getEntities().stream()
                    .filter(entity -> entity instanceof Creature)
                    .map(entity -> (Creature) entity)
                    .filter(mob -> mob.getTarget() != null)
                    .filter(mob -> player.getUniqueId().equals(mob.getTarget().getUniqueId()))
                    .forEach(mob -> mob.setTarget(null));

            addPotionEffects(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unvanishPlayer(Player player) {
        try {
            setVanishMeta(player, false);

            for (Player onlinePlayer : Ruom.getOnlinePlayers()) onlinePlayer.showPlayer(Ruom.getPlugin(), player);

            player.setSleepingIgnored(false);

            try {
                player.setCollidable(true);
            } catch (NoClassDefFoundError | NoSuchMethodError e) {
                try {
                    player.spigot().setCollidesWithEntities(true);
                } catch (NoClassDefFoundError | NoSuchMethodError e1) {
                    e.printStackTrace();
                }
            }

            removePotionEffects(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPotionEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 99999, 255, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999, 0, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 99999, 255, false, false, false));
    }

    public void sendVanishActionBar(Player player) {
        CharAnimation charAnimation = new CharAnimation(CharAnimation.Style.SQUARE_BLOCK);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!SpigotMain.vanishedPlayers.contains(player.getName())) {
                        cancel();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        BungeeComponentSerializer.get().serialize(MiniMessage.miniMessage().parse(String.format(
                                "<color:#F8BD04>%s <gradient:#C1D6F1:#929CCB>You are now invisible to other players!</gradient>",
                                charAnimation.get()))));
            }
        }.runTaskTimer(SpigotMain.getInstance(), 20, 20);
    }

    public void sendVanishMessageToOthers(String userName) {
        Ruom.getOnlinePlayers().stream().
                filter(staffPlayer -> staffPlayer.hasPermission("sayanvaish.staff.vanishmessage")).collect(Collectors.toList())
                .forEach(staffPlayer -> staffPlayer.sendMessage(StringUtils.colorize(
                        String.format("&a%s is now invisible to other players.", userName))));
    }

    /*
    UNVANISH SECTION
     */

    public void sendJoinMessage(String userName) {
        PlayerUtils.sendMessage(StringUtils.colorize(SpigotMain.getInstance().configYML.getConfig().getString("join")
                        .replace("%player_name%", userName))
                , Ruom.getOnlinePlayers().toArray(new Player[0]));
    }

    public void removePotionEffects(Player player) {
        player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
    }

    public void sendUnVanishMessageToOthers(String userName) {
        Ruom.getOnlinePlayers().stream().
                filter(staffPlayer -> staffPlayer.hasPermission("sayanvanish.staff.vanishmessage")).collect(Collectors.toList())
                .forEach(staffPlayer -> staffPlayer.sendMessage(StringUtils.colorize(
                        String.format("&c%s is now visible to other players.", userName))));
    }
}
