package me.ashenguard.agmranks.ranks.systems;

import me.ashenguard.agmranks.users.User;
import org.bukkit.Bukkit;

import java.util.List;

public class PlaytimeSystem extends RankingSystem {
    private static final List<String> names = List.of("Playtime");
    public static boolean isType(String type) {
        for (String name: names) if (name.equalsIgnoreCase(type)) return true;
        return false;
    }

    @Override public String getName() {
        return "Playtime";
    }

    private final int taskTimer;
    private final boolean auto;

    public PlaytimeSystem() {
        this.auto = config.getBoolean("RankingSystem.AutoRankup", true);
        this.taskTimer = config.getInt("RankingSystem.AutoRankupTaskTimer", 0) * 20;
    }

    @Override public void onEnable() {
        if (auto && taskTimer > 0) Bukkit.getScheduler().runTaskTimer(plugin, new AutoRankingTask(), taskTimer, taskTimer);
        messenger.Debug("Ranks", "Playtime system has been activated.", "AutoRankup= §6" + (auto ? "Enabled" : "Disabled"), "TaskTimer= §6" + (taskTimer/20));
    }

    @Override public double getScore(User user) {
        return user.getPlaytime();
    }

    @Override public PaymentResponse payCost(User user, double amount) {
        if (getScore(user) < amount) return new PaymentResponse(amount, false, "Insufficient Balance");
        return PaymentResponse.ALREADY_PAID;
    }
}
