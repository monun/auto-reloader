package io.github.monun.autoreloader.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;

public class AutoReloaderPlugin extends JavaPlugin {
    private File updateFile;

    private UpdateAction updateAction;

    private int countdownSeconds = 2;

    @Override
    public void onEnable() {
        Server server = getServer();

        try {
            Field field = SimplePluginManager.class.getDeclaredField("updateDirectory");
            field.setAccessible(true);
            File updateDirectory = (File) field.get(server.getPluginManager());
            updateFile = new File(updateDirectory, "UPDATE");
            updateFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
            return;
        }

        saveDefaultConfig();
        Configuration config = getConfig();
        updateAction = UpdateAction.valueOf(Objects.requireNonNull(config.getString("update-action")).toUpperCase());
        countdownSeconds = config.getInt("countdown-seconds");

        BukkitScheduler scheduler = server.getScheduler();
        scheduler.runTaskTimer(this, this::monitor, 20L, 20L);
    }

    private boolean update = false;
    private int updateSeconds = 0;

    private void monitor() {
        if (update) {
            if (--updateSeconds <= 0) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), updateAction.commands);
            }

            return;
        }

        if (updateFile.exists()) {
            update = true;
            updateSeconds = countdownSeconds;

            Bukkit.broadcastMessage(ChatColor.YELLOW + "UPDATE file has been detected.");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Server will " + updateAction.message + " in " + updateSeconds + " seconds");
        }
    }
}


