package ir.syrent.velocityvanish.spigot.ruom.adventure;

import ir.syrent.velocityvanish.spigot.ruom.Ruom;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class AdventureApi {

    private static BukkitAudiences adventure;

    public static BukkitAudiences get() {
        return adventure;
    }

    public static void initialize() {
        if (adventure == null)
            adventure = BukkitAudiences.create(Ruom.getPlugin());
    }

}
