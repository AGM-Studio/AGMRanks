package me.ashenguard.agmranks;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class Utils {
    public static <T, Z> Z getPlayerData(Player player, String name, PersistentDataType<T, Z> type) {
        return getPlayerData(player, name, type, null);
    }
    public static <T, Z> Z getPlayerData(Player player, String name, PersistentDataType<T, Z> type, Z def) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AGMRanks.getInstance(), name);

        @Nullable Z result = container.get(key, type);
        return result != null ? result : def;
    }

    public static <T, Z> boolean setPlayerData(Player player, String name, PersistentDataType<T, Z> type, Z value) {
        return setPlayerData(player, name, type, value, null);
    }
    public static <T, Z> boolean setPlayerData(Player player, String name, PersistentDataType<T, Z> type, Z value, Predicate<Z> validate) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(AGMRanks.getInstance(), name);

        if (validate != null && !validate.test(getPlayerData(player, name, type))) return false;

        container.set(key, type, value);
        return true;
    }


}
