package ru.mrartur4ik.bantools.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mrartur4ik.bantools.BanTools;
import ru.mrartur4ik.bantools.Utils;
import ru.mrartur4ik.bantools.config.Ban;
import ru.mrartur4ik.bantools.config.BansConfiguration;
import ru.mrartur4ik.bantools.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class BanIPCommand extends SimpleCommand {

    private static final BanTools plugin = BanTools.getInstance();
    private final BansConfiguration bansConfig = plugin.getBansConfig();
    private final Configuration config = plugin.getConfig();

    public BanIPCommand() {
        super("ban-ip", "Забанить игрока по IP навсегда", "/ban-ip <никнейм/ip> [причина]", "bantools.ban-ip");
    }

    @Override
    public boolean exec(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if(args.length > 0) {
            String ip = args[0];
            if(!bansConfig.getIPBans().containsKey(ip)) {
                String reason = "";
                UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
                if(args.length > 1) {
                    StringBuilder str = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        str.append(args[i]).append(i < args.length - 1 ? "" : " ");
                    }
                    reason = ChatColor.translateAlternateColorCodes('&', str.toString());
                }
                if(sender instanceof Player) {
                    uuid = ((Player) sender).getUniqueId();
                }
                Ban ban = new Ban(reason, uuid);

                if(Pattern.matches("([0-9]{1,3}[.]){3}[0-9]{1,3}", ip)) {
                    bansConfig.banIP(ip.replace('.', '-'), ban);

                    Bukkit.broadcastMessage(plugin.broadcastMessage(ip.replace('.', '-'), ban));

                    for(Player p : Utils.getOnlinePlayersByAddress(ip)) {
                        p.kickPlayer(plugin.kickMessage(ban, true));
                    }
                } else {
                    OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(ip);

                    if(target == null) {
                        sender.sendMessage(config.getColorizedString("info.player-has-not-played-before"));
                        return true;
                    }

                    List<String> ips = bansConfig.getIPs(target.getUniqueId());

                    for(String s : ips) {
                        if(bansConfig.getIPBans().containsKey(s)) {
                            sender.sendMessage(config.getColorizedString("info.player-already-in-ban"));
                            break;
                        }
                        bansConfig.banIP(s, ban);

                        Bukkit.broadcastMessage(plugin.broadcastMessage(s, ban));
                    }

                    if(target.isOnline()) {
                        Player p = (Player) target;
                        String address = p.getAddress().getAddress().getHostAddress().replace('.', '-');
                        if(!bansConfig.getIPBans().containsKey(address)){
                            bansConfig.banIP(address, ban);

                            Bukkit.broadcastMessage(plugin.broadcastMessage(address, ban));
                        }

                        p.kickPlayer(plugin.kickMessage(ban, true));
                    }
                }
                bansConfig.saveConfig();
            } else {
                sender.sendMessage(config.getColorizedString("info.player-already-in-ban"));
            }
            return true;
        }
        return false;
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if(args.length == 1) {
            List<String> list = new ArrayList<>();
            for(@NotNull OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                for(String ip : bansConfig.getIPs(p.getUniqueId())) {
                    if(!bansConfig.getIPBans().containsKey(ip)) {
                        list.add(p.getName());
                        break;
                    }
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
