package com.iluyf.mc;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.ChatColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Expex extends JavaPlugin implements Listener {
    public List<String> list;
    public String[] array = { "amethyst_block", "blue_ice", "coal_block", "copper_block", "diamond_block",
            "emerald_block", "enchanted_golden_apple", "gold_block", "iron_block", "lapis_block", "netherite_block",
            "quartz_block", "redstone_block", "sand", "shulker_shell", "totem_of_undying" };

    @Override
    public void onLoad() {
        getLogger().info(AQUA + "KittenExpEx 已加载。");
    }

    @Override
    public void onEnable() {
        this.getCommand("expex").setExecutor(new ExpCommand());
        list = List.of(array);
        getLogger().info(GREEN + "KittenExpEx 开始运行。");
        getLogger().info(String.format("加载了 %d 条价目表", array.length));
    }

    @Override
    public void onDisable() {
        getLogger().info(RED + "KittenExpEx 暂停运行。");
    }

    public class ExpCommand implements TabExecutor {

        @Override
        public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
            if (1 == args.length) {
                return Arrays.asList("list", "item");
            }
            if (2 == args.length && "item".equals(args[0])) {
                return list;
            }
            if (3 == args.length && "item".equals(args[0]) && list.contains(args[1])) {
                return Arrays.asList("[数量]");
            }
            return new ArrayList<>();
        }

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
            if (1 > args.length) {
                return false;
            }
            if (cmd.getName().equalsIgnoreCase("expex") || cmd.getName().equalsIgnoreCase("ee")) {
                if (sender.hasPermission("kittenexpex.expex")) {
                    if ("list".equalsIgnoreCase(args[0])) {
                        String msg = "【资源列表】";
                        for (String i : list) {
                            msg += String.format("\n%s 名称：%s 价格：%s", i, getConfig().getString(i + ".name"),
                                    getConfig().getInt(i + ".price"));
                        }
                        sender.sendMessage(msg);
                        return true;
                    }
                    if ("item".equalsIgnoreCase(args[0]) && (sender instanceof Player)) {
                        if (3 > args.length) {
                            return false;
                        }
                        int count;
                        try {
                            count = Integer.valueOf(args[2]);
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
                        int priceAll = getConfig().getInt(args[1] + ".price") * count;
                        if (32 > priceAll) {
                            return false;
                        }
                        String name = getConfig().getString(args[1] + ".name");
                        ItemStack item = new ItemStack(Material.getMaterial(args[1].toUpperCase()), count);
                        Player player = (Player) sender;
                        int level = player.getLevel();
                        int exp, lexp;
                        double pexp = player.getExp();
                        if (16 > level) {
                            exp = level * level + 6 * level;
                        } else if (32 > level && 16 <= level) {
                            exp = (int) (2.5 * level * level - 40.5 * level + 360);
                        } else {
                            exp = (int) (4.5 * level * level - 162.5 * level + 2220);
                        }
                        if (15 > level) {
                            lexp = 2 * level + 7;
                        } else if (31 > level && 15 <= level) {
                            lexp = 5 * level - 38;
                        } else {
                            lexp = 9 * level - 158;
                        }
                        exp += pexp * lexp;
                        if (exp < priceAll) {
                            sender.sendMessage(String.format("需要 %d 点经验值，你当前的 %d 点经验值不够喵！", priceAll, exp));
                        } else {
                            exp -= priceAll;
                            player.setExp(0);
                            player.setLevel(0);
                            player.giveExp(exp);
                            PlayerInventory inventory = player.getInventory();
                            inventory.addItem(item);
                            getLogger().info(String.format("%s 兑换了 %d 个[%s]", player.getName(), count, name));
                            sender.sendMessage(String.format("兑换了 %d 个[%s]", count, name));
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
