package me.ashenguard.agmranks.commands;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.gui.RankUpGUI;
import me.ashenguard.api.AdvancedCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandRanks extends AdvancedCommand {
    public CommandRanks() {
        super(AGMRanks.getInstance(), "Ranks", true);
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        RankUpGUI rankUpGUI = new RankUpGUI(player);
        rankUpGUI.show();
    }

    @Override
    public List<String> tabs(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}
