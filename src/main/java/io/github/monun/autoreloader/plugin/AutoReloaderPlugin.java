package io.github.monun.autoreloader.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Objects;
import java.util.logging.Logger;

public class AutoReloaderPlugin extends JavaPlugin {
    private File updateFolder;
    private UpdateAction updateAction;
    private int countdownTicks = 40;

    @Override
    public void onEnable() {
        Server server = getServer();
        Logger logger = getLogger();

        server.getPluginManager().clearPlugins();

        try {
            Field field = SimplePluginManager.class.getDeclaredField("updateDirectory");
            field.setAccessible(true);
            updateFolder = (File) field.get(server.getPluginManager());
            logger.info("Update directory: " + updateFolder.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
            return;
        }

        saveDefaultConfig();
        Configuration config = getConfig();
        updateAction = UpdateAction.valueOf(Objects.requireNonNull(config.getString("update-action")).toUpperCase());
        countdownTicks = config.getInt("countdown-ticks");

        BukkitScheduler scheduler = server.getScheduler();
        scheduler.runTask(this, () -> {
                    File[] files = updateFolder.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
                    if (files == null || files.length == 0) return;

                    for (File file : files) {
                        try {
                            Files.delete(file.toPath());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
        scheduler.runTaskTimer(this, this::monitor, 10L, 10L);
    }

    private int updateTicks = 0;

    private void monitor() {
        if (updateTicks > 0) {
            if (--updateTicks <= 0) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), updateAction.commands);
            }

            return;
        }

        File[] files = updateFolder.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));

        if (files != null && files.length > 0) {
            updateTicks = countdownTicks;

            Server server = getServer();
            server.broadcast(ChatColor.YELLOW + "There are plugin files to update.", "op");
            server.broadcast(ChatColor.YELLOW + "Server will " + updateAction.message + " in " + (countdownTicks + 19) / 20 + " seconds", "op");
        }
    }
}


enum UpdateAction {
    RELOAD("reload confirm", "reload"),
    RESTART("restart", "restart"),
    SHUTDOWN("stop", "shutdown");

    public final String commands;
    public final String message;

    UpdateAction(String commands, String message) {
        this.commands = commands;
        this.message = message;
    }
}
