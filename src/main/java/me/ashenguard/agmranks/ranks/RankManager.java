package me.ashenguard.agmranks.ranks;

import com.google.common.collect.ImmutableMap;
import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.api.messenger.Messenger;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class RankManager {
    public static final Predicate<String> NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9]*$").asPredicate();
    public static final Predicate<String> TRACKED_NAME_PATTERN = Pattern.compile("^track_[a-z0-9]+_[a-z][a-z0-9]+$").asPredicate();

    private static RankManager instance = null;
    public static RankManager getInstance() {
        return instance;
    }
    public static Map<String, RankBatch> getBatches() {
        return getInstance().getRankBatches();
    }
    public static RankBatch getBatch(String name) {
        return getInstance().getRankBatch(name);
    }

    private final File folder = new File(AGMRanks.getInstance().getDataFolder(), "ranks");
    private final Map<String, RankBatch> batches = new HashMap<>();

    public RankManager() {
        RankManager.instance = this;
    }

    public void loadRanks() {
        final Messenger messenger = AGMRanks.getMessenger();
        final File[] files = folder.listFiles(file -> !file.isFile() && NAME_PATTERN.test(file.getName()));

        if (files == null || files.length == 0) {
            messenger.info("There are no active batch to be loaded");
            return;
        }
        Arrays.stream(files).map(File::getName).forEach(file -> {
            try {
                messenger.debug("Ranks", String.format("Loading batch '§6%s§r'...", file));
                batches.put(file, new RankBatch(file));
                messenger.debug("Ranks", String.format("Batch '§6%s§r' loaded with %d ranks in it.", file, batches.get(file).getRanks().size()));
            } catch (Throwable throwable) {
                messenger.handleException(String.format("Loading batch '%s' caused an exception", file), throwable);
            }
        });
    }

    public Map<String, RankBatch> getRankBatches() {
        return ImmutableMap.copyOf(batches);
    }
    public RankBatch getRankBatch(String name) {
        return batches.getOrDefault(name, null);
    }
}