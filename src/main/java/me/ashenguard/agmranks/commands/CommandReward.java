package me.ashenguard.agmranks.commands;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.RankBatch;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandReward {
    private static final Pattern[] PERM_PATTERN = {
            Pattern.compile("[Pp]erm:\s?(.+)\s"),
            Pattern.compile("[Pp]ermission:\s?(.+)\s")
    };
    private static final Pattern PRESTIGE_PATTERN = Pattern.compile("[Pp]restige:\s?(\\d+)\s");
    private static final Pattern SUDO_PATTERN = Pattern.compile("^\s*[Ss]udo");
    private static final Pattern TRIM_PATTERN = Pattern.compile("^\s*(.*)\s*$");

    private final Set<String> permissions = new HashSet<>();
    private final Set<Integer> prestiges = new HashSet<>();
    private final boolean sudo;
    private final String command;
    private final RankBatch batch;

    public CommandReward(RankBatch batch, String command) {
        this.batch = batch;
        for (Pattern pattern: PERM_PATTERN) {
            Matcher permMatcher = pattern.matcher(command);
            while (permMatcher.find()) {
                command = command.replace(permMatcher.group(), "");
                permissions.add(permMatcher.group(1));
            }
        }

        Matcher prestigeMatcher = PRESTIGE_PATTERN.matcher(command);
        while (prestigeMatcher.find()) {
            command = command.replace(prestigeMatcher.group(), "");
            prestiges.add(Integer.parseInt(prestigeMatcher.group(1)));
        }

        Matcher sudoMatcher = SUDO_PATTERN.matcher(command);
        this.sudo = sudoMatcher.find();
        if (this.sudo) command = command.replace(sudoMatcher.group(), "");

        Matcher trimMatcher = TRIM_PATTERN.matcher(command);
        if (trimMatcher.find()) this.command = trimMatcher.group(1);
        else this.command = command;
    }

    public void execute(Player player) {
        if (!prestiges.contains(batch.getPlayerPrestige(player))) return;
        for (String permission:permissions) if (!player.hasPermission(permission)) return;

        if (sudo) Bukkit.dispatchCommand(player, command);
        else try {
            Bukkit.dispatchCommand(AGMRanksCommandSender.getInstance(), command);
        } catch (Throwable throwable) {
            AGMRanks.getMessenger().warning("Failed to execute commands with the AGMRanks' remote command sender. Attempting to execute command through console");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}