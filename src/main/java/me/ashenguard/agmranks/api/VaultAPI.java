package me.ashenguard.agmranks.api;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.api.messenger.Messenger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;

import static org.bukkit.Bukkit.getServer;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class VaultAPI {
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


    public VaultAPI() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            messenger.Warning("§6Vault§r wasn't found");
            return;
        }

        economyEnabled = setupEconomy();
        chatEnabled = setupChat();
        permissionsEnabled = setupPermissions();

        messenger.Debug("Vault", "Vault was hooked with this status: ",
                "Vault Economy: §6" + (economyEnabled? "§aEnable": "§cDisable"),
                "Vault Chat: §6" + (chatEnabled? "§aEnable": "§cDisable"),
                "Vault Permission: §6" + (permissionsEnabled? "§aEnable": "§cDisable"));

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
        messenger.Debug("Vault", String.format("§6%s§r has been added to \"§6%s§r\" group", player.getName(), group));
    }

    public void removePlayerGroup(OfflinePlayer player, String group) {
        permission.playerRemoveGroup(null, player, group);
        messenger.Debug("Vault",String.format("§6%s§r has been removed from \"§6%s§r\" group", player.getName(), group));
    }

    public double getPlayerBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public EconomyResponse withdrawPlayerMoney(OfflinePlayer player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (response.transactionSuccess())
            messenger.Debug("Vault", String.format("§6%s§r's %.1f transaction (Withdraw) has been§a successful§r", player.getName(), amount));
        else
            messenger.Debug("Vault",String.format("§6%s§r's %.1f transaction (Withdraw) has been§c failed§r", player.getName(), amount));
        return response;
    }
    public EconomyResponse depositPlayerMoney(OfflinePlayer player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        if (response.transactionSuccess())
            messenger.Debug("Vault", String.format("§6%s§r's %.1f transaction (Deposit) has been§a successful§r", player.getName(), amount));
        else
            messenger.Debug("Vault",String.format("§6%s§r's %.1f transaction (Deposit) has been§c failed§r", player.getName(), amount));
        return response;
    }

    public boolean playerGroupExists(String group) {
        return Arrays.stream(permission.getGroups()).toList().contains(group.toLowerCase());
    }
}
