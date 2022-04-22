package me.ashenguard.agmranks.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("ConstantConditions")
public class CustomCommandSender implements RemoteConsoleCommandSender {
    @Override public void sendMessage(@NotNull String message) {}
    @Override public void sendMessage(@NotNull String... messages) {}
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String message) {}
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {}

    @Override public @NotNull Server getServer() {
        return Bukkit.getServer();
    }

    @Override public @NotNull String getName() {
        return "AGMRanks";
    }

    public static class CustomSpigot extends CommandSender.Spigot {
        public void sendMessage(@NotNull BaseComponent component) {}
        public void sendMessage(@NotNull BaseComponent... components) {}
        public void sendMessage(@Nullable UUID sender, @NotNull BaseComponent component) {}
        public void sendMessage(@Nullable UUID sender, @NotNull BaseComponent... components) {}
    }

    @Override public @NotNull Spigot spigot() {
        return new CustomSpigot();
    }

    @Override public boolean isPermissionSet(@NotNull String name) {
        return true;
    }
    @Override public boolean isPermissionSet(@NotNull Permission perm) {
        return true;
    }

    @Override public boolean hasPermission(@NotNull String name) {
        return true;
    }
    @Override public boolean hasPermission(@NotNull Permission perm) {
        return true;
    }

    @Override public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return null;
    }
    @Override public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return null;
    }
    @Override public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return null;
    }
    @Override public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return null;
    }

    @Override public void removeAttachment(@NotNull PermissionAttachment attachment) {}

    @Override public void recalculatePermissions() {}

    @Override public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return new HashSet<>();
    }

    @Override public boolean isOp() {
        return true;
    }

    @Override public void setOp(boolean value) {}
}
