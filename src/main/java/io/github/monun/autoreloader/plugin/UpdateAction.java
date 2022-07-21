package io.github.monun.autoreloader.plugin;

public enum UpdateAction {
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
