package jp.azisaba.main.resourceworld.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/** Dependency-free regression check, run by Gradle's check task. */
public class SpawnRegressionCheck {
    public static void main(String[] args) {
        for (World.Environment environment : new World.Environment[] {
                World.Environment.NORMAL, World.Environment.NETHER, World.Environment.THE_END}) {
            checkSpawn(environment, false);
            checkSpawn(environment, true);
        }
        System.out.println("Spawn regression checks passed (all three environments, safe terrain and lava).");
    }

    private static void checkSpawn(World.Environment environment, boolean lava) {
        Location[] center = {new Location(null, 0, 0, 0)};
        Map<String, Material> changedBlocks = new HashMap<>();
        WorldBorder border = proxy(WorldBorder.class, (object, method, args) -> switch (method.getName()) {
            case "setCenter" -> {
                center[0] = ((Location) args[0]).clone();
                yield null;
            }
            case "getCenter" -> center[0].clone();
            case "isInside" -> {
                Location location = (Location) args[0];
                yield Math.abs(location.getX() - center[0].getX()) < 32
                        && Math.abs(location.getZ() - center[0].getZ()) < 32;
            }
            default -> throw new AssertionError(method);
        });
        World world = proxy(World.class, (object, method, args) -> switch (method.getName()) {
            case "getSpawnLocation" -> new Location((World) object, -1235, 93, 847, 123, 12);
            case "getWorldBorder" -> border;
            case "getEnvironment" -> environment;
            case "getMinHeight" -> environment == World.Environment.NORMAL ? -64 : 0;
            case "getMaxHeight" -> environment == World.Environment.NORMAL ? 320 : 256;
            case "getBlockAt" -> {
                Location location = args[0] instanceof Location value ? value
                        : new Location((World) object, (int) args[0], (int) args[1], (int) args[2]);
                yield block(location.getBlockX(), location.getBlockY(), location.getBlockZ(), lava, changedBlocks);
            }
            default -> throw new AssertionError(method);
        });

        Location spawn = Safety.prepareSpawn(world);
        require(spawn.getWorld() == world, "Spawn must refer to the generated world");
        require(spawn.getX() == -1234.5 && spawn.getY() == 93 && spawn.getZ() == 847.5,
                "Generated spawn must survive fixed coordinates and the previous world's border");
        require(spawn.getYaw() == 123 && spawn.getPitch() == 12, "Spawn direction must be preserved");
        require(Safety.isSafe(spawn), "Spawn must be safe even when vanilla selects a lava location");
        require(lava || changedBlocks.isEmpty(), "Safe generated terrain must not be flattened");
    }

    private static Block block(int x, int y, int z, boolean lava, Map<String, Material> changedBlocks) {
        String key = x + "," + y + "," + z;
        return proxy(Block.class, (object, method, args) -> {
            Material type = changedBlocks.getOrDefault(key, y < 93
                    ? (lava && x == -1235 && y == 92 && z == 847 ? Material.LAVA : Material.STONE)
                    : Material.AIR);
            return switch (method.getName()) {
                case "getType" -> type;
                case "isPassable" -> type == Material.AIR || type == Material.LAVA;
                case "isLiquid" -> type == Material.LAVA;
                case "getCollisionShape" -> proxy(VoxelShape.class, (shape, shapeMethod, shapeArgs) -> {
                    if (shapeMethod.getName().equals("overlaps")) {
                        return type != Material.AIR && type != Material.LAVA
                                && new BoundingBox(0, 0, 0, 1, 1, 1).overlaps((BoundingBox) shapeArgs[0]);
                    }
                    throw new AssertionError(shapeMethod);
                });
                case "setType" -> {
                    changedBlocks.put(key, (Material) args[0]);
                    yield null;
                }
                default -> throw new AssertionError(method);
            };
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
