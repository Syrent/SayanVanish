package ir.syrent.velocityvanish.spigot.utils;

import com.cryptomorin.xseries.ReflectionUtils;

public class ServerVersion {

    /**
     * @return The server's version without "1.", That would be an integer.
     */
    public static int getVersion() {
        return ReflectionUtils.VER;
    }

    /**
     * @return true if the server is running on 1.8 - 1.12.2
     */
    public static boolean isLegacy() {
        return !supports(13);
    }

    /**
     * @return true if the server is running on 1.8.* or lower.
     */
    public static boolean isSuperLegacy() {
        return !supports(9);
    }

    /**
     * Checks whether the server version is equal or greater than the given version.
     * @param version the version to compare the server version with
     * @return true if the version is equal or newer, otherwise false
     */
    public static boolean supports(int version) {
        return ReflectionUtils.supports(version);
    }

}
