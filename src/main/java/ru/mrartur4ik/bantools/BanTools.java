package ru.mrartur4ik.bantools;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.mrartur4ik.bantools.commands.*;
import ru.mrartur4ik.bantools.config.Ban;
import ru.mrartur4ik.bantools.config.BansConfiguration;
import ru.mrartur4ik.bantools.config.Configuration;

import java.text.SimpleDateFormat;
import java.util.*;

public class BanTools extends JavaPlugin implements Listener, Runnable {

    private Configuration config;
    private BansConfiguration bansConfig;

    private static BanTools instance;

    public SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("ru"));

    @Override
    public void onEnable() {
        instance = this;

        ConfigurationSerialization.registerClass(Ban.class);

        this.config = new Configuration();
        this.bansConfig = new BansConfiguration();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this, 0, 20);

        CommandMap commandMap = Bukkit.getCommandMap();

        List<Command> cmdList = new ArrayList<>();
        cmdList.add(new BanCommand());
        cmdList.add(new BanIPCommand());
        cmdList.add(new ReloadBansCommand());
        cmdList.add(new UnbanCommand());
        cmdList.add(new UnbanIPCommand());
        cmdList.add(new TempBanCommand());
        cmdList.add(new TempBanIPCommand());

        commandMap.registerAll(getName(), cmdList);

        getServer().getPluginManager().registerEvents(this, this);
    }

    public static BanTools getInstance() {
        return instance;
    }

    public BansConfiguration getBansConfig() {
        return bansConfig;
    }

    @Override
    public @NotNull Configuration getConfig() {
        return config;
    }

    public String kickMessage(Ban ban, boolean ipban) {
        String message = ipban ? config.getColorizedString("kick.ip-kick-message")
                : config.getColorizedString("kick.kick-message");

        StringBuilder str = new StringBuilder(message.replace("%player%", Utils.getDisplayName(ban.getFrom())));
        if (!ban.getReason().equals("")) {
            str.append("\n");
            str.append(config.getColorizedString("kick.reason").replace("%reason%", ban.getReason()));
        }
        if (ban.getTime() != -1) {
            str.append("\n");
            str.append(config.getColorizedString("kick.expire").replace("%date%", dateFormat.format(new Date(ban.getTime()))));
        }

        return str.toString();
    }

    public String broadcastMessage(UUID uuid, Ban ban) {
        StringBuilder str = new StringBuilder(config.getColorizedString("broadcast.ban-message").replace("%banned%", Utils.getDisplayName(uuid)).replace("%player%", Utils.getDisplayName(ban.getFrom())));

        if (!ban.getReason().equals("")) {
            str.append(config.getColorizedString("broadcast.reason").replace("%reason%", ban.getReason()));
        }
        if (ban.getTime() != -1) {
            str.append(config.getColorizedString("broadcast.expire").replace("%date%", BanTools.getInstance().dateFormat.format(new Date(ban.getTime()))));
        }

        return str.toString();
    }

    public String broadcastMessage(String address, Ban ban) {
        String banned;
        List<OfflinePlayer> list = bansConfig.getPlayersByAddress(address);
        if(!list.isEmpty()) {
            StringBuilder bannedpls = new StringBuilder();
            for(int i = 0; i < list.size(); i++) {
                OfflinePlayer bannedpl = list.get(i);
                bannedpls.append(Utils.getDisplayName(bannedpl.getUniqueId())).append(i < list.size() - 1 ? "": ", ");
            }
            banned = bannedpls.toString();
        } else {
            banned = address.replace('-', '.');
        }

        StringBuilder str = new StringBuilder(config.getColorizedString("broadcast.ban-ip-message").replace("%banned%", banned).replace("%player%", Utils.getDisplayName(ban.getFrom())));

        if (!ban.getReason().equals("")) {
            str.append(config.getColorizedString("broadcast.reason").replace("%reason%", ban.getReason()));
        }
        if (ban.getTime() != -1) {
            str.append(config.getColorizedString("broadcast.expire").replace("%date%", BanTools.getInstance().dateFormat.format(new Date(ban.getTime()))));
        }

        return str.toString();
    }

    public String unbanMessage(UUID uuid, Ban ban) {
        return config.getColorizedString("broadcast.unban-message").replace("%unbanned%", Utils.getDisplayName(uuid)).replace("%player%", Utils.getDisplayName(ban.getFrom()));
    }

    public String unbanMessage(String address, Ban ban) {
        String banned;
        List<OfflinePlayer> list = bansConfig.getPlayersByAddress(address);
        if(!list.isEmpty()) {
            StringBuilder bannedpls = new StringBuilder();
            for(int i = 0; i < list.size(); i++) {
                OfflinePlayer bannedpl = list.get(i);
                bannedpls.append(Utils.getDisplayName(bannedpl.getUniqueId())).append(i < list.size() - 1 ? "": ", ");
            }
            banned = bannedpls.toString();
        } else {
            banned = address.replace('-', '.');
        }

        return config.getColorizedString("broadcast.unban-message").replace("%banned%", banned).replace("%player%", Utils.getDisplayName(ban.getFrom()));
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Map<String, Ban> ipbans = bansConfig.getIPBans();
        Map<UUID, Ban> bans = bansConfig.getBans();

        UUID uuid = event.getPlayer().getUniqueId();
        String ip = event.getAddress().getHostAddress().replace(".", "-");

        bansConfig.addIP(uuid, event.getAddress());
        bansConfig.saveConfig();

        if (ipbans.containsKey(ip)) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage(ipbans.get(ip), true));
            return;
        }

        if(bans.containsKey(uuid)) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, kickMessage(bans.get(uuid), false));
        }
    }

    @Override
    public void run() {
        Map<UUID, Ban> bans = bansConfig.getBans();
        for(Ban ban : bans.values()) {
            if(ban.getTime() <= System.currentTimeMillis()) {
                bansConfig.set("bans." + Utils.getByValue(bans, ban), null);
                bansConfig.saveConfig();
            }
        }

        Map<String, Ban> ipbans = bansConfig.getIPBans();
        for(Ban ban : ipbans.values()) {
            if(ban.getTime() <= System.currentTimeMillis()) {
                bansConfig.set("ipbans." + Utils.getByValue(ipbans, ban), null);
                bansConfig.saveConfig();
            }
        }
    }
}