package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.api.LuckPermsAPI;
import me.ashenguard.agmranks.commands.CommandExecutor;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Ordinal;
import net.luckperms.api.track.Track;
import org.apache.commons.lang.IllegalClassException;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RankingInstance {
    private final String filename;

    private final String name;
    private final Track track;

    private final List<CommandExecutor> commands = new ArrayList<>();
    private final List<Rank> ranks = new ArrayList<>();

    private final int prestiges;
    private final int multiplier;

    private static Configuration loadConfig(String filename, String name, String track) {
        List<Placeholder> placeholders = Arrays.asList(
                new Placeholder("TRACK", (p, s) -> track == null ? "''" : track),
                new Placeholder("NAME", (p, s) -> name)
        );
        return new Configuration(
                AGMRanks.getInstance(),
                String.format("ranks/%s/config.yml", filename),
                AGMRanks.getInstance().getResource("templates/instance-template.yml"),
                string -> {
                    for (Placeholder placeholder: placeholders) string = placeholder.apply(string, null);
                    return string;
                }
        );
    }


    public RankingInstance(String filename, String name, String track) {
        Configuration config = loadConfig(filename, name, track);
        File folder = getFolder();
        if (!folder.exists()) folder.mkdirs();

        this.name = name;
        this.filename = filename;
        this.track = LuckPermsAPI.getTrack(track);

        this.prestiges = config.getInt("PrestigeLimit", 0);
        this.multiplier = config.getInt("PrestigeMultiplier", 1);

        config.getStringList("Commands").stream().map(CommandExecutor::new).forEach(commands::add);

        int id = 1;
        while (new File(folder, Ordinal.to(id) + ".yml").exists()) ranks.add(new Rank(this, id++, this.track));
    }

    public void executeCommands(Player player) {
        commands.forEach(command -> command.execute(player));
    }

    public String getName() {
        return name;
    }

    public List<Rank> getRanks() {
        return ranks;
    }

    public Track getTrack() {
        return track;
    }

    public String getFilename() {
        return filename;
    }

    public File getFolder() {
        return new File(AGMRanks.getInstance().getDataFolder(), String.format("ranks/%s", filename));
    }

    public void updateWith(Class<? extends Event> event) {
        Bukkit.getPluginManager().registerEvents(new EventListener<>(event, this), AGMRanks.getInstance());
    }

    public void update(Player player) {

    }

    public Rank getHighRank(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AGMRanks.getInstance(), String.format("%s_high_rank", filename));

        Integer id = container.get(key, PersistentDataType.INTEGER);
        if (id == null) return null;
        return ranks.get(id - 1);
    }

    public long getScore(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AGMRanks.getInstance(), String.format("%s_score", filename));

        Long score = container.get(key, PersistentDataType.LONG);
        return score == null ? 0 : score;
    }

    public Rank getRank(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AGMRanks.getInstance(), String.format("%s_rank", filename));

        Integer id = container.get(key, PersistentDataType.INTEGER);
        if (id == null) return null;
        return ranks.get(id - 1);
    }

    public void setHighRank(Player player, Rank rank, boolean overwrite) {
        if (rank.getRankingInstance() != this) return;

        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AGMRanks.getInstance(), String.format("%s_high_rank", filename));
        if (overwrite) container.set(key, PersistentDataType.INTEGER, rank.getId());
        else {
            Integer current = container.get(key, PersistentDataType.INTEGER);
            if (current != null && current < rank.getId()) container.set(key, PersistentDataType.INTEGER, rank.getId());
        }
    }

    public void setRank(Player player, Rank rank) {
        if (rank.getRankingInstance() != this) return;

        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AGMRanks.getInstance(), String.format("%s_rank", filename));

        container.set(key, PersistentDataType.INTEGER, rank.getId());
    }

    static class EventListener<T extends Event> implements Listener {
        private final boolean playerEvent;
        private final RankingInstance instance;

        public EventListener(Class<T> cls, RankingInstance instance) {
            this.playerEvent = PlayerEvent.class.isAssignableFrom(cls);
            if (!playerEvent && EntityEvent.class.isAssignableFrom(cls)) throw new IllegalClassException(String.format("%s is not assignable from PlayerEvent or EntityEvent", cls.getSimpleName()));
            this.instance = instance;
        }

        @EventHandler
        public void onEvent(T event) {
            if (playerEvent && event instanceof PlayerEvent)
                instance.update(((PlayerEvent) event).getPlayer());
            else if (event instanceof EntityEvent && ((EntityEvent) event).getEntity() instanceof Player)
                instance.update((Player) ((EntityEvent) event).getEntity());
        }
    }
}
