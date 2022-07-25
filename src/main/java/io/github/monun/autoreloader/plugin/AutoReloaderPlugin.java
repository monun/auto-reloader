package io.github.monun.autoreloader.plugin;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;

public class AutoReloaderPlugin extends JavaPlugin {
    private File reloadFile;

    private UpdateAction updateAction;

    @Override
    public void onEnable() {
        Server server = getServer();

        try {
            Field field = SimplePluginManager.class.getDeclaredField("updateDirectory");
            field.setAccessible(true);
            File updateDirectory = (File) field.get(server.getPluginManager());
            updateDirectory.mkdirs();
            reloadFile = new File(updateDirectory, "RELOAD");

            try (BufferedWriter w = Files.newWriter(reloadFile, Charsets.UTF_8)) {
                w.write("Delete this file when you want to reload");
            }
        } catch (Exception e) {
            e.printStackTrace();
            setEnabled(false);
            return;
        }

        saveDefaultConfig();
        Configuration config = getConfig();
        updateAction = UpdateAction.valueOf(Objects.requireNonNull(config.getString("update-action")).toUpperCase());

        BukkitScheduler scheduler = server.getScheduler();
        scheduler.runTaskTimer(this, this::monitor, 4L, 4L);
    }

    private void monitor() {
        if (!reloadFile.exists()) {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Server will " + updateAction.message);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), updateAction.commands);
        }
    }
}


