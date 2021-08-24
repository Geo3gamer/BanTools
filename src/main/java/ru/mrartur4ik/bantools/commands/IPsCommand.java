package ru.mrartur4ik.bantools.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.mrartur4ik.bantools.BanTools;
import ru.mrartur4ik.bantools.config.BansConfiguration;
import ru.mrartur4ik.bantools.config.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IPsCommand extends SimpleCommand {

    private static final BanTools plugin = BanTools.getInstance();

    private final BansConfiguration bansConfig = plugin.getBansConfig();
    private final Configuration config = plugin.getConfig();

    public IPsCommand() {
        super("ips", "Посмотреть IP игрока", "/ips <никнейм>", "bantools.ips");
    }

    @Override
    public boolean exec(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        //TODO
        return false;
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if(args.length == 1) {
            List<String> list = new ArrayList<>();
            for(@NotNull OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                if(bansConfig.getIPs(p.getUniqueId()) != null) {
                    list.add(p.getName());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}


