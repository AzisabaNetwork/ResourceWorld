package jp.azisaba.main.resourceworld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ResourceWorldConfig {

    @ConfigOptions(path = "Chat.Prefix", type = OptionType.CHAT_FORMAT)
    public String chatPrefix = "&c[&6Resource&c] ";
    @ConfigOptions(path = "Chat.WarnMessage", type = OptionType.CHAT_FORMAT)
    public String chatWarning = "&e{TIME}後&aに資源ワールドを再生成します。";
    @ConfigOptions(path = "Log.LogInConsole")
    public boolean logInConsole = false;
    @ConfigOptions(path = "useMultiverse")
    public boolean useMultiverse = true;
    public List<RecreateWorld> createWorldList = new ArrayList<RecreateWorld>();
    private ResourceWorld plugin;
    private FileConfiguration conf;

    public ResourceWorldConfig(ResourceWorld plugin) {
        this.plugin = plugin;
        this.conf = plugin.getConfig();
    }

    public void loadConfig() {
        for (Field field : getClass().getFields()) {
            ConfigOptions anno = field.getAnnotation(ConfigOptions.class);

            if (anno == null) {
                continue;
            }

            String path = anno.path();

            if (conf.get(path) == null) {

                try {

                    if (anno.type() == OptionType.NONE) {
                        conf.set(path, field.get(this));
                    } else if (anno.type() == OptionType.LOCATION) {
                        Location loc = (Location) field.get(this);

                        conf.set(path, loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                                + "," + loc.getYaw() + "," + loc.getPitch());
                    } else if (anno.type() == OptionType.CHAT_FORMAT) {

                        String msg = (String) field.get(this);
                        conf.set(path, msg);

                        msg = msg.replace("&", "§");
                        field.set(this, msg);
                    } else if (anno.type() == OptionType.SOUND) {
                        conf.set(path, field.get(this).toString());
                    } else if (anno.type() == OptionType.LOCATION_LIST) {
                        @SuppressWarnings("unchecked")
                        List<Location> locations = (List<Location>) field.get(this);

                        List<String> strs = new ArrayList<String>();

                        if (!locations.isEmpty()) {

                            for (Location loc : locations) {
                                strs.add(loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + ","
                                        + loc.getZ()
                                        + "," + loc.getYaw() + "," + loc.getPitch());
                            }
                        } else {
                            strs.add("WorldName,X,Y,Z,Yaw,Pitch");
                        }

                        conf.set(path, strs);
                    }

                    plugin.saveConfig();
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {

                try {
                    if (anno.type() == OptionType.NONE) {
                        field.set(this, conf.get(path));
                    } else if (anno.type() == OptionType.LOCATION) {

                        String[] strings = conf.getString(path).split(",");
                        Location loc = null;
                        try {
                            loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                    Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                            loc.setYaw(Float.parseFloat(strings[4]));
                            loc.setPitch(Float.parseFloat(strings[5]));
                        } catch (Exception e) {
                            // None
                        }

                        if (loc == null) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, loc);
                    } else if (anno.type() == OptionType.SOUND) {

                        String name = conf.getString(path);
                        Sound sound;

                        try {
                            sound = Sound.valueOf(name.toUpperCase());
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error. " + path + " の値がロードできませんでした。");
                            continue;
                        }

                        field.set(this, sound);
                    } else if (anno.type() == OptionType.CHAT_FORMAT) {

                        String unformatMessage = conf.getString(path);

                        unformatMessage = unformatMessage.replace("&", "§");

                        field.set(this, unformatMessage);
                    } else if (anno.type() == OptionType.LOCATION_LIST) {

                        List<String> strList = conf.getStringList(path);

                        List<Location> locList = new ArrayList<Location>();

                        for (String str : strList) {

                            String[] strings = str.split(",");
                            Location loc = null;
                            try {
                                loc = new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]),
                                        Double.parseDouble(strings[2]), Double.parseDouble(strings[3]));
                                loc.setYaw(Float.parseFloat(strings[4]));
                                loc.setPitch(Float.parseFloat(strings[5]));
                            } catch (Exception e) {
                                // None
                            }

                            if (loc == null) {
                                Bukkit.getLogger().warning("Error. " + path + " の " + str + "がロードできませんでした。");
                                continue;
                            }

                            locList.add(loc);
                        }

                        field.set(this, locList);
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error. " + e.getMessage());
                }
            }
        }

        loadAdditionalSettings();
    }

    private void loadAdditionalSettings() {

        if (conf.getConfigurationSection("RecreateWorlds") == null) {
            conf.set("RecreateWorlds.ExWorldName.WorldBorder", 1000);
            conf.set("RecreateWorlds.ExWorldName.KeepInventory", true);
            conf.set("RecreateWorlds.ExWorldName.Environment", "Normal");
            conf.set("RecreateWorlds.ExWorldName.Portal", "PortalName");

            plugin.saveConfig();
            return;
        }

        for (String str : conf.getConfigurationSection("RecreateWorlds").getKeys(false)) {
            String worldName = str;
            double borderSize;
            boolean keepInventory;
            Environment env;
            String portal = null;
            int protect = -1;

            boolean canLoad = true;
            if (conf.get("RecreateWorlds." + str + ".WorldBorder") == null) {
                plugin.getLogger().info("'RecreateWorlds." + str + ".WorldBorder'" + " が指定されていません!");
                canLoad = false;
            }
            if (conf.get("RecreateWorlds." + str + ".KeepInventory") == null) {
                plugin.getLogger().info("'RecreateWorlds." + str + ".KeepInventory'" + " が指定されていません!");
                canLoad = false;
            }
            if (conf.get("RecreateWorlds." + str + ".Environment") == null) {
                plugin.getLogger().info("'RecreateWorlds." + str + ".Environment'" + " が指定されていません!");
                canLoad = false;
            }

            portal = conf.getString("RecreateWorlds." + str + ".Portal", null);
            protect = conf.getInt("RecreateWorlds." + str + ".Protect", -1);

            if (!canLoad) {
                continue;
            }

            borderSize = conf.getDouble("RecreateWorlds." + str + ".WorldBorder");
            keepInventory = conf.getBoolean("RecreateWorlds." + str + ".KeepInventory");

            try {

                String e = conf.getString("RecreateWorlds." + str + ".Environment");

                if (e.equalsIgnoreCase("end")) {
                    e = "THE_END";
                }

                env = Environment.valueOf(e.toUpperCase());
            } catch (Exception e) {
                plugin.getLogger().info(str + " の'Environment'の値が正しくありません。'Normal', 'Nether', 'End' のどれかを指定してください。");
                continue;
            }

            RecreateWorld rWorld = new RecreateWorld(worldName, env, borderSize, keepInventory, portal, protect);

            plugin.getLogger().info("==========[" + worldName + "]==========");
            plugin.getLogger().info("Env: " + env.toString());
            plugin.getLogger().info("WorldBorder: " + borderSize);
            plugin.getLogger().info("KeepInventory: " + keepInventory);

            if (portal != null) {
                plugin.getLogger().info("Portal: " + portal);
            }
            if (protect > 0) {
                plugin.getLogger().info("Protect: " + protect);
            }
            plugin.getLogger().info("==============================");

            createWorldList.add(rWorld);
        }
    }

    public enum OptionType {
        LOCATION, LOCATION_LIST, SOUND, CHAT_FORMAT, NONE
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigOptions {
        public String path();

        public OptionType type() default OptionType.NONE;
    }
}
