package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.Utils;
import me.ashenguard.agmranks.api.LuckPermsAPI;
import me.ashenguard.agmranks.commands.CommandExecutor;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Ordinal;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
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

    public final String SCORE_PDC_KEY, RANK_PDC_KEY, HIGHRANK_PDC_KEY;

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
                    for (Placeholder placeholder : placeholders) string = placeholder.apply(string, null);
                    return string;
                }
        );
    }

    public RankingInstance(String filename) {
        this(filename, filename, null);
    }

    public RankingInstance(String filename, String name, String track) {
        Configuration config = loadConfig(filename, name, track);
        File folder = getFolder();
        if (!folder.exists()) folder.mkdirs();

        this.filename = filename;
        this.name = config.getString("Name", filename);
        this.track = LuckPermsAPI.getTrack(config.getString("Track", ""));

        this.prestiges = config.getInt("PrestigeLimit", 0);
        this.multiplier = config.getInt("PrestigeMultiplier", 1);

        config.getStringList("Commands").stream().map(CommandExecutor::new).forEach(commands::add);

        int id = 1;
        while (new File(folder, Ordinal.to(id) + ".yml").exists()) ranks.add(new Rank(this, id++, this.track));

        SCORE_PDC_KEY = String.format("%s_score", filename);
        RANK_PDC_KEY = String.format("%s_rank", filename);
        HIGHRANK_PDC_KEY = String.format("%s_highrank", filename);
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

    public <T extends Event> void updateWith(final Class<T> cls) {
        RankingInstance instance = this;
        class EventListener implements Listener {
            private final boolean playerEvent;

            public EventListener() {
                this.playerEvent = PlayerEvent.class.isAssignableFrom(cls);

                assert playerEvent || EntityEvent.class.isAssignableFrom(cls) : String.format("%s is not assignable from PlayerEvent or EntityEvent", cls.getSimpleName());
            }

            @EventHandler
            public void onEvent(T event) {
                if (playerEvent && event instanceof PlayerEvent)
                    instance.update(((PlayerEvent) event).getPlayer());
                else if (event instanceof EntityEvent && ((EntityEvent) event).getEntity() instanceof Player)
                    instance.update((Player) ((EntityEvent) event).getEntity());
            }
        }
        Bukkit.getPluginManager().registerEvents(new EventListener(), AGMRanks.getInstance()
        );
    }

    public void update(Player player) {

    }

    public long getScore(Player player) {
        return Utils.getPlayerData(player, SCORE_PDC_KEY, PersistentDataType.LONG, (long) 0);
    }

    public void setScore(Player player, long score) {
        Utils.setPlayerData(player, SCORE_PDC_KEY, PersistentDataType.LONG, score);
    }

    public Rank getRank(Player player) {
        int id = Utils.getPlayerData(player, RANK_PDC_KEY, PersistentDataType.INTEGER, -1);
        if (0 > id || id >= ranks.size()) return null;
        return ranks.get(id);
    }

    public void setRank(Player player, Rank rank) {
        if (rank.getRankingInstance() != this) return;
        Utils.setPlayerData(player, RANK_PDC_KEY, PersistentDataType.INTEGER, rank.getId());
    }

    public Rank getHighRank(Player player) {
        int id = Utils.getPlayerData(player, HIGHRANK_PDC_KEY, PersistentDataType.INTEGER, -1);
        if (0 > id || id >= ranks.size()) return null;
        return ranks.get(id);
    }

    public void setHighRank(Player player, Rank rank, boolean overwrite) {
        if (rank.getRankingInstance() != this) return;
        Utils.setPlayerData(player, RANK_PDC_KEY, PersistentDataType.INTEGER, rank.getId(), overwrite ? null : integer -> integer != null && integer < rank.getId());
    }

    public int getPrestige(Player player) {
        return Utils.getPlayerData(player, SCORE_PDC_KEY, PersistentDataType.INTEGER, 0);
    }

    public void setPrestige(Player player, int prestige) {
        Utils.setPlayerData(player, SCORE_PDC_KEY, PersistentDataType.INTEGER, prestige);
    }
}
