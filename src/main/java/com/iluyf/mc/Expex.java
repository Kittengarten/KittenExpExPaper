package com.iluyf.mc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public class Expex extends JavaPlugin implements Listener {

    public Set<String> set;

    @Override
    public void onLoad() {
        getServer().sendMessage(Component.text("KittenExpEx 已加载。", NamedTextColor.AQUA));
    }

    @Override
    public void onEnable() {
        PluginCommand command = getCommand("expex");
        if (command != null) {
            command.setExecutor(new ExpCommand());
        } else {
            getLogger().severe("无法使用命令 'expex'：未在 plugin.yml 中注册！");
        }
        ConfigurationSection section = getConfig().getDefaultSection();
        if (section != null) {
            set = section.getKeys(false);
        }
        getServer().sendMessage(Component.text("KittenExpEx 开始运行。", NamedTextColor.GREEN));
        getServer().sendMessage(Component.join(
                JoinConfiguration.spaces(),
                Component.text("加载了"),
                Component.text(set.size(), NamedTextColor.AQUA),
                Component.text("条价目表。")));
    }

    @Override
    public void onDisable() {
        getServer().sendMessage(Component.text("KittenExpEx 暂停运行。", NamedTextColor.RED));
    }

    public class ExpCommand implements TabExecutor {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
            if (1 == args.length) {
                return Arrays.asList("list", "item", "exp");
            }
            switch (args[0].toLowerCase()) {
                case "item" -> {
                    switch (args.length) {
                        case 2 -> {
                            try {
                                return new ArrayList<>(set);
                            } catch (NumberFormatException e) {
                                return new ArrayList<>();
                            }
                        }
                        case 3 -> {
                            if (set.contains(args[1])) {
                                return Arrays.asList("[数量] ");
                            }
                            return new ArrayList<>();
                        }
                        default -> {
                            return new ArrayList<>();
                        }
                    }
                }
                case "exp" -> {
                    return switch (args.length) {
                        case 2 ->
                            Arrays.asList("[数量]");
                        default ->
                            new ArrayList<>();
                    };
                }
                default -> {
                    return new ArrayList<>();
                }
            }
        }

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (1 > args.length) {
                return false;
            }
            switch (cmd.getName().toLowerCase()) {
                case "expex", "ee" -> {
                }
                default -> {
                    return false;
                }
            }
            if (!sender.hasPermission("kittenexpex.expex")) {
                return false;
            }
            Player player;
            // 设置玩家当前的经验值（包括等级）
            interface PlayerExpSetter {

                void set(Player p, int exp);
            }
            PlayerExpSetter playerExpSetter = (p, exp) -> {
                p.setExp(0);
                p.setLevel(0);
                p.giveExp(exp);
            };
            // 获取玩家当前的总经验值（包括等级）
            interface ExpGetterByPlayer {

                int getBy(Player p);
            }
            ExpGetterByPlayer expGetterByPlayer = (p) -> {
                int level = p.getLevel();
                // 经验值计算公式
                interface ExpGetter {

                    int get();
                }
                ExpGetter expGetter = () -> {
                    if (16 > level) {
                        return level * level + 6 * level;
                    }
                    if (31 > level) {
                        return (int) (2.5 * level * level - 40.5 * level + 360);
                    }
                    return (int) (4.5 * level * level - 162.5 * level + 2220);
                };
                ExpGetter expGetterByLevel = () -> {
                    if (15 > level) {
                        return 2 * level + 7;
                    }
                    if (30 > level) {
                        return 5 * level - 38;
                    }
                    return 9 * level - 158;
                };
                return expGetter.get() + (int) (p.getExp() * expGetterByLevel.get());
            };
            int priceAll, exp;
            switch (args[0].toLowerCase()) {
                case "list" -> {
                    StringBuilder msg = new StringBuilder("【资源列表】");
                    for (String i : set) {
                        msg.append(String.format("\n%s 名称：%s 价格：%d", i, getConfig().getString(i + ".name"),
                                getConfig().getInt(i + ".price")));
                    }
                    sender.sendMessage(msg.toString());
                    return true;
                }
                case "item" -> {
                    if (!(sender instanceof Player)) {
                        return false;
                    }
                    player = (Player) sender;
                    if (3 > args.length) {
                        return false;
                    }
                    int count;
                    try {
                        count = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    if (1 > count) {
                        return false;
                    }
                    if (64 < count) {
                        sender.sendMessage("一次最多兑换一组喵！");
                        return true;
                    }
                    priceAll = getConfig().getInt(args[1] + ".price") * count;
                    if (32 > priceAll) {
                        return false;
                    }
                    String name = getConfig().getString(args[1] + ".name");
                    ItemStack item = new ItemStack(Material.getMaterial(args[1].toUpperCase()), count);
                    exp = expGetterByPlayer.getBy(player);
                    PlayerInventory inventory = player.getInventory();
                    int firstEmpty = inventory.firstEmpty();
                    if (exp < priceAll) {
                        sender.sendMessage(String.format("需要 %d 点经验值，你当前的 %d 点经验值不够喵！", priceAll, exp));
                        return true;
                    }
                    if (-1 == firstEmpty) {
                        sender.sendMessage("物品栏已满喵！");
                        return true;
                    }
                    exp -= priceAll;
                    playerExpSetter.set(player, exp);
                    inventory.setItem(firstEmpty, item);
                    Component msg = Component.join(
                            JoinConfiguration.spaces(),
                            Component.text("兑换了"),
                            Component.text(count, NamedTextColor.GREEN),
                            Component.text("个"),
                            Component.text("[" + name + "]", NamedTextColor.YELLOW));
                    getServer().sendMessage(Component.join(
                            JoinConfiguration.spaces(),
                            Component.text(player.getName(), NamedTextColor.GOLD),
                            msg));
                    sender.sendMessage(msg);
                    return true;
                }
                case "exp" -> {
                    if (!(sender instanceof Player)) {
                        return false;
                    }
                    player = (Player) sender;
                    if (2 > args.length) {
                        return false;
                    }
                    int gexp;
                    try {
                        gexp = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("不合法的值！", NamedTextColor.RED));
                        return false;
                    }
                    if (1 > gexp) {
                        sender.sendMessage(Component.join(
                                JoinConfiguration.spaces(),
                                Component.text("一次最少兑换"),
                                Component.text(1, NamedTextColor.AQUA),
                                Component.text("点喵！")));
                        return true;
                    }
                    if (256 < gexp) {
                        sender.sendMessage(Component.join(
                                JoinConfiguration.spaces(),
                                Component.text("一次最多兑换"),
                                Component.text(256, NamedTextColor.AQUA),
                                Component.text("点喵！")));
                        return true;
                    }
                    priceAll = 2 * gexp;
                    exp = expGetterByPlayer.getBy(player);
                    if (exp < priceAll) {
                        sender.sendMessage(Component.join(
                                JoinConfiguration.spaces(),
                                Component.text("需要"),
                                Component.text(priceAll, NamedTextColor.AQUA),
                                Component.text("点经验值，你当前的"),
                                Component.text(exp, NamedTextColor.AQUA),
                                Component.text("点经验值不够喵！")));
                        return true;
                    }
                    exp -= priceAll;
                    playerExpSetter.set(player, exp);
                    player.giveExp(gexp, true);
                    Component msg = Component.join(
                            JoinConfiguration.spaces(),
                            Component.text("兑换了"),
                            Component.text(gexp, NamedTextColor.AQUA),
                            Component.text("点"),
                            Component.text("[经验值]", NamedTextColor.AQUA));
                    getServer().sendMessage(Component.join(
                            JoinConfiguration.spaces(),
                            Component.text(player.getName(), NamedTextColor.GOLD),
                            msg));
                    sender.sendMessage(msg);
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }
    }
}
