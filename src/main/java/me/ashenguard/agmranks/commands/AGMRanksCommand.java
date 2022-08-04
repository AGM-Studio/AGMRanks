package me.ashenguard.agmranks.commands;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.Messages;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.api.commands.AGMCommand;
import me.ashenguard.api.commands.AGMCommandException;
import me.ashenguard.api.commands.annotations.AGMCommandHandler;
import me.ashenguard.api.commands.annotations.AGMSubcommandHandler;
import me.ashenguard.api.spigot.SpigotPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AGMRanksCommand extends AGMCommand {
    public static void register(SpigotPlugin plugin) {
        new AGMRanksCommand(plugin).register();
    }

    public AGMRanksCommand(SpigotPlugin plugin) {
        super(plugin, "agmranks");
    }

    @AGMCommandHandler
    public void command(CommandSender sender) {
        // Todo Admin GUI
        AGMRanks.getMessenger().response(sender, "§cAdministrator GUI is a WIP, For now use the subcommands.");
    }

    @AGMSubcommandHandler("reload")
    public void reload(CommandSender sender) {
        AGMRanks.getInstance().reload();
    }

    @AGMSubcommandHandler("create batch")
    public void createBatch(CommandSender sender, String id, String name) {
        AGMCommandException.check(this, RankManager.NAME_PATTERN.test(id), Messages.InvalidBatchNameError);
        AGMCommandException.check(this, RankManager.getInstance().getRankBatch(id) == null, Messages.BatchExistsError);

        RankBatch.create(id, name, 0);

        plugin.messenger.response(sender, Messages.RankBatchCreated);
    }

    @AGMSubcommandHandler("create rank")
    public void createRank(CommandSender sender, String batch) {
        AGMCommandException.check(this, RankManager.getBatch(batch) != null, Messages.BatchNotFoundError);

        RankManager.getBatch(batch).createRank();

        AGMRanks.getMessenger().response(sender, "A new rank has been added to the batch");
    }

    @AGMSubcommandHandler("reset")
    public void reset(CommandSender sender, Player player, String batch, boolean reset) {
        AGMCommandException.check(this, player != null, Messages.PlayerNotFoundError);
        AGMCommandException.check(this, RankManager.getBatch(batch) != null, Messages.BatchNotFoundError);

        RankBatch match = RankManager.getBatch(batch);

        match.setPlayerPrestige(player, 0);
        match.setPlayerRank(player, match.getRank(0));
        match.setPlayerScore(player, 0);
        if (reset) match.setPlayerHighRank(player, match.getRank(0), true);

        AGMRanks.getMessenger().response(sender, "Player ranking has been reset to 1st rank.");
    }
}