package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.commands.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

public class RankingInstance {
    private final List<CommandExecutor> commands = new ArrayList<>();

    public void executeCommands(Player player) {
        commands.forEach(command -> command.execute(player));
    }

    public String getFolderName() {
        return null;
    }

    public void updateWith(Class<? extends Event> event) {
    }
}
