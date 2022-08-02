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
import me.ashenguard.exceptions.AdvancedCommandException;
import org.bukkit.command.CommandSender;

public class AGMRanksCommand extends AGMCommand {
    public static void register(SpigotPlugin plugin) {
        new AGMRanksCommand(plugin).register();
    }

    public AGMRanksCommand(SpigotPlugin plugin) {
        super(plugin, "agmranks");
    }

    @AGMCommandHandler
    public void run(CommandSender sender) {
        // Todo Admin GUI
        AGMRanks.getMessenger().response(sender, "§cAdministrator GUI is a WIP, For now use the subcommands.");
    }

    @AGMSubcommandHandler("reload")
    public void reload(CommandSender sender) {
        AGMRanks.getInstance().reload();
    }

    @AGMSubcommandHandler("create batch")
    public void createNormalBatch(CommandSender sender, String id, String name) {
        AGMCommandException.check(this, RankManager.NAME_PATTERN.test(id), Messages.InvalidBatchNameError);
        AGMCommandException.check(this, RankManager.getInstance().getRankBatch(id) == null, Messages.BatchExistsError);

        RankBatch.create(id, name, 0);

        plugin.messenger.response(sender, Messages.RankBatchCreatedNormal);
    }

    @AGMSubcommandHandler("create rank")
    public void run(CommandSender sender, String[] args) {
        AdvancedCommandException.check(args.length > 0 && RankManager.getInstance().getRankBatch(args[0]) != null, "§cSelect a batch to add new rank to");

        RankBatch batch = RankManager.getInstance().getRankBatch(args[0]);
        batch.createRank();

        AGMRanks.getMessenger().response(sender, "A new rank has been added to the batch");
    }
}