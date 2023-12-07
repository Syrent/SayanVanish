package ir.syrent.nms.accessors;

import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Class generated by NMS Mapper.
 * <p>
 * This class is a reflection accessor for net.minecraft.server.network.ServerGamePacketListenerImpl
 *
 * @since 2023-10-01 13:06:03
 */
public class ServerGamePacketListenerImplAccessor {
  /**
   * This method returns the {@link Class} object of the requested NMS class.
   * <p>
   * This method is safe to call: exception is handled and null is returned in case of failure.
   *
   * @return the resolved class object or null if the class does not exist
   */
  public static Class<?> getType() {
    return AccessorUtils.getType(ServerGamePacketListenerImplAccessor.class, mapper -> {

          /* SEARGE */
          mapper.map("SEARGE", "1.8.8", "net.minecraft.network.NetHandlerPlayServer"); // 1.8.8 - 1.13.2
          mapper.map("SEARGE", "1.14", "net.minecraft.network.play.ServerPlayNetHandler"); // 1.14 - 1.16.5
          mapper.map("SEARGE", "1.17", "net.minecraft.src.C_31_"); // 1.17 - 1.20.2

          /* SPIGOT */
          mapper.map("SPIGOT", "1.8.8", "net.minecraft.server.${V}.PlayerConnection"); // 1.8.8 - 1.16.5
          mapper.map("SPIGOT", "1.17", "net.minecraft.server.network.PlayerConnection"); // 1.17 - 1.20.2

        });
  }

  /**
   * This method returns the {@link Method} object of the requested NMS method.
   * <p>
   * Requested method: send, mapping: mojang
   * Parameters of requested method: (net.minecraft.network.protocol.Packet)
   * <p>
   * This method is safe to call: exception is handled and null is returned in case of failure.
   *
   * @return the method object or null if either class does not exist or it does not have this field in the specific environment
   */
  public static Method getMethodSend1() {
    return AccessorUtils.getMethod(ServerGamePacketListenerImplAccessor.class, "send1", mapper -> {

          /* SEARGE */
          mapper.map("SEARGE", "1.8.8", "func_147359_a"); // 1.8.8 - 1.16.5
          mapper.map("SEARGE", "1.17", "m_141995_"); // 1.17 - 1.18.2
          mapper.map("SEARGE", "1.19", "m_9829_"); // 1.19 - 1.20.1

          /* SPIGOT */
          mapper.map("SPIGOT", "1.8.8", "sendPacket"); // 1.8.8 - 1.17.1
          mapper.map("SPIGOT", "1.18", "a"); // 1.18 - 1.20.1

        }, PacketAccessor.getType());
  }

  /**
   * This method returns the {@link Field} object of the requested NMS field.
   * <p>
   * Requested field: connection, mapping: mojang
   * <p>
   * This method is safe to call: exception is handled and null is returned in case of failure.
   *
   * @return the resolved field object or null if either class does not exist or it does not have this field in the specific environment
   */
  public static Field getFieldConnection() {
    return AccessorUtils.getField(ServerGamePacketListenerImplAccessor.class, "connection1", mapper -> {

          /* SEARGE */
          mapper.map("SEARGE", "1.8.8", "field_147371_a"); // 1.8.8 - 1.16.5
          mapper.map("SEARGE", "1.17", "f_9742_"); // 1.17 - 1.20.1

          /* SPIGOT */
          mapper.map("SPIGOT", "1.8.8", "networkManager"); // 1.8.8 - 1.16.5
          mapper.map("SPIGOT", "1.17", "a"); // 1.17 - 1.18.2
          mapper.map("SPIGOT", "1.19", "b"); // 1.19 - 1.19.3
          mapper.map("SPIGOT", "1.19.4", "h"); // 1.19.4 - 1.20.1

        });
  }
}
