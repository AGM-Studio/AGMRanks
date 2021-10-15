package me.ashenguard.agmranks;

import me.ashenguard.api.messenger.Messenger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;

import static org.bukkit.Bukkit.getServer;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused", "FieldCanBeLocal"})
public class Vault {
    private final AGMRanks plugin = AGMRanks.getInstance();
    private final Messenger messenger = AGMRanks.getMessenger();

    private boolean economyEnabled = false;
    private boolean chatEnabled = false;
    private boolean permissionsEnabled = false;

    public boolean isEconomyEnabled() {
        return economyEnabled;
    }
    public boolean isChatEnabled() {
        return chatEnabled;
    }
    public boolean isPermissionsEnabled() {
        return permissionsEnabled;
    }

    private Permission permission = null;
    private Economy economy = null;
    private Chat chat = null;


    public Vault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            messenger.Warning("§6Vault§r wasn't found");
            return;
        }

        economyEnabled = setupEconomy();
        chatEnabled = setupChat();
        permissionsEnabled = setupPermissions();

        messenger.Debug("Vault", "Vault was hooked with this status: ",
                "Economy: §6" + (economyEnabled? "§aEnable": "§cDisable"),
                "Chat: §6" + (chatEnabled? "§aEnable": "§cDisable"),
                "Permission: §6" + (permissionsEnabled? "§aEnable": "§cDisable"));

        messenger.Info("§6Vault§r successfully hooked");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return true;
    }
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return false;
        chat = rsp.getProvider();
        return true;
    }
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return false;
        permission = rsp.getProvider();
        return true;
    }

    public void addPlayerGroup(OfflinePlayer player, String group) {
        permission.playerAddGroup(null, player, group);
        messenger.Debug("Vault", "Player added to a group", "Player= §6" + player.getName(), "Group= §6" + group);
    }

    public void removePlayerGroup(OfflinePlayer player, String group) {
        permission.playerRemoveGroup(null, player, group);
        messenger.Debug("Vault", "Player removed from a group", "Player= §6" + player.getName(), "Group= §6" + group);
    }

    public double getPlayerBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public void withdrawPlayerMoney(OfflinePlayer player, double amount) {
        economy.withdrawPlayer(player, amount);
        messenger.Debug("Vault", "Player payed some money", "Player= §6" + player.getName(), "Money= §6" + amount, "New Balance= §6" + (int) getPlayerBalance(player));
    }

    public void depositPlayerMoney(OfflinePlayer player, double amount) {
        economy.depositPlayer(player, amount);
        messenger.Debug("Vault", "Player got some money", "Player= §6" + player.getName(), "Money= §6" + amount, "New Balance= §6" + (int) getPlayerBalance(player));
    }

    public boolean playerGroupExists(String group) {
        return Arrays.stream(permission.getGroups()).toList().contains(group.toLowerCase());
    }
}
