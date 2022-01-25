package me.ashenguard.agmranks.ranks.systems;

import me.ashenguard.agmranks.users.User;
import org.bukkit.Bukkit;

import java.util.List;

public class ExperienceSystem extends RankingSystem {
    private static final List<String> names = List.of("EXP", "Experience");
    public static boolean isType(String type) {
        for (String name: names) if (name.equalsIgnoreCase(type)) return true;
        return false;
    }

    @Override public String getName() {
        return "Experience";
    }

    private final boolean useExperience;
    private final int taskTimer;
    private final boolean auto;

    public ExperienceSystem() {
        this.useExperience = config.getBoolean("RankingSystem.UseExperienceOnRanking", true);
        this.auto = config.getBoolean("RankingSystem.AutoRankup", true);
        this.taskTimer = config.getInt("RankingSystem.AutoRankupTaskTimer", 0) * 20;
    }

    @Override public void onEnable() {
        if (auto && taskTimer > 0) Bukkit.getScheduler().runTaskTimer(plugin, new AutoRankingTask(), taskTimer, taskTimer);
        messenger.Debug("Ranks", "Experience system has been activated.", "UseExperience= §6" + (useExperience ? "Enabled" : "Disabled"), "AutoRankup= §6" + (auto ? "Enabled" : "Disabled"), "TaskTimer= §6" + (taskTimer/20));
    }

    @Override public double getScore(User user) {
        return user.getExperience();
    }

    @Override
    public PaymentResponse payCost(User user, double amount) {
        if (getScore(user) < amount) return new PaymentResponse(amount, false, "Insufficient Balance");
        if (!useExperience) return PaymentResponse.ALREADY_PAID;
        user.setExperience(user.getExperience() - amount);
        return new PaymentResponse(amount, true, null);
    }
}
