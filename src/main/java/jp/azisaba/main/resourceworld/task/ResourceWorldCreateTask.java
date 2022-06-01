package jp.azisaba.main.resourceworld.task;

import jp.azisaba.main.resourceworld.RecreateWorld;
import jp.azisaba.main.resourceworld.ResourceWorld;
import jp.azisaba.main.resourceworld.utils.TimeCalculateManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class ResourceWorldCreateTask {

    private ResourceWorld plugin;

    private List<RecreateWorld> worldList = new ArrayList<RecreateWorld>();

    private BukkitTask task;

    public ResourceWorldCreateTask(ResourceWorld plugin, List<RecreateWorld> worldList) {
        this.plugin = plugin;
        this.worldList = worldList;
    }

    public void runTask() {

        long waitTicks = getWaitTicks() / 2;

        if (plugin.config.logInConsole) {
            plugin.getLogger().info("次の時刻確認タスクを " + waitTicks + " tick(s) 後に実行します。");
        }

        task = getTask().runTaskLater(plugin, waitTicks);
    }

    public void stopTask() {
        if (task != null) {
            task.cancel();
        }
    }

    private long getWaitTicks() {
        long nextRecreate = TimeCalculateManager.getNextRecreate();
        long now = System.currentTimeMillis();

        double seconds = (double) (nextRecreate - now) / 1000;
        return (long) Math.floor(seconds * 20L);
    }

    private BukkitRunnable getTask() {
        return new BukkitRunnable() {
            public void run() {

                if (TimeCalculateManager.getNextRecreate() - System.currentTimeMillis() > 1000) {

                    long waitTicks = getWaitTicks() / 2;

                    if (waitTicks <= 0) {
                        waitTicks = 1;
                    }

                    if (plugin.config.logInConsole) {
                        plugin.getLogger()
                                .info("次の時刻確認タスクは " + waitTicks + " tick(s) 後に実行します。 ");
                    }
                    task = getTask().runTaskLater(plugin, waitTicks);
                    return;
                }

                ExecuteRecreateTask recreateTask = new ExecuteRecreateTask(plugin);
                worldList.forEach(recreateTask::add);

                recreateTask.run();

                task = getTask().runTaskLater(plugin, 20 * 60 * 60);
            }
        };
    }
}
