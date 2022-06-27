package me.ashenguard.agmranks.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecutor {
    private static final Pattern PERM_PATTERN = Pattern.compile("[Pp]erm:\s?(.+)\s");
    private static final Pattern SUDO_PATTERN = Pattern.compile("^\s*[Ss]udo");
    private static final Pattern TRIM_PATTERN = Pattern.compile("^\s*(.*)\s*$");

    private final List<String> permissions = new ArrayList<>();
    private final boolean sudo;
    private final String command;

    public CommandExecutor(String command) {
        Matcher permMatcher = PERM_PATTERN.matcher(command);
        while (permMatcher.find()) {
            command = command.replace(permMatcher.group(), "");
            permissions.add(permMatcher.group(1));
        }

        Matcher sudoMatcher = SUDO_PATTERN.matcher(command);
        if (sudoMatcher.find()) {
            command = command.replace(sudoMatcher.group(), "");
            this.sudo = true;
        } else this.sudo = false;

        Matcher trimMatcher = TRIM_PATTERN.matcher(command);
        if (trimMatcher.find()) this.command = trimMatcher.group(1);
        else this.command = command;
    }

    public void execute(Player player) {
        for (String permission:permissions) if (!player.hasPermission(permission)) return;

        if (sudo) Bukkit.dispatchCommand(player, command);
        else Bukkit.dispatchCommand(AGMRanksCommandSender.getInstance(), command);
    }
}