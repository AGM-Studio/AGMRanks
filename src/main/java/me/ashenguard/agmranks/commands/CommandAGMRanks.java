package me.ashenguard.agmranks.commands;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.gui.AdminGUI;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.AdvancedCommand;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.utils.encoding.Ordinal;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandAGMRanks extends AdvancedCommand {
    private final Messenger messenger = AGMRanks.getInstance().messenger;

    public CommandAGMRanks() {
        super(AGMRanks.getInstance(), "AGMRanks", true);
    }

    @Override
    protected boolean playerOnlyCondition(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 0;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Player player = (Player) sender;

            AdminGUI adminInventory = new AdminGUI(player);
            adminInventory.show();
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            messenger.send(sender, "Plugin has been reloaded");
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("newRank")) {
            Rank rank = new Rank(AGMRanks.getRankManager().getRanks().size() + 1);
            AGMRanks.getRankManager().saveRank(rank);

            messenger.send(sender, "A new rank with default values created, Check config files for §b" + Ordinal.to(rank.id) + ".yml§r file");
            messenger.send(sender, "NOTE: The new created rank will not be loaded until you reload the plugin. It's recommended to first configure new rank and then reload the plugin.");
        }

        List<String> expArgs = Arrays.asList("exp", "experience");
        if (args.length >= 4 && expArgs.contains(args[0].toLowerCase())) {
            Player player = Bukkit.getPlayer(args[2]);
            if (player == null) return;

            double amount;
            try {
                amount = Double.parseDouble(args[3]);
            } catch (NumberFormatException ignored) {
                return;
            }

            User user = UserManager.getUser(player);
            if (args[1].equalsIgnoreCase("give")) user.setExperience(amount + user.getExperience());
            else if (args[1].equalsIgnoreCase("set")) user.setExperience(amount);
            else if (args[1].equalsIgnoreCase("take")) user.setExperience(user.getExperience() - amount);

            messenger.send(sender, "Player now has a new amount of experience", String.format("Player: §c%s", player.getName()), String.format("Experience: §6%f", user.getExperience()));
        }

        if (args.length > 2 && args[0].equalsIgnoreCase("resetPlayer")) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) return;

            boolean resetHighestRank = args.length == 3 && args[2].equalsIgnoreCase("true");
            User user = UserManager.getUser(player);
            user.setRank(1);
            if (resetHighestRank)
                user.setHighestRank(1);

            messenger.send(sender, String.format("§c%s§r's rank is now set to 1st rank", player.getName()));
            if (resetHighestRank) messenger.send(sender, "Their highest rank is also now set to 1st as well");
        }
    }

    @Override
    public List<String> tabs(CommandSender commandSender, Command command, String alias, String[] args) {
        List<String> tabs = new ArrayList<>();
        if (args.length == 0 || args.length == 1) tabs = Arrays.asList("reload", "newRank", "exp", "experience", "resetPlayer");
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("resetPlayer"))
                tabs = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            else if (args[0].equalsIgnoreCase("exp") || args[0].equalsIgnoreCase("experience"))
                tabs = Arrays.asList("give", "set", "take");
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("resetPlayer"))
                tabs = Arrays.asList("true", "false");
            else if (args[0].equalsIgnoreCase("exp") || args[0].equalsIgnoreCase("experience"))
                tabs = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
        }

        List<String> available = new ArrayList<>();
        for (String tab: tabs) {
            String arg = args[args.length - 1];
            if (tab.toLowerCase().startsWith(arg.toLowerCase())) {
                available.add(tab);
            }
        }
        return available;
    }
}
