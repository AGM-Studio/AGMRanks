package me.ashenguard.agmranks;

import me.ashenguard.api.messenger.Message;
import me.ashenguard.api.spigot.SpigotPlugin;

public enum Messages implements Message {
    // Errors
    InvalidBatchNameError("§cPlease specify a valid id for this instance, A valid id only contains a-z and 0-9, starting with a-z."),
    BatchExistsError("§cThis batch already exists."),
    BatchNotFoundError("§cBatch not found."),

    PlayerNotFoundError("§cPlayer not found."),
    // Rank Messages
    RankBatchCreated("The batch with the default rank has been created."),
    RankedUp("You have been ranked up.");

    private final String path;
    private final String value;

    Messages(String path, String value) {
        this.path = path;
        this.value = value;
    }

    Messages(String value) {
        this.path = name();
        this.value = value;
    }

    @Override
    public String getMessage(SpigotPlugin plugin) {
        String message = plugin.translation.get(getPath(), null);
        return message != null ? message : getDefault();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getDefault() {
        return value;
    }
}
