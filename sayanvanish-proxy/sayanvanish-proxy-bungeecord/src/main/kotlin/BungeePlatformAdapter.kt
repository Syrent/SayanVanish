import org.sayandev.sayanvanish.api.PlatformAdapter
import org.sayandev.sayanvanish.api.VanishUser
import org.sayandev.sayanvanish.bungeecord.api.BungeeVanishUser

object BungeePlatformAdapter : PlatformAdapter<BungeeVanishUser> {
    override fun adapt(user: VanishUser): BungeeVanishUser {
        return BungeeVanishUser.fromUser(user)
    }

    @JvmStatic
    fun get(): BungeePlatformAdapter {
        return BungeePlatformAdapter
    }
}