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

public class RankManager {
    private static final Predicate<String> NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9]*$").asPredicate();

    private static RankManager instance = null;
    public static RankManager getInstance() {
        return instance;
    }

    public final File folder = new File(AGMRanks.getInstance().getDataFolder(), "ranks");
    private final Map<String, RankingInstance> instances = new HashMap<>();

    public RankManager() {
        RankManager.instance = this;
    }

    public void loadRanks() {
        final Messenger messenger = AGMRanks.getMessenger();
        Arrays.stream(folder.listFiles((file) -> !file.isFile() && NAME_PATTERN.test(file.getParent()))).map(File::getName).forEach(file -> {
            try {
                messenger.debug("Ranks", String.format("Loading '§6%s§r'...", file));
                instances.put(file, new RankingInstance(file));
                messenger.debug("Ranks", String.format("'§6%s§r' loaded with %d ranks in it.", file, instances.get(file).getRanks().size()));
            } catch (AssertionError error) {
                messenger.warning(
                        String.format("While loading '%s' got following error:", file),
                        error.getMessage()
                );
            } catch (Throwable throwable) {
                messenger.handleException(String.format("Loading instance '%s' caused an exception", file), throwable);
            }
        });
    }

    public Map<String, RankingInstance> getRankingInstances() {
        return ImmutableMap.copyOf(instances);
    }
    public RankingInstance getRankingInstance(String name) {
        return instances.getOrDefault(name, null);
    }
}