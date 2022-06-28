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

@SuppressWarnings("unused")
public class RankBatch {
    private final String id;

    private final String name;
    private final Track track;
    private final String permission;

    private final List<CommandExecutor> commands = new ArrayList<>();
    private final List<Rank> ranks = new ArrayList<>();

    private final int prestiges;
    private final int multiplier;

    private final String SCORE_PDC_KEY, RANK_PDC_KEY, HIGHRANK_PDC_KEY;

    private static Configuration loadConfig(String filename, String name, String track) {
        List<Placeholder> placeholders = Arrays.asList(
                new Placeholder("TRACK", (p, s) -> track == null ? "''" : track),
                new Placeholder("NAME", (p, s) -> name)
        );
        return new Configuration(
                AGMRanks.getInstance(),
                String.format("ranks/%s/config.yml", filename),
                AGMRanks.getInstance().getResource("templates/batch-template.yml"),
                string -> {
                    for (Placeholder placeholder : placeholders) string = placeholder.apply(string, null);
                    return string;
                }
        );
    }

    public RankBatch(String id) {
        this(id, id, null);
    }

    public RankBatch(String filename, String name, String track) {
        Configuration config = loadConfig(filename, name, track);
        File folder = getFolder();
        if (!folder.exists()) folder.mkdirs();

        this.id = filename;
        this.name = config.getString("Name", filename);
        this.track = LuckPermsAPI.getTrack(config.getString("Track", ""));
        this.permission = config.getString("Permission", "");

        this.prestiges = config.getInt("PrestigeLimit", 0);
        this.multiplier = config.getInt("PrestigeMultiplier", 1);

        config.getStringList("Commands").stream().map(CommandExecutor::new).forEach(commands::add);

        int id = 0;
        while (new File(folder, Ordinal.to(id + 1) + ".yml").exists()) ranks.add(new Rank(this, id++, this.track));

        SCORE_PDC_KEY = String.format("%s_score", filename);
        RANK_PDC_KEY = String.format("%s_rank", filename);
        HIGHRANK_PDC_KEY = String.format("%s_highrank", filename);

        Bukkit.getScheduler().runTaskTimer(AGMRanks.getInstance(), this::autoRankup, 0, 600);
    }

    private void autoRankup() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!this.hasPermission(player)) return;
            Rank rank = getPlayerRank(player);
            while ((rank = rank.getNext()) != null) {
                if (rank.hasCost()) return;
                if (rank.areRequirementsMet(player)) rank.rankup(player);
            }
        });
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

    public Rank getRank(int id) {
        return 0 <= id && id < ranks.size() ? ranks.get(id) : null;
    }

    public Track getTrack() {
        return track;
    }

    public boolean hasPermission(Player player) {
        return permission != null && permission.length() > 0 && player.hasPermission(permission);
    }

    public String getID() {
        return id;
    }

    public File getFolder() {
        return new File(AGMRanks.getInstance().getDataFolder(), String.format("ranks/%s", id));
    }

    public <T extends Event> void updateWith(final Class<T> cls) {
        RankBatch batch = this;
        class EventListener implements Listener {
            private final boolean playerEvent;

            public EventListener() {
                this.playerEvent = PlayerEvent.class.isAssignableFrom(cls);

                assert playerEvent || EntityEvent.class.isAssignableFrom(cls) : String.format("%s is not assignable from PlayerEvent or EntityEvent", cls.getSimpleName());
            }

            @EventHandler
            public void onEvent(T event) {
                if (playerEvent && event instanceof PlayerEvent)
                    batch.update(((PlayerEvent) event).getPlayer());
                else if (event instanceof EntityEvent && ((EntityEvent) event).getEntity() instanceof Player)
                    batch.update((Player) ((EntityEvent) event).getEntity());
            }
        }
        Bukkit.getPluginManager().registerEvents(new EventListener(), AGMRanks.getInstance()
        );
    }

    public void update(Player player) {
        Rank current = getPlayerRank(player);
        for (Rank rank: ranks) {
            if (rank.getID() > current.getID()) break;
            if (rank.areRequirementsMet(player)) continue;

            Rank reset = rank.getPrevious();
            setPlayerRank(player, reset);
            AGMRanks.getMessenger().response(player, AGMRanks.getTranslations().get("RankReset"));
            break;
        }
    }

    public long getPlayerScore(Player player) {
        return Utils.getPlayerData(player, SCORE_PDC_KEY, PersistentDataType.LONG, (long) 0);
    }

    public void setPlayerScore(Player player, long score) {
        Utils.setPlayerData(player, SCORE_PDC_KEY, PersistentDataType.LONG, score);
    }

    public Rank getPlayerRank(Player player) {
        int id = Utils.getPlayerData(player, RANK_PDC_KEY, PersistentDataType.INTEGER, -1);
        if (0 > id || id >= ranks.size()) return null;
        return ranks.get(id);
    }

    public void setPlayerRank(Player player, Rank rank) {
        if (rank.getBatch() != this) return;
        Utils.setPlayerData(player, RANK_PDC_KEY, PersistentDataType.INTEGER, rank.getID());
    }

    public Rank getPlayerHighRank(Player player) {
        int id = Utils.getPlayerData(player, HIGHRANK_PDC_KEY, PersistentDataType.INTEGER, -1);
        if (0 > id || id >= ranks.size()) return null;
        return ranks.get(id);
    }

    public void setPlayerHighRank(Player player, Rank rank, boolean overwrite) {
        if (rank.getBatch() != this) return;
        Utils.setPlayerData(player, RANK_PDC_KEY, PersistentDataType.INTEGER, rank.getID(), overwrite ? null : integer -> integer != null && integer < rank.getID());
    }

    public int getPlayerPrestige(Player player) {
        return Utils.getPlayerData(player, SCORE_PDC_KEY, PersistentDataType.INTEGER, 0);
    }

    public void setPlayerPrestige(Player player, int prestige) {
        Utils.setPlayerData(player, SCORE_PDC_KEY, PersistentDataType.INTEGER, prestige);
    }
}
