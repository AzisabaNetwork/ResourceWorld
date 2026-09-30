package jp.azisaba.main.resourceworld.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

public class Safety {

    public static Location prepareOverworldSpawn(World world, int protect) {
        Location spawn = new Location(world, 0.5, Math.clamp(63, world.getMinHeight() + 1,
                world.getMaxHeight() - 2), 0.5);
        int radius = Math.max(0, protect);
        createFloor(spawn, Material.STONE, radius, radius);
        createSpace(spawn, radius, 20, radius);
        return spawn;
    }

    public static Location getLoginLocation(Location savedLocation, long lastSeen, long regeneratedAt) {
        if (lastSeen < regeneratedAt || !isSafe(savedLocation)) {
            return getSafeSpawn(savedLocation.getWorld().getSpawnLocation());
        }
        return savedLocation;
    }

    public static Location getSafeSpawn(Location location) {
        Location spawn = location.clone();
        World world = spawn.getWorld();
        spawn.setX(spawn.getBlockX() + 0.5);
        spawn.setZ(spawn.getBlockZ() + 0.5);
        spawn.setY(Math.clamp(spawn.getBlockY(), world.getMinHeight() + 1, world.getMaxHeight() - 2));
        if (!world.getWorldBorder().isInside(spawn)) {
            Location center = world.getWorldBorder().getCenter();
            spawn.setX(center.getX());
            spawn.setZ(center.getZ());
        }
        if (!isSafe(spawn)) {
            Material floor = switch (world.getEnvironment()) {
                case NETHER -> Material.NETHERRACK;
                case THE_END -> Material.END_STONE;
                default -> Material.STONE;
            };
            createFloor(spawn, floor, 1, 1);
            createSpace(spawn, 1, 2, 1);
        }
        return spawn;
    }

    public static boolean isSafe(Location location) {
        World world = location.getWorld();
        if (location.getY() <= world.getMinHeight() || location.getY() + 1.8 >= world.getMaxHeight()
                || !world.getWorldBorder().isInside(location)) {
            return false;
        }
        BoundingBox body = new BoundingBox(location.getX() - 0.3, location.getY(), location.getZ() - 0.3,
                location.getX() + 0.3, location.getY() + 1.8, location.getZ() + 0.3);
        int floorY = Location.locToBlock(location.getY() - 0.01);
        Block floor = world.getBlockAt(location.getBlockX(), floorY, location.getBlockZ());
        if (floor.isPassable() && !floor.isLiquid() && !isHazard(floor.getType()) && floorY > world.getMinHeight()) {
            floor = world.getBlockAt(location.getBlockX(), --floorY, location.getBlockZ());
        }
        BoundingBox support = new BoundingBox(body.getMinX(), location.getY() - 1, body.getMinZ(),
                body.getMaxX(), location.getY(), body.getMaxZ());
        if (floor.isLiquid() || isHazard(floor.getType()) || !floor.getCollisionShape().overlaps(
                support.shift(-location.getBlockX(), -floorY, -location.getBlockZ()))) {
            return false;
        }
        // Check the whole player body, including adjacent blocks at fractional coordinates.
        for (int x = Location.locToBlock(location.getX() - 0.3); x <= Location.locToBlock(location.getX() + 0.3); x++) {
            for (int y = Math.max(world.getMinHeight(), location.getBlockY() - 1);
                 y <= Location.locToBlock(location.getY() + 1.8); y++) {
                for (int z = Location.locToBlock(location.getZ() - 0.3); z <= Location.locToBlock(location.getZ() + 0.3); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getCollisionShape().overlaps(body.clone().shift(-x, -y, -z))
                            || (y >= location.getBlockY() && (block.isLiquid() || isHazard(block.getType())))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isHazard(Material material) {
        return switch (material) {
            case LAVA, FIRE, SOUL_FIRE, CACTUS, MAGMA_BLOCK, CAMPFIRE, SOUL_CAMPFIRE,
                    SWEET_BERRY_BUSH, POWDER_SNOW, WITHER_ROSE -> true;
            default -> false;
        };
    }

    public static void createFloor(Location loc, Material material, int x1, int z1) {
        Location floor1 = loc.clone();
        floor1.subtract(x1, 1, z1);

        Location floor2 = loc.clone();
        floor2.add(x1, -1, z1);

        for (int x = floor1.getBlockX(); x <= floor2.getBlockX(); x++) {
            for (int y = floor1.getBlockY(); y <= floor2.getBlockY(); y++) {
                for (int z = floor1.getBlockZ(); z <= floor2.getBlockZ(); z++) {

                    Location l = new Location(loc.getWorld(), x, y, z);

                    if (l.getBlock().getType() != material)
                        l.getBlock().setType(material, false);
                }
            }
        }
    }

    public static void createSpace(Location loc, int x1, int y1, int z1) {
        Location pos1 = loc.clone();
        pos1.subtract(x1, 0, z1);

        Location pos2 = loc.clone();
        pos2.add(x1, y1, z1);
        pos2.setY(Math.min(pos2.getY(), loc.getWorld().getMaxHeight() - 1));

        for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
            for (int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++) {
                for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {

                    Location l = new Location(loc.getWorld(), x, y, z);

                    if (l.getBlock().getType() != Material.AIR && l.getBlock().getType() != Material.VOID_AIR)
                        l.getBlock().setType(Material.AIR, false);
                }
            }
        }
    }
}
