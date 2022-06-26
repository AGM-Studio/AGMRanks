package me.ashenguard.agmranks.commands;

import me.ashenguard.agmranks.AGMRanks;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecutor {
    private static final Pattern PERM_PATTERN = Pattern.compile("[Pp]erm:\s?(.+)\s");
    private static final Pattern SUDO_PATTERN = Pattern.compile("^\s*[Ss]udo");

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

        this.command = StringUtils.trim(command);
    }

    public void execute(Player player) {
        for (String permission:permissions) if (!player.hasPermission(permission)) return;

        if (sudo) Bukkit.dispatchCommand(player, command);
        else Bukkit.dispatchCommand(AGMRanks.getCommandSender(), command);
    }
}