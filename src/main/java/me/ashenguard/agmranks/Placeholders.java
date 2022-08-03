package me.ashenguard.agmranks;
 /*
import me.ashenguard.agmranks.ranks.OldRank;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.agmranks.ranks.systems.RankingSystem;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.placeholder.PHExtension;
import me.ashenguard.api.placeholder.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnusedReturnValue")
public class Placeholders extends PHExtension {
    private final AGMRanks plugin = AGMRanks.getInstance();
    private final RankManager manager = plugin.rankManager;
    private final RankingSystem system = manager.rankingSystem;

    private final Function<OldRank, String> getCost = (rank -> {
        if (rank == null) return "-";
        return String.valueOf(rank.cost);
    });
    private final BiFunction<User, OldRank, String> getTotalCost = ((user, rank) -> {
        if (rank == null) return "-";
        return String.valueOf(system.getCost(user, rank));
    });
    private final Function<OldRank, String> getOrdinal = (rank -> {
        if (rank == null) return "-";
        return rank.getOrdinal();
    });
    private final BiFunction<User, OldRank, String> getName = ((user, rank) -> {
        if (rank == null) return "-";
        return rank.getTranslatedName(user.player);
    });

    @Override
    public @NotNull String getIdentifier() {
        return "AGMRanks";
    }

    public Placeholders(){
        super(AGMRanks.getInstance());

        UserPlaceholder("Rank", (user, s) -> getOrdinal.apply(user.getRank()));
        UserPlaceholder("RankName", ((user, s) -> getName.apply(user, user.getRank())));

        UserPlaceholderEndedIn("_Rank", ((user, s) -> getOrdinal.apply(getRank(user, s))));
        UserPlaceholderEndedIn("_RankName", ((user, s) -> getName.apply(user, getRank(user, s))));
        UserPlaceholderEndedIn("_RankCost", ((user, s) -> getCost.apply(getRank(user, s))));
        UserPlaceholderEndedIn("_RankTotalCost", ((user, s) -> getTotalCost.apply(user, getRank(user, s))));

        UserPlaceholder("Playtime", ((user, s) -> String.valueOf(user.getPlaytime())));
        UserPlaceholder("Playtime_Minutes", ((user, s) -> String.valueOf(user.getPlaytime() % 60)));
        UserPlaceholder("Playtime_Hours", ((user, s) -> String.valueOf(TimeUnit.HOURS.convert(user.getPlaytime(), TimeUnit.MINUTES) % 24)));
        UserPlaceholder("Playtime_Days", ((user, s) -> String.valueOf(TimeUnit.DAYS.convert(user.getPlaytime(), TimeUnit.MINUTES))));

        UserPlaceholder("Experience", ((user, s) -> String.valueOf(user.getExperience())));
    }

    private OldRank getRank(User user, String value) {
        if (value.equalsIgnoreCase("Next") && user.getRank().hasNextRank()) return user.getRank().getNext();
        if (value.equalsIgnoreCase("Max")) return user.getBestAvailableRank();

        Matcher match = Pattern.compile("\\d+").matcher(value);
        if (match.find()) return manager.getRank(Integer.parseInt(match.group()));
        return null;
    }

    private Placeholder UserPlaceholder(String identifier, BiFunction<User, String, String> function) {
        return new Placeholder(this, identifier, ((player, name) -> {
            User user = UserManager.getUser(player);
            return function.apply(user, name);
        }));
    }
    private Placeholder UserPlaceholder(Predicate<String> validator, Function<String, String> extractor, BiFunction<User, String, String> function) {
        return new Placeholder(this, validator, extractor, ((player, name) -> {
            User user = UserManager.getUser(player);
            return function.apply(user, name);
        }));
    }
    private Placeholder UserPlaceholderEndedIn(String ended, BiFunction<User, String, String> function) {
        return UserPlaceholder((identifier) -> identifier.endsWith(ended), (identifier) -> identifier.substring(0, identifier.length() - ended.length()), function);
    }
}
*/