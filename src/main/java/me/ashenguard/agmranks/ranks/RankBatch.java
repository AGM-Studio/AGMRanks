package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.commands.CommandReward;
import me.ashenguard.agmranks.player.PlayerBatchInfo;
import me.ashenguard.agmranks.player.RankedPlayer;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Ordinal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class RankBatch {
    private final String id;
    private final int sort;

    private final String name;
    private final String permission;

    private final List<CommandReward> commands = new ArrayList<>();
    private final List<Rank> ranks = new ArrayList<>();

    private final PlaceholderItemStack icon;

    private final int prestiges;
    private final int multiplier;

    private final String SCORE_PDC_KEY, RANK_PDC_KEY, HIGHRANK_PDC_KEY, PRESTIGE_PDC_KEY;

    private static Configuration getConfig(String filename, String name) {
        return new Configuration(AGMRanks.getInstance(), String.format("ranks/%s/config.yml", filename), "templates/batch-config.yml", new Placeholder("NAME", (p, s) -> name));
    }

    public static RankBatch from(File folder) {
        return new RankBatch(folder);
    }

    private RankBatch(File folder) {
        this.id = folder.getName();

        String name = (id.substring(0, 1).toUpperCase() + id.substring(1).toLowerCase()).replace("_", " ");
        Configuration config = getConfig(this.id, name);

        this.sort = config.getInt("Sorting", 0);
        this.name = config.getString("Name", name);
        this.permission = config.getString("Permission", null);

        PlaceholderItemStack icon = PlaceholderItemStack.fromSection(config.getConfigurationSection("Icon"));
        this.icon = icon != null ? icon : PlaceholderItemStack.nullItem();

        this.prestiges = config.getInt("PrestigeLimit", 0);
        this.multiplier = config.getInt("PrestigeMultiplier", 1);

        config.getStringList("Commands").forEach(cmd -> commands.add(new CommandReward(this, cmd)));

        int i = 0;
        while (new File(folder,Ordinal.to(i + 1) + ".yml").exists()) ranks.add(new Rank(this, i++));

        RANK_PDC_KEY = String.format("ranking_%s_rank", this.id);
        SCORE_PDC_KEY = String.format("ranking_%s_score", this.id);
        HIGHRANK_PDC_KEY = String.format("ranking_%s_highrank", this.id);
        PRESTIGE_PDC_KEY = String.format("ranking_%s_prestige", this.id);
    }

    public static RankBatch create(String id, String name) {
        File folder = new File(AGMRanks.getInstance().getDataFolder(), String.format("ranks/%s", id));

        RankBatch batch = new RankBatch(folder);
        if (batch.ranks.size() == 0) batch.createRank();

        return batch;
    }

    public Rank createRank() {
        new Configuration(
                AGMRanks.getInstance(),
                String.format("ranks/%s/%s.yml", this.id, Ordinal.to(ranks.size() + 1)),
                "templates/rank-template.yml",
                new Placeholder("NAME", (p, s) -> String.format("&6%s &7Rank", Ordinal.to(ranks.size() + 1)))
        );

        Rank rank = new Rank(this, ranks.size());
        ranks.add(rank);
        return rank;
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

    public boolean hasPermission(Player player) {
        return permission == null || permission.length() == 0 || player.hasPermission(permission);
    }

    public String getID() {
        return id;
    }

    public File getFolder() {
        return new File(AGMRanks.getInstance().getDataFolder(), String.format("ranks/%s", id));
    }

    public int getSortingKey() {
        return sort;
    }

    public PlaceholderItemStack getIcon() {
        return icon;
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
        PlayerBatchInfo info = RankedPlayer.get(player).getBatchInfo(this);

        Rank current = info.getRank();
        for (Rank rank: ranks) {
            if (rank.getID() > current.getID()) break;
            if (rank.areRequirementsMet(player)) continue;

            Rank reset = rank.getPrevious();
            info.setRank(reset);
            AGMRanks.getMessenger().response(player, AGMRanks.getTranslations().get("RankReset"));
            break;
        }
    }

    public void autoRankup() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            PlayerBatchInfo info = RankedPlayer.get(player).getBatchInfo(this);
            if (info == null) return;

            Rank rank = info.getRank();
            if (rank == null) {
                rank = this.getRank(0);
                info.setRank(rank);
            }
            while ((rank = rank.getNext()) != null) {
                if (rank.hasCost()) return;
                if (rank.areRequirementsMet(player)) rank.rankup(player);
            }
        });
    }

    public final String getScorePDCKey() {
        return SCORE_PDC_KEY;
    }
    public final String getRankPDCKey() {
        return RANK_PDC_KEY;
    }
    public final String getHighRankPDCKey() {
        return HIGHRANK_PDC_KEY;
    }
    public final String getPrestigePDCKey() {
        return PRESTIGE_PDC_KEY;
    }

    public PlayerBatchInfo getPlayerInfo(Player player) {
        return RankedPlayer.get(player).getBatchInfo(this);
    }
}
