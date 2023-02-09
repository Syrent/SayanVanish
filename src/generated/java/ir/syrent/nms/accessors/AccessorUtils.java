package ir.syrent.nms.accessors;

/*
 * Copyright 2022 ScreamingSandals
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessorUtils {
    private static final Map<Class<?>, Class<?>> classCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, String>, Field> fieldCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, String>, Object> enumCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, String>, Method> methodCache = new HashMap<>();
    private static final Map<Map.Entry<Class<?>, Integer>, Constructor<?>> constructorCache = new HashMap<>();

    private static int MAJOR_VERSION;
    private static int MINOR_VERSION;
    private static int PATCH_VERSION;
    private static boolean availableMapping_SEARGE = false;
public static boolean isAvailableMapping_SEARGE() {
return availableMapping_SEARGE;
}
private static boolean availableMapping_SPIGOT = false;
public static boolean isAvailableMapping_SPIGOT() {
return availableMapping_SPIGOT;
}
private static String craftBukkitImpl = "v1_99_R9";

public static String getCraftBukkitImpl() {
    return craftBukkitImpl;
}


    static {
        try {
    Class<?> mcForgeClass = Class.forName("net.minecraftforge.common.MinecraftForge");
    availableMapping_SEARGE = true; // auto-generated field

    String str;
    try {
        // Flattening versions
        Class<?> mcpVersionClass = Class.forName("net.minecraftforge.versions.mcp.MCPVersion");
        str = mcpVersionClass.getMethod("getMCVersion").invoke(null).toString();
    } catch (Throwable ignored) {
        // Legacy versions
        str = mcForgeClass.getField("MC_VERSION").get(null).toString();
    }

    if (str != null) {
        int[] res = convertVersion(str);
        MAJOR_VERSION = res[0];
        MINOR_VERSION = res[1];
        PATCH_VERSION = res[2];
    }
} catch (Throwable ignored) {
    // probably not using MCP mappings
}
try {
    Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
    Method method = bukkitClass.getMethod("getServer");
    if (MAJOR_VERSION == 0) { // we don't know version yet, this server don't implement forge
        Pattern versionPattern = Pattern.compile("\\(MC: (\\d+)\\.(\\d+)\\.?(\\d+?)?");
        Matcher matcher = versionPattern.matcher(bukkitClass.getMethod("getVersion").invoke(null).toString());
        int majorVersion = 1;
        int minorVersion = 0;
        int patchVersion = 0;
        if (matcher.find()) {
            MatchResult matchResult = matcher.toMatchResult();
            try {
                majorVersion = Integer.parseInt(matchResult.group(1), 10);
            } catch (Exception ignored) {
            }
            try {
                minorVersion = Integer.parseInt(matchResult.group(2), 10);
            } catch (Exception ignored) {
            }
            if (matchResult.groupCount() >= 3) {
                try {
                    patchVersion = Integer.parseInt(matchResult.group(3), 10);
                } catch (Exception ignored) {
                }
            }
        }
        MAJOR_VERSION = majorVersion;
        MINOR_VERSION = minorVersion;
        PATCH_VERSION = patchVersion;
    }

    String packName = method.invoke(null).getClass().getPackage().getName();
    craftBukkitImpl = packName.substring(packName.lastIndexOf('.') + 1);
    availableMapping_SPIGOT = true;
} catch (Throwable ignored) {
    // probably not CraftBukkit Based
}

    }

    

    public static Class<?> getType(Class<?> accessor, Consumer<AccessorMapper> mapper) {
        Class<?> cache = classCache.get(accessor);
        if (cache != null) {
            return cache;
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        if (availableMapping_SEARGE) {
    // getting the last mapping
    String res = reduceMapping(map, "SEARGE");

    if (res != null) {
        // trying to use it
        try {
            Class<?> clazz = Class.forName(res);
            classCache.put(accessor, clazz);
            return clazz;
        } catch (Throwable ignored) {
        }
    }
}
if (availableMapping_SPIGOT) {
    // getting the last mapping
    String res = reduceMapping(map, "SPIGOT");

    if (res != null) {
        // trying to use it
        try {
            Class<?> clazz = Class.forName(res);
            classCache.put(accessor, clazz);
            return clazz;
        } catch (Throwable ignored) {
        }
    }
}


        // cache that we don't have it
        classCache.put(accessor, null);
        return null;
    }

    public static Field getField(Class<?> accessor, String field, Consumer<AccessorMapper> mapper) {
        Map.Entry<Class<?>, String> kvholder = new AbstractMap.SimpleEntry<>(accessor, field);
        if (fieldCache.containsKey(kvholder)) {
            return fieldCache.get(kvholder);
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        if (availableMapping_SEARGE) {
    // getting the last mapping
    String res = reduceMapping(map, "SEARGE");

    if (res != null) {
        // trying to use it
        try {
            Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
            Class<?> clazz1 = clazz;
            do {
                try {
                    Field fieldC = clazz1.getDeclaredField(res);
                    fieldC.setAccessible(true);
                    fieldCache.put(kvholder, fieldC);
                    return fieldC;
                } catch (Throwable ignored2) {
                }
            } while ((clazz1 = clazz1.getSuperclass()) != null && clazz1 != Object.class);
        } catch (Throwable ignored) {
        }
    }
}
if (availableMapping_SPIGOT) {
    // getting the last mapping
    String res = reduceMapping(map, "SPIGOT");

    if (res != null) {
        // trying to use it
        try {
            Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
            Class<?> clazz1 = clazz;
            do {
                try {
                    Field fieldC = clazz1.getDeclaredField(res);
                    fieldC.setAccessible(true);
                    fieldCache.put(kvholder, fieldC);
                    return fieldC;
                } catch (Throwable ignored2) {
                }
            } while ((clazz1 = clazz1.getSuperclass()) != null && clazz1 != Object.class);
        } catch (Throwable ignored) {
        }
    }
}


        // cache that we don't have it
        fieldCache.put(kvholder, null);
        return null;
    }

    public static Object getEnumField(Class<?> accessor, String field, Consumer<AccessorMapper> mapper) {
        Map.Entry<Class<?>, String> kvholder = new AbstractMap.SimpleEntry<>(accessor, field);
        if (enumCache.containsKey(kvholder)) {
            return enumCache.get(kvholder);
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        if (availableMapping_SEARGE) {
    String res = reduceMapping(map, "SEARGE");

    try {
        Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
        try {
            Field fieldC = clazz.getDeclaredField(res);
            fieldC.setAccessible(true);

            Object enumeration = fieldC.get(null);
            enumCache.put(kvholder, enumeration);
            return enumeration;
        } catch (Throwable ignored2) {
        }
    } catch (Throwable ignored) {}
}
if (availableMapping_SPIGOT) {
    String res = reduceMapping(map, "SPIGOT");

    try {
        Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
        try {
            Field fieldC = clazz.getDeclaredField(res);
            fieldC.setAccessible(true);

            Object enumeration = fieldC.get(null);
            enumCache.put(kvholder, enumeration);
            return enumeration;
        } catch (Throwable ignored2) {
        }
    } catch (Throwable ignored) {}
}


        // cache that we don't have it
        enumCache.put(kvholder, null);
        return null;
    }

    public static Method getMethod(Class<?> accessor, String method, Consumer<AccessorMapper> mapper, Class<?>... params) {
        Map.Entry<Class<?>, String> kvholder = new AbstractMap.SimpleEntry<>(accessor, method);
        if (methodCache.containsKey(kvholder)) {
            return methodCache.get(kvholder);
        }

        AccessorMapper accessorMapper = new AccessorMapper();
        mapper.accept(accessorMapper);

        Map<String, Map<int[], String>> map = accessorMapper.map;

        if (availableMapping_SEARGE) {
    // getting the last mapping
    String res = reduceMapping(map, "SEARGE");

    if (res != null) {
        try {
            Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
            Class<?> clazz2 = clazz;
            do {
                try {
                    Method methodC = clazz2.getDeclaredMethod(res, params);
                    methodC.setAccessible(true);
                    methodCache.put(kvholder, methodC);
                    return methodC;
                } catch (Throwable ignored2) {
                }
            } while ((clazz2 = clazz2.getSuperclass()) != null && clazz2 != Object.class);
        } catch (Throwable ignored) {
        }
    }
}
if (availableMapping_SPIGOT) {
    // getting the last mapping
    String res = reduceMapping(map, "SPIGOT");

    if (res != null) {
        try {
            Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
            Class<?> clazz2 = clazz;
            do {
                try {
                    Method methodC = clazz2.getDeclaredMethod(res, params);
                    methodC.setAccessible(true);
                    methodCache.put(kvholder, methodC);
                    return methodC;
                } catch (Throwable ignored2) {
                }
            } while ((clazz2 = clazz2.getSuperclass()) != null && clazz2 != Object.class);
        } catch (Throwable ignored) {
        }
    }
}


        // cache that we don't have it
        methodCache.put(kvholder, null);
        return null;
    }

    public static Constructor<?> getConstructor(Class<?> accessor, int constructor, Class<?>... params) {
        Map.Entry<Class<?>, Integer> kvholder = new AbstractMap.SimpleEntry<>(accessor, constructor);
        if (constructorCache.containsKey(kvholder)) {
            return constructorCache.get(kvholder);
        }

        try {
            Class<?> clazz = (Class<?>) accessor.getMethod("getType").invoke(null);
            Constructor<?> constructorC = clazz.getDeclaredConstructor(params);
            constructorC.setAccessible(true);
            constructorCache.put(kvholder, constructorC);
            return constructorC;
        } catch (Throwable ignored) {
        }

        // cache that we don't have it
        constructorCache.put(kvholder, null);
        return null;
    }

    public static boolean isVersion(int[] ver) {
        return isVersion(ver[0], ver[1], ver[2]);
    }

    public static boolean isVersion(int major, int minor, int patch) {
        return MAJOR_VERSION > major || (MAJOR_VERSION >= major && (MINOR_VERSION > minor || (MINOR_VERSION >= minor && PATCH_VERSION >= patch)));
    }

    public static int compare(int[] ver, int[] ver2) {
        if (ver[0] != ver2[0]) {
            return ver[0] - ver2[0];
        }
        if (ver[1] != ver2[1]) {
            return ver[1] - ver2[1];
        }
        return ver[2] - ver2[2];
    }

    public static int[] convertVersion(String version) {
        int[] result = new int[3];
        if (version != null) {
            String[] split = version.split("\\.");
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    result[0] = Integer.parseInt(split[i]);
                } else if (i == 1) {
                    result[1] = Integer.parseInt(split[i]);
                } else if (i == 2) {
                    result[2] = Integer.parseInt(split[i]);
                }
            }
        }
        return result;
    }

    public static String reduceMapping(Map<String, Map<int[], String>> map, String mapping) {
        Optional<String> opt = Optional.ofNullable(map.get(mapping))
                .flatMap(m -> m.entrySet()
                        .stream()
                        .filter(entry -> isVersion(entry.getKey()))
                        .sorted((o1, o2) -> compare(o1.getKey(), o2.getKey()))
                        .reduce((first, second) -> second)
                        .map(Map.Entry::getValue)
                );
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return null;
        }
    }

    public static class AccessorMapper {
        private final Map<String, Map<int[], String>> map = new HashMap<>();

        public AccessorMapper map(String mappingType, String minVersion, String symbol) {
            if (!map.containsKey(mappingType)) {
                map.put(mappingType, new HashMap<>());
            }
            if (mappingType.equals("SPIGOT")) {
    symbol = symbol.replace("${V}", craftBukkitImpl);
}

            Map<int[], String> map2 = map.get(mappingType);
            map2.put(convertVersion(minVersion), symbol);
            return this;
        }
    }

    public static Class<?> getOrCatch(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}