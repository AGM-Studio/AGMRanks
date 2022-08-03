package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.Utils;
import me.ashenguard.agmranks.commands.CommandReward;
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
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class RankBatch {
    private final String id;

    private final String name;
    private final String permission;

    private final List<CommandReward> commands = new ArrayList<>();
    private final List<Rank> ranks = new ArrayList<>();

    private final PlaceholderItemStack icon;

    private final int prestiges;
    private final int multiplier;

    private final String SCORE_PDC_KEY, RANK_PDC_KEY, HIGHRANK_PDC_KEY;

    public RankBatch(String id) {
        this(id, id);
    }

    public RankBatch(String filename, String name) {
        Configuration config = new Configuration(
                AGMRanks.getInstance(),
                String.format("ranks/%s/config.yml", filename),
                "templates/batch-template.yml",
                new Placeholder("NAME", (p, s) -> name)
        );

        this.id = filename;
        this.name = config.getString("Name", filename);
        this.permission = config.getString("Permission", null);

        PlaceholderItemStack icon = PlaceholderItemStack.fromSection(config.getConfigurationSection("Icon"));
        this.icon = icon != null ? icon : PlaceholderItemStack.nullItem();

        this.prestiges = config.getInt("PrestigeLimit", 0);
        this.multiplier = config.getInt("PrestigeMultiplier", 1);

        config.getStringList("Commands").forEach(cmd -> commands.add(new CommandReward(this, cmd)));

        int id = 0;
        File folder = getFolder();
        while (new File(folder,Ordinal.to(id + 1) + ".yml").exists()) ranks.add(new Rank(this, id++));

        SCORE_PDC_KEY = String.format("ranking_%s_score", filename);
        RANK_PDC_KEY = String.format("ranking_%s_rank", filename);
        HIGHRANK_PDC_KEY = String.format("ranking_%s_highrank", filename);

        Bukkit.getScheduler().runTaskTimer(AGMRanks.getInstance(), this::autoRankup, 0, 600);
    }

    public static RankBatch create(String id, String name, int sorting) {
        new Configuration(
                AGMRanks.getInstance(),
                String.format("ranks/%s/config.yml", id),
                "templates/batch-config.yml",
                new Placeholder("NAME", (p, s) -> name),
                new Placeholder("SORTING", (p, s) -> String.valueOf(sorting))
        );

        RankBatch batch = new RankBatch(id);
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
        return permission == null || permission.length() > 0 || player.hasPermission(permission);
    }

    public String getID() {
        return id;
    }

    public File getFolder() {
        return new File(AGMRanks.getInstance().getDataFolder(), String.format("ranks/%s", id));
    }

    public int getSortingKey() {
        return 0;
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

    private void autoRankup() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!this.hasPermission(player)) return;
            Rank rank = getPlayerRank(player);
            if (rank == null) {
                rank = this.getRank(0);
                setPlayerRank(player, rank);
            }
            while ((rank = rank.getNext()) != null) {
                if (rank.hasCost()) return;
                if (rank.areRequirementsMet(player)) rank.rankup(player);
            }
        });
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
