package me.ashenguard.agmranks;

import com.google.common.collect.ImmutableSet;
import me.ashenguard.agmranks.commands.AGMRanksCommand;
import me.ashenguard.agmranks.commands.RanksCommand;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.api.itemstack.ItemLibrary;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.placeholder.Translations;
import me.ashenguard.api.spigot.SpigotPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class AGMRanks extends SpigotPlugin {
    private static AGMRanks instance = null;

    public static AGMRanks getInstance() {
        return instance;
    }
    public static Messenger getMessenger() {
        return instance.messenger;
    }
    public static Translations getTranslations() {
        return instance.translation;
    }

    @Override
    public @NotNull List<String> getRequirements() {
        return Collections.singletonList("AGMCore");
    }

    @Override
    public int getBStatsID() {
        return 7704;
    }

    @Override
    public int getSpigotID() {
        return 75787;
    }

    @Override
    public void onPluginEnable() {
        instance = this;

        loadBatches();

        // if (PlaceholderManager.enable) new Placeholders().register();

        AGMRanksCommand.register(this);
        RanksCommand.register(this);

        ItemLibrary.createLibraryFile(this, "agmranks.yml", "GUI/items.yml");
    }

    public static final Predicate<String> BATCH_NAME_PATTERN = Pattern.compile("^[a-z][a-z\\d]*$").asPredicate();
    private static final Set<RankBatch> batches = new HashSet<>();

    public void loadBatches() {
        batches.clear();

        final File folder = new File(AGMRanks.getInstance().getDataFolder(), "ranks");
        final File[] files = folder.listFiles(file -> !file.isFile() && BATCH_NAME_PATTERN.test(file.getName()));

        if (files == null || files.length == 0) {
            messenger.info("There are no active batch to be loaded");
            return;
        }
        Arrays.stream(files).forEach(file -> {
            try {
                messenger.debug("Ranks", String.format("Loading batch '§6%s§r'...", file.getName()));
                RankBatch batch = RankBatch.from(file);
                messenger.debug("Ranks", String.format("Batch '§6%s§r' loaded with %d ranks in it.", file.getName(), batch.getRanks().size()));
                batches.add(batch);
            } catch (Throwable throwable) {
                messenger.handleException(String.format("Loading batch '%s' caused an exception", file.getName()), throwable);
            }
        });

        Bukkit.getScheduler().runTaskTimer(AGMRanks.getInstance(), () -> batches.forEach(RankBatch::autoRankup), 0, 600);
    }

    public static Set<RankBatch> getBatches() {
        return ImmutableSet.copyOf(batches);
    }
    public static RankBatch getBatch(String id) {
        return batches.stream().filter(batch -> batch.getID().equals(id)).findAny().orElse(null);
    }
}
