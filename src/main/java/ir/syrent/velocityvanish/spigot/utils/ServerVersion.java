package ir.syrent.velocityvanish.spigot.utils;

import com.cryptomorin.xseries.ReflectionUtils;

public class ServerVersion {

    public static int getVersion() {
        return ReflectionUtils.MINOR_NUMBER;
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
     * @return The server's patch number. Example return: 1.19.3 -> 3
     */
    public static int getPatchNumber() {
        return ReflectionUtils.PATCH_NUMBER;
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
