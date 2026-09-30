package jp.azisaba.main.resourceworld.listeners;

import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import jp.azisaba.main.resourceworld.RecreateWorld;
import jp.azisaba.main.resourceworld.ResourceWorld;
import jp.azisaba.main.resourceworld.utils.Safety;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CreateSafetySpawnListener implements Listener {

    private final ResourceWorld plugin;
    private final Set<String> worlds;
    private final NamespacedKey regeneratedAt;

    public CreateSafetySpawnListener(ResourceWorld plugin, List<RecreateWorld> worlds) {
        this.plugin = plugin;
        this.worlds = worlds.stream().map(RecreateWorld::getWorldName).collect(Collectors.toSet());
        this.regeneratedAt = new NamespacedKey(plugin, "regenerated_at");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(AsyncPlayerSpawnLocationEvent event) {
        Location savedLocation = event.getSpawnLocation();
        if (!worlds.contains(savedLocation.getWorld().getName())) {
            return;
        }
        UUID playerId = event.getConnection().getProfile().getId();
        try {
            // World/block/player-data access must run on the server thread, before the player is placed.
            Location spawn = plugin.getServer().getScheduler().callSyncMethod(plugin, () -> {
                World world = plugin.getServer().getWorld(savedLocation.getWorld().getName());
                if (world == null) {
                    throw new IllegalStateException("ログイン先ワールドがアンロードされました。");
                }
                Location location = savedLocation.clone();
                location.setWorld(world);
                long generation = world.getPersistentDataContainer().getOrDefault(regeneratedAt,
                        PersistentDataType.LONG, 0L);
                long lastSeen = plugin.getServer().getOfflinePlayer(playerId).getLastSeen();
                return Safety.getLoginLocation(location, lastSeen, generation);
            }).get();
            event.setSpawnLocation(spawn);
        } catch (InterruptedException | ExecutionException | RuntimeException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            plugin.getLogger().log(Level.SEVERE, "ログイン時の安全なスポーン地点を取得できませんでした。", e);
            event.getConnection().disconnect(Component.text("安全なログイン地点を取得できませんでした。再接続してください。"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Location location = event.getRespawnLocation();
        if (worlds.contains(location.getWorld().getName()) && !Safety.isSafe(location)) {
            event.setRespawnLocation(Safety.getSafeSpawn(location.getWorld().getSpawnLocation()));
        }
    }
}
