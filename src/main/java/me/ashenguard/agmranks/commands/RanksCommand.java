package me.ashenguard.agmranks.commands;

import me.ashenguard.agmranks.gui.BatchGUI;
import me.ashenguard.agmranks.gui.BatchMenuGUI;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.api.commands.AGMCommand;
import me.ashenguard.api.commands.annotations.AGMCommandHandler;
import me.ashenguard.api.spigot.SpigotPlugin;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RanksCommand extends AGMCommand {
    public static void register(SpigotPlugin plugin) {
        new RanksCommand(plugin).register();
    }

    protected RanksCommand(SpigotPlugin plugin) {
        super(plugin, "ranks");
    }

    @AGMCommandHandler
    public void run(Player player, String batchName) {
        RankBatch[] batches = RankManager.getInstance().getRankBatches().values().toArray(RankBatch[]::new);
        RankBatch match = Arrays.stream(batches).filter(b -> b.getID().equalsIgnoreCase(batchName)).findFirst().orElse(null);

        if (match != null) BatchGUI.show(player, match);
        else if (batches.length == 1) BatchGUI.show(player, batches[0]);
        else BatchMenuGUI.show(player);
    }
}
