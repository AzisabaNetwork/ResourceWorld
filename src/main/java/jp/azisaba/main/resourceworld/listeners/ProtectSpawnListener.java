package jp.azisaba.main.resourceworld.listeners;

import jp.azisaba.main.resourceworld.ProtectManager;
import jp.azisaba.main.resourceworld.RecreateWorld;
import jp.azisaba.main.resourceworld.ResourceWorld;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ProtectSpawnListener implements Listener {

    //	private ResourceWorld plugin;
    private HashMap<String, RecreateWorld> worldMap = new HashMap<String, RecreateWorld>();
    private List<Material> protectableMaterials = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST,
            Material.CRAFTING_TABLE);

    public ProtectSpawnListener(ResourceWorld plugin, List<RecreateWorld> worlds) {
        //		this.plugin = plugin;

        if (worlds == null) {
            return;
        }

        for (RecreateWorld world : worlds) {
            worldMap.put(world.getWorldName(), world);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreakEvent(BlockBreakEvent e) {

        if (ProtectManager.get() == false) {
            return;
        }

        Player p = e.getPlayer();
        World world = p.getWorld();

        if (!worldMap.containsKey(world.getName())) {
            return;
        }

        RecreateWorld rWorld = worldMap.get(world.getName());
        Location spawnPoint = world.getSpawnLocation();
        Location breakPoint = e.getBlock().getLocation();

        if (spawnPoint == null) {
            return;
        }

        if (!isCenterArea(breakPoint, spawnPoint, rWorld.getProtect())) {
            return;
        }

        if (Arrays.asList(Material.WHEAT, Material.CARROTS, Material.POTATOES).contains(e.getBlock().getType())) {
            return;
        }

        if (p.getGameMode() != GameMode.SURVIVAL) {
            if (p.hasPermission("resourceworld.centerprotect.bypass")) {
                return;
            }
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {

        if (ProtectManager.get() == false) {
            return;
        }

        Player p = e.getPlayer();
        World world = p.getWorld();

        if (!worldMap.containsKey(world.getName())) {
            return;
        }

        RecreateWorld rWorld = worldMap.get(world.getName());
        Location spawnPoint = world.getSpawnLocation();
        Location breakPoint = e.getBlock().getLocation();

        if (spawnPoint == null) {
            return;
        }

        if (!isCenterArea(breakPoint, spawnPoint, rWorld.getProtect())) {
            return;
        }

        if (Arrays.asList(Material.WHEAT, Material.CARROTS, Material.POTATOES).contains(e.getBlock().getType())) {
            return;
        }

        if (p.getGameMode() != GameMode.SURVIVAL) {
            if (p.hasPermission("resourceworld.centerprotect.bypass")) {
                return;
            }
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockFromTo(BlockFromToEvent e) {

        if (ProtectManager.get() == false) {
            return;
        }

        Block block = e.getToBlock();

        if (!worldMap.containsKey(block.getWorld().getName())) {
            return;
        }

        RecreateWorld rWorld = worldMap.get(block.getWorld().getName());

        if (!isCenterArea(block.getLocation(), block.getWorld().getSpawnLocation(), rWorld.getProtect())) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWaterFill(PlayerBucketFillEvent e) {

        if (ProtectManager.get() == false) {
            return;
        }

        Player p = e.getPlayer();
        World world = p.getWorld();
        Block block = e.getBlockClicked();

        if (!worldMap.containsKey(world.getName())) {
            return;
        }

        RecreateWorld rWorld = worldMap.get(world.getName());
        Location spawnPoint = world.getSpawnLocation();
        Location waterLoc = block.getLocation();

        if (spawnPoint == null) {
            return;
        }

        if (!isCenterArea(waterLoc, spawnPoint, rWorld.getProtect())) {
            return;
        }

        if (p.getGameMode() != GameMode.SURVIVAL) {
            if (p.hasPermission("resourceworld.centerprotect.bypass")) {
                return;
            }
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWaterEmpty(PlayerBucketEmptyEvent e) {

        if (ProtectManager.get() == false) {
            return;
        }

        Player p = e.getPlayer();
        World world = p.getWorld();
        Block block = e.getBlockClicked();

        if (!worldMap.containsKey(world.getName())) {
            return;
        }

        RecreateWorld rWorld = worldMap.get(world.getName());
        Location spawnPoint = world.getSpawnLocation();
        Location waterLoc = block.getLocation();

        if (spawnPoint == null) {
            return;
        }

        if (!isCenterArea(waterLoc, spawnPoint, rWorld.getProtect())) {
            return;
        }

        if (p.getGameMode() != GameMode.SURVIVAL) {
            if (p.hasPermission("resourceworld.centerprotect.bypass")) {
                return;
            }
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void noUproot(PlayerInteractEvent e) {

        if (ProtectManager.get() == false) {
            return;
        }

        Player p = e.getPlayer();
        World world = p.getWorld();

        if (!worldMap.containsKey(world.getName())) {
            return;
        }

        /**
         * Protect from everyone
         */
        //		if (p.getGameMode() != GameMode.SURVIVAL) {
        //			if (p.hasPermission("resourceworld.centerprotect.bypass")) {
        //				return;
        //			}
        //		}

        RecreateWorld rWorld = worldMap.get(world.getName());
        Location spawnPoint = world.getSpawnLocation();

        if (spawnPoint == null) {
            return;
        }

        if (e.getClickedBlock() == null) {
            return;
        }

        if (!isCenterArea(e.getClickedBlock().getLocation(), spawnPoint, rWorld.getProtect())) {
            return;
        }

        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.FARMLAND) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockFall(EntityChangeBlockEvent e) {
        Entity ent = e.getEntity();
        World world = ent.getWorld();

        if (!worldMap.containsKey(world.getName())) {
            return;
        }

        RecreateWorld rWorld = worldMap.get(world.getName());
        Location spawnPoint = world.getSpawnLocation();

        if (spawnPoint == null) {
            return;
        }

        if (!isCenterArea(e.getBlock().getLocation(), spawnPoint, rWorld.getProtect())) {
            return;
        }

        if ((e.getEntityType() == EntityType.FALLING_BLOCK)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void cancelLockette(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        World world = p.getWorld();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (e.getItem() == null) {
            return;
        }

        if (e.getItem().getType() != Material.SIGN || !protectableMaterials.contains(e.getClickedBlock().getType())) {
            return;
        }

        Block signBlock = e.getClickedBlock().getRelative(e.getBlockFace());

        if (!worldMap.containsKey(world.getName())) {
            return;
        }

        RecreateWorld rWorld = worldMap.get(world.getName());
        Location spawnPoint = world.getSpawnLocation();

        if (spawnPoint == null) {
            return;
        }

        if (!isCenterArea(signBlock.getLocation(), spawnPoint, rWorld.getProtect())) {
            return;
        }

        e.setCancelled(true);
        p.sendMessage(ChatColor.RED + "中央のため保護できません。");
    }

    private boolean isCenterArea(Location breakLoc, Location spawnLoc, int protect) {

        spawnLoc = spawnLoc.clone();
        spawnLoc.setX(spawnLoc.getBlockX());
        spawnLoc.setZ(spawnLoc.getBlockZ());

        if (protect <= 0) {
            return false;
        }

        if (Math.abs(breakLoc.getX()) - Math.abs(spawnLoc.getX()) >= protect) {
            return false;
        }
        if (Math.abs(breakLoc.getZ()) - Math.abs(spawnLoc.getZ()) >= protect) {
            return false;
        }

        return true;
    }
}
