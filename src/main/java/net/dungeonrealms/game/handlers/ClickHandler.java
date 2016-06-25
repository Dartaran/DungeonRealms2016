package net.dungeonrealms.game.handlers;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.PlayerManager;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.miscellaneous.TradeCalculator;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.inventory.NPCMenus;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.inventory.SupportMenus;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.player.stats.StatsManager;
import net.dungeonrealms.game.player.support.Support;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMounts;
import net.dungeonrealms.game.world.entities.types.pets.EnumPets;
import net.dungeonrealms.game.world.entities.utils.EntityAPI;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import net.dungeonrealms.game.world.entities.utils.PetUtils;
import net.dungeonrealms.game.world.items.EnumItem;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nick on 10/2/2015.
 */
public class ClickHandler {

    static ClickHandler instance = null;

    public static ClickHandler getInstance() {
        if (instance == null) {
            instance = new ClickHandler();
        }
        return instance;
    }

    public void doClick(InventoryClickEvent event) {
        String name = event.getInventory().getName();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == -999) return;

        /*
        Animal Tamer NPC
         */
        switch (name) {
            case "Animal Tamer":
                event.setCancelled(true);
                if (slot > 18) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack == null) return;
                    if (nmsStack.getTag() == null) return;
                    List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                    if (playerMounts.contains(nmsStack.getTag().getString("mountType"))) {
                        player.sendMessage(ChatColor.RED + "You already own this mount!");
                        return;
                    } else {
                        EnumMounts mount = EnumMounts.getByName(nmsStack.getTag().getString("mountType"));
                        if (MountUtils.hasRequiredLevel(mount, player.getUniqueId())) {
                            if (MountUtils.hasMountPrerequisites(mount, playerMounts)) {
                                if (BankMechanics.getInstance().takeGemsFromInventory(nmsStack.getTag().getInt("mountCost"), player)) {
                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNTS, mount.getRawName(), true);
                                    if (mount != EnumMounts.MULE) {
                                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT, mount.getRawName(), true);
                                        Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.MOUNT_OWNER);
                                    }
                                    player.sendMessage(ChatColor.GREEN + "You have purchased the " + mount.getDisplayName() + ChatColor.GREEN + " mount.");
                                    player.closeInventory();
                                    return;
                                } else {
                                    player.sendMessage(ChatColor.RED + "You cannot afford this mount, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("mountCost") + ChatColor.RED + " Gems.");
                                    return;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You must own the previous mount to upgrade.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not meet the minimum level requirements to own this mount.");
                        }
                    }
                }
                break;


        /*
        Skill Trainer NPC
         */
            case "Profession Vendor":
                event.setCancelled(true);
                if (slot > 9) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    if (BankMechanics.getInstance().takeGemsFromInventory(100, player)) {
                        switch (slot) {
                            case 0:
                                player.getInventory().addItem(ItemManager.createPickaxe(1));
                                player.sendMessage(ChatColor.GREEN + "Transaction successful.");
                                player.closeInventory();
                                break;
                            case 1:
                                player.getInventory().addItem(ItemManager.createFishingPole(1));
                                player.sendMessage(ChatColor.GREEN + "Transaction successful.");
                                player.closeInventory();
                                break;
                            default:
                                break;
                        }
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot afford this item, you require " + ChatColor.BOLD + "100" + ChatColor.RED + " Gems");
                    }
                    return;
                }
                break;


        /*
        E-Cash Vendor NPC
         */
            case "E-Cash Vendor":
                event.setCancelled(true);
                if (slot > 25) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack == null) return;
                    if (nmsStack.getTag() == null) return;
                    if (nmsStack.getTag().hasKey("playerTrailType")) {
                        List<String> playerTrails = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, player.getUniqueId());
                        if (playerTrails.contains(nmsStack.getTag().getString("playerTrailType"))) {
                            player.sendMessage(ChatColor.RED + "You already own this trail!");
                            return;
                        } else {
                            if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PARTICLES, nmsStack.getTag().getString("playerTrailType").toUpperCase(), false);
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_TRAIL, nmsStack.getTag().getString("playerTrailType").toUpperCase(), true);
                                player.sendMessage(ChatColor.GREEN + "You have purchased the " + ParticleAPI.ParticleEffect.getByName(nmsStack.getTag().getString("playerTrailType")).getDisplayName() + " trail.");
                                player.closeInventory();
                                return;
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot afford this trail, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                                return;
                            }
                        }
                    }
                    if (nmsStack.getTag().hasKey("skinType")) {
                        if (DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT, player.getUniqueId()).equals("")) {
                            player.sendMessage(ChatColor.RED + "You cannot purchase a mount skin as you do not own a mount!");
                            return;
                        }
                        List<String> playerMountSkins = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNT_SKINS, player.getUniqueId());
                        if (playerMountSkins.contains(nmsStack.getTag().getString("skinType"))) {
                            player.sendMessage(ChatColor.RED + "You already own this mount skin!");
                            return;
                        } else {
                            if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.MOUNT_SKINS, nmsStack.getTag().getString("skinType").toUpperCase(), false);
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT_SKIN, nmsStack.getTag().getString("skinType").toUpperCase(), true);
                                player.sendMessage(ChatColor.GREEN + "You have purchased the " + EnumMountSkins.getByName(nmsStack.getTag().getString("skinType")).getDisplayName() + " mount skin");
                                player.closeInventory();
                                return;
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot afford this mount skin, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                                return;
                            }
                        }
                    }
                    if (nmsStack.getTag().hasKey("petType")) {
                        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, player.getUniqueId());
                        if (playerPets.contains(nmsStack.getTag().getString("petType"))) {
                            player.sendMessage(ChatColor.RED + "You already own this pet.");
                            return;
                        } else {
                            if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, nmsStack.getTag().getString("petType").toUpperCase(), false);
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, nmsStack.getTag().getString("petType").toUpperCase(), true);
                                player.sendMessage(ChatColor.GREEN + "You have purchased the " + EnumPets.getByName(nmsStack.getTag().getString("petType")).getDisplayName() + " pet.");
                                player.closeInventory();
                                return;
                            } else {
                                player.sendMessage(ChatColor.RED + "You cannot afford this pet, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                                return;
                            }
                        }
                    }
                    /*if (nmsStack.getTag().hasKey("donationStore")) {
                        player.closeInventory();
                        TextComponent bungeeMessage = new TextComponent(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE");
                        bungeeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://shop.dungeonrealms.net"));
                        bungeeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Dungeon Realms Shop").create()));
                        TextComponent test = new TextComponent(ChatColor.RED + "To Purchase E-Cash from our Shop, Click ");
                        test.addExtra(bungeeMessage);
                        player.spigot().sendMessage(test);
                        return;
                    }*/
                    if (nmsStack.getTag().hasKey("storageExpansion")) {
                        if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                            player.getInventory().addItem(ItemManager.createItem(EnumItem.StorageExpansion));
                            player.sendMessage(ChatColor.GREEN + "You have purchased a Storage Expansion.");
                            player.closeInventory();
                            return;
                        } else {
                            player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                            return;
                        }
                    }
                    if (nmsStack.getTag().hasKey("repairHammer")) {
                        player.sendMessage(ChatColor.RED + "This is currently not implemented");
                        player.closeInventory();
                    /*if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                        player.getInventory().addItem(ItemManager.createItem(EnumItem.RepairHammer));
                        player.sendMessage(ChatColor.GREEN + "You have purchased Five Repair Hammers.");
                        player.closeInventory();
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                        return;
                    }*/
                    }
                    if (nmsStack.getTag().hasKey("retrainingBook")) {
                        if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                            player.getInventory().addItem(ItemManager.createItem(EnumItem.RetrainingBook));
                            player.sendMessage(ChatColor.GREEN + "You have purchased a Retraining Book.");
                            player.closeInventory();
                            return;
                        } else {
                            player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash");
                            return;
                        }
                    }
                    if (nmsStack.getTag().hasKey("medalOfGathering")) {
                        player.sendMessage(ChatColor.RED + "This is currently not implemented!");
                        player.closeInventory();
                    /*if (DonationEffects.getInstance().removeECashFromPlayer(player, nmsStack.getTag().getInt("ecashCost"))) {
                        player.getInventory().addItem(ItemManager.createItem(EnumItem.MedalOfGathering));
                        player.sendMessage(ChatColor.GREEN + "You have purchased a Medal Of Gathering.");
                        player.closeInventory();
                        return;
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot afford this, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("ecashCost") + ChatColor.RED + " E-Cash!");
                        return;
                    }*/
                    }
                }
                break;


        /*
        Inn Keeper NPC
         */
            case "Hearthstone Re-Location":
                event.setCancelled(true);
                if (slot > 9) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack == null) return;
                    if (nmsStack.getTag() == null) return;
                    if (nmsStack.getTag().hasKey("hearthstoneLocation")) {
                        String hearthstoneLocation = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, player.getUniqueId()));
                        if (hearthstoneLocation.equalsIgnoreCase(nmsStack.getTag().getString("hearthstoneLocation"))) {
                            player.sendMessage(ChatColor.RED + "Your Hearthstone is already at this location!");
                            return;
                        } else {
                            if (TeleportAPI.canSetHearthstoneLocation(player, nmsStack.getTag().getString("hearthstoneLocation"))) {
                                if (BankMechanics.getInstance().takeGemsFromInventory(nmsStack.getTag().getInt("gemCost"), player)) {
                                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEARTHSTONE, nmsStack.getTag().getString("hearthstoneLocation"), true);
                                    player.sendMessage(ChatColor.GREEN + "Hearthstone set to " + nmsStack.getTag().getString("hearthstoneLocation") + ".");
                                    player.closeInventory();
                                    return;
                                } else {
                                    player.sendMessage(ChatColor.RED + "You do NOT have enough gems for this location, you require " + ChatColor.BOLD + nmsStack.getTag().getInt("gemCost") + ChatColor.RED + " Gems.");
                                    return;
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You have not explored the surrounding area of this Hearthstone Location yet");
                                return;
                            }
                        }
                    }
                }
                break;


        /*
        Merchant
         */
            case "Merchant":
                Inventory tradeWindow = event.getInventory();
                if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
                    event.setCancelled(true);
                    return;
                }
                if (!(event.isShiftClick()) || (event.isShiftClick() && slot < 27)) {
                    if (!(event.getSlotType() == InventoryType.SlotType.CONTAINER)) {
                        return;
                    }
                    if (event.getInventory().getType() == InventoryType.PLAYER) {
                        return;
                    }
                    if (slot > 26 || slot < 0) {
                        return;
                    }
                    if (!(slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18 || slot == 19
                            || slot == 20 || slot == 21) && !(slot > 27)) {
                        event.setCancelled(true);
                        tradeWindow.setItem(slot, tradeWindow.getItem(slot));
                        player.setItemOnCursor(event.getCursor());
                        player.updateInventory();
                    } else if (!(event.isShiftClick())) {
                        if ((event.getCursor() == null || event.getCursor().getType() == Material.AIR && event.getCurrentItem() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (!CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")))) {
                            event.setCancelled(true);
                            ItemStack slotItem = tradeWindow.getItem(slot);
                            tradeWindow.setItem(slot, new ItemStack(Material.AIR));
                            event.setCursor(slotItem);
                            player.updateInventory();
                        } else if ((event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR && event.getCursor() != null)) {
                            if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("subType"))) {
                                event.setCancelled(true);
                                player.updateInventory();
                            }
                            event.setCancelled(true);
                            ItemStack currentItem = event.getCursor();
                            tradeWindow.setItem(slot, currentItem);
                            event.setCursor(new ItemStack(Material.AIR));
                            player.updateInventory();
                        } else if (event.getCurrentItem() != null && event.getCursor() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && (!CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton"))) {
                            event.setCancelled(true);
                            ItemStack currentItem = event.getCursor();
                            ItemStack slotItem = event.getCurrentItem();
                            event.setCursor(slotItem);
                            event.setCurrentItem(currentItem);
                            player.updateInventory();
                        }
                    }
                }
                if (event.isShiftClick() && slot < 26) {
                    if (!(slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 9 || slot == 10 || slot == 11 || slot == 12 || slot == 18
                            || slot == 19 || slot == 20 || slot == 21) && !(slot > 27)) {
                        event.setCancelled(true);
                        if (tradeWindow.getItem(slot) != null && tradeWindow.getItem(slot).getType() != Material.AIR) {
                            tradeWindow.setItem(slot, tradeWindow.getItem(slot));
                            player.updateInventory();
                        }
                    } else if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && !CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")) {
                        event.setCancelled(true);
                        ItemStack slotItem = event.getCurrentItem();
                        if (player.getInventory().firstEmpty() != -1) {
                            tradeWindow.setItem(slot, new ItemStack(Material.AIR));
                            player.getInventory().setItem(player.getInventory().firstEmpty(), slotItem);
                            player.updateInventory();
                        }
                    }
                }
                if (event.isShiftClick() && slot >= 27 && !(event.isCancelled()) && !(event.getCurrentItem().getType() == Material.BOOK)) {
                    event.setCancelled(true);
                    ItemStack slotItem = event.getCurrentItem();
                    int localSlot = event.getSlot();
                    int x = -1;
                    while (x < 26) {
                        x++;
                        if (!(x == 0 || x == 1 || x == 2 || x == 3 || x == 9 || x == 10 || x == 11 || x == 12 || x == 18 || x == 19 || x == 20 || x == 21)) {
                            continue;
                        }
                        ItemStack itemStack = tradeWindow.getItem(x);
                        if (!(itemStack == null)) {
                            continue;
                        }
                        tradeWindow.setItem(x, slotItem);
                        if (tradeWindow.getItem(x) != null) {
                            tradeWindow.getItem(x).setAmount(slotItem.getAmount());
                        }
                        player.getInventory().setItem(localSlot, new ItemStack(Material.AIR));
                        player.updateInventory();
                        break;
                    }
                }
                List<ItemStack> player_Offer = new ArrayList<>();
                int x = -1;
                while (x < 26) {
                    x++;
                    if (!(x == 0 || x == 1 || x == 2 || x == 3 || x == 9 || x == 10 || x == 11 || x == 12 || x == 18 || x == 19 || x == 20 || x == 21)) {
                        continue;
                    }
                    ItemStack itemStack = tradeWindow.getItem(x);
                    if (itemStack == null || itemStack.getType() == Material.AIR || CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) {
                        continue;
                    }
                    player_Offer.add(itemStack);
                }
                List<ItemStack> new_Offer = TradeCalculator.calculateMerchantOffer(player_Offer);
                x = -1;
                while (x < 26) {
                    x++;
                    if ((x == 0 || x == 1 || x == 2 || x == 3 || x == 4 || x == 9 || x == 10 || x == 11 || x == 12 || x == 13 || x == 22 || x == 18 || x == 19
                            || x == 20 || x == 21)) {
                        continue;
                    }
                    tradeWindow.setItem(x, new ItemStack(Material.AIR));
                }
                x = -1;
                while (x < 26) {
                    x++;
                    if (new_Offer.size() > 0) {
                        if ((x == 0 || x == 1 || x == 2 || x == 3 || x == 4 || x == 9 || x == 10 || x == 11 || x == 12 || x == 13 || x == 22 || x == 18 || x == 19
                                || x == 20 || x == 21)) {
                            continue;
                        }
                        int index = new_Offer.size() - 1;
                        ItemStack itemStack = new_Offer.get(index);
                        tradeWindow.setItem(x, itemStack);
                        new_Offer.remove(index);
                    }
                }
                player.updateInventory();
                if (CraftItemStack.asNMSCopy(event.getCurrentItem()) != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag() != null && CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag().hasKey("acceptButton")) {
                    event.setCancelled(true);
                    if (event.getCurrentItem().getDurability() == 8) {
                        int player_Inv_Available = 0;
                        int inv_Needed = 0;
                        event.setCurrentItem(new ItemBuilder().setItem(Material.INK_SACK, (short) 10, ChatColor.GREEN + "Trade accepted.", new String[]{
                                ""
                        }).setNBTString("acceptButton", "whynot").build());
                        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1F, 2.F);

                        for (ItemStack itemStack : player.getInventory()) {
                            if (itemStack == null || itemStack.getType() == Material.AIR) {
                                player_Inv_Available++;
                            }
                        }
                        int slot_Variable = -1;
                        while (slot_Variable < 26) {
                            slot_Variable++;
                            if (!(slot_Variable == 5 || slot_Variable == 6 || slot_Variable == 7 || slot_Variable == 8 || slot_Variable == 14 || slot_Variable == 15
                                    || slot_Variable == 16 || slot_Variable == 17 || slot_Variable == 23 || slot_Variable == 24 || slot_Variable == 25 || slot_Variable == 26)) {
                                continue;
                            }
                            ItemStack itemStack = tradeWindow.getItem(slot_Variable);
                            if (itemStack == null || itemStack.getType() == Material.AIR || (CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) || itemStack.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            inv_Needed++;
                        }
                        if (player_Inv_Available < inv_Needed) {
                            player.sendMessage(ChatColor.RED + "Inventory is full.");
                            player.sendMessage(ChatColor.GRAY + "You require " + ChatColor.BOLD + (inv_Needed - player_Inv_Available) + ChatColor.GRAY + " more free slots to complete this trade!");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                InventoryCloseEvent closeEvent = new InventoryCloseEvent(player.getOpenInventory());
                                Bukkit.getServer().getPluginManager().callEvent(closeEvent);
                            }, 2L);
                            return;
                        }
                        slot_Variable = -1;
                        while (slot_Variable < 26) {
                            slot_Variable++;
                            if (!(slot_Variable == 5 || slot_Variable == 6 || slot_Variable == 7 || slot_Variable == 8 || slot_Variable == 14 || slot_Variable == 15
                                    || slot_Variable == 16 || slot_Variable == 17 || slot_Variable == 23 || slot_Variable == 24 || slot_Variable == 25 || slot_Variable == 26)) {
                                continue;
                            }
                            ItemStack itemStack = tradeWindow.getItem(slot_Variable);
                            if (itemStack == null || itemStack.getType() == Material.AIR || (CraftItemStack.asNMSCopy(itemStack) != null && CraftItemStack.asNMSCopy(itemStack).getTag() != null && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("acceptButton")) || itemStack.getType() == Material.THIN_GLASS) {
                                continue;
                            }
                            if (itemStack.getType() == Material.EMERALD) {
                                itemStack = BankMechanics.createBankNote(itemStack.getAmount());
                            }
                            player.getInventory().setItem(player.getInventory().firstEmpty(), itemStack);
                        }
                        player.sendMessage(ChatColor.GREEN + "Trade Accepted.");

                        for (ItemStack stack : tradeWindow.getContents()) {
                            if (stack != null && stack.getType() == Material.MAGMA_CREAM) {
                                break;
                            }
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1F, 1F);
                        tradeWindow.clear();

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            player.updateInventory();
                            player.closeInventory();
                        }, 2L);

                        return;
                    }
                    player.updateInventory();
                }
                break;
            case "Dungeoneer":
                event.setCancelled(true);
                if (slot > 9) return;
                if (event.getCurrentItem().getType() != Material.AIR) {
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack == null) return;
                    if (nmsStack.getTag() == null) return;
                    if (nmsStack.getTag().hasKey("shardTier") && nmsStack.getTag().hasKey("shardCost")) {
                        int shardCost = nmsStack.getTag().getInt("shardCost");
                        if (player.getInventory().firstEmpty() == -1) {
                            player.sendMessage(ChatColor.RED + "Your inventory is currently full!");
                            return;
                        }
                        if (API.removePortalShardsFromPlayer(player, nmsStack.getTag().getInt("shardTier"), nmsStack.getTag().getInt("shardCost"))) {
                            player.sendMessage(ChatColor.RED + "- " + shardCost + ChatColor.BOLD + "PKS");
                            player.sendMessage(ChatColor.GREEN + "Transaction successful.");
                            player.closeInventory();

                            //Give them their scroll.
                            if (event.getCurrentItem().getType() == Material.CHEST) {
                                //Add the storage mule upgrade.
                                ItemStack muleUpgrade = ItemManager.createMuleUpgrade(nmsStack.getTag().getInt("muleLevel"));
                                player.getInventory().addItem(muleUpgrade);
                            } else {
                                //Give them their scroll.
                                player.getInventory().addItem(ItemManager.createProtectScroll(nmsStack.getTag().getInt("shardTier")));
                            }
                            return;
                        } else {
                            String color = nmsStack.getTag().getString("shardColor");
                            player.sendMessage(ChatColor.RED + "You do " + ChatColor.UNDERLINE + "NOT" + ChatColor.RED + " have enough " + color + "Portal Key Shards" + ChatColor.RED + " to buy a " + event.getCurrentItem().getItemMeta().getDisplayName());
                            player.sendMessage(ChatColor.RED + "COST: " + ChatColor.WHITE + shardCost + color + " Portal Key Shards");
                            player.sendMessage(ChatColor.GRAY + "Defeat " + ChatColor.UNDERLINE + "Instanced Dungeons" + ChatColor.GRAY + " to obtain Portal Key Shards.");
                            return;
                        }
                    }
                }
                break;
            case "Friend Management":
                event.setCancelled(true);
                if (slot >= 44) return;
                if (slot == 1) {
                    PlayerMenus.openFriendsMenu(player);
                }
                if (slot == 0) {
                    player.sendMessage(ChatColor.GREEN + "Please enter the player's name...");
                    player.closeInventory();
                    Chat.listenForMessage(player, chat -> {
                        Player target = Bukkit.getPlayer(chat.getMessage());
                        if (target != null) {
                            FriendHandler.getInstance().sendRequest(player, target);
                            player.sendMessage(ChatColor.GREEN + "Friend request sent to " + target.getName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Oops, I can't find that player!");
                        }
                    }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
                    return;
                }
                FriendHandler.getInstance().addOrRemove(player, event.getClick(), event.getCurrentItem());
                break;
            case "Friends":
                event.setCancelled(true);
                if (slot >= 54) return;
                if (slot == 0) {
                    PlayerMenus.openFriendInventory(player);
                    return;
                }
                FriendHandler.getInstance().remove(player, event.getClick(), event.getCurrentItem());
                break;
            case "Mailbox":
                event.setCancelled(true);
                if (slot == 0) {
                    player.sendMessage(ChatColor.RED + "You cannot send mail yet! It's coming soon! :-)");
                    return;
                } else if (slot == 8) {
                    return;
                } else {
                    if (event.getCurrentItem() != null) {
                        ItemStack clickedItem = event.getCurrentItem();
                        MailHandler.getInstance().giveItemToPlayer(clickedItem, player);
                    }
                }
                break;
            case "Pet Selection":
                event.setCancelled(true);
                if (event.getCurrentItem().getType() == Material.BARRIER) {
                    PlayerMenus.openPlayerProfileMenu(player);
                    return;
                }
                if (event.getCurrentItem().getType() == Material.LEASH) {
                    if (EntityAPI.hasPetOut(player.getUniqueId())) {
                        Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                        if (entity.isAlive()) {
                            entity.getBukkitEntity().remove();
                        }
                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                        }
                        EntityAPI.removePlayerPetList(player.getUniqueId());
                        player.sendMessage(ChatColor.AQUA + "Pet dismissed.");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "You currently do not have a pet in the world.");
                    }
                    return;
                }
                if (event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.LEASH) {
                    if (event.getClick() == ClickType.LEFT) {
                        if (EntityAPI.hasPetOut(player.getUniqueId())) {
                            Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                            if (entity.isAlive()) {
                                entity.getBukkitEntity().remove();
                            }
                            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                            }
                            EntityAPI.removePlayerPetList(player.getUniqueId());
                        }
                        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                        if (nmsStack.getTag() == null || nmsStack.getTag().getString("petType") == null) {
                            player.sendMessage("Uh oh... Something went wrong with your pet! Please inform a staff member! [NBTTag]");
                            player.closeInventory();
                            return;
                        }
                        String petName = "";
                        if (nmsStack.getTag().getString("petName") != null) {
                            petName = nmsStack.getTag().getString("petName");
                        }
                        String toStore = nmsStack.getTag().getString("petType") + "@" + petName;
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_PET, toStore, true);
                        PetUtils.spawnPet(player.getUniqueId(), nmsStack.getTag().getString("petType"), petName);
                    } else if (event.getClick() == ClickType.RIGHT) {
                        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                        if (nmsStack.getTag() == null || nmsStack.getTag().getString("petType") == null) {
                            player.sendMessage("Uh oh... Something went wrong with your pet! Please inform a staff member! [NBTTag]");
                            player.closeInventory();
                            return;
                        }
                        String petType = nmsStack.getTag().getString("petType");
                        String petName = "";
                        if (nmsStack.getTag().getString("petName") != null) {
                            petName = nmsStack.getTag().getString("petName");
                        }
                        player.sendMessage(ChatColor.GRAY + "Enter a name for your pet, or type " + ChatColor.RED + ChatColor.UNDERLINE +"cancel" + ChatColor.GRAY + " to end the process.");
                        player.closeInventory();
                        String finalPetName = petName;
                        Chat.listenForMessage(player, newPetName -> {
                            if (newPetName.getMessage().equalsIgnoreCase("cancel") || newPetName.getMessage().equalsIgnoreCase("exit")) {
                                player.sendMessage(ChatColor.GRAY + "Pet naming " + ChatColor.RED + ChatColor.UNDERLINE + "CANCELLED.");
                                return;
                            }
                            String inputName = newPetName.getMessage();

                            // Name must be below 12 characters
                            if (inputName.length() > 12) {
                                player.sendMessage(ChatColor.RED + "Your pet name exceeds the maximum length of 12 characters.");
                                player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You were " + (inputName.length() - 16) + " characters over the limit.");
                                return;
                            }
                            Pattern pattern = Pattern.compile("^([A-Za-z]|[0-9])+$");
                            Matcher matcher = pattern.matcher(inputName);

                            // Name must be alphanumerical
                            if (!matcher.find()) {
                                player.sendMessage(ChatColor.RED + "Your pet name can only contain alphanumerical values.");
                                return;
                            }

                            String checkedPetName = Chat.getInstance().checkForBannedWords(inputName);

                            String newPet = petType + "@" + checkedPetName;
                            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, petType, false);
                            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.PETS, petType + "@" + finalPetName, false);
                            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.PETS, newPet, true);
                            player.sendMessage(ChatColor.GRAY + "Pet name changed to " + ChatColor.GREEN + ChatColor.UNDERLINE + checkedPetName);
                        }, null);
                    }
                }
                break;
            case "Mount Selection":
                event.setCancelled(true);
                if (event.getCurrentItem().getType() == Material.BARRIER) {
                    PlayerMenus.openPlayerProfileMenu(player);
                    return;
                }
                if (event.getCurrentItem().getType() == Material.LEASH) {
                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                        Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                        if (entity.isAlive()) {
                            entity.getBukkitEntity().remove();
                        }
                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                        }
                        EntityAPI.removePlayerMountList(player.getUniqueId());
                        player.sendMessage(ChatColor.AQUA + "Mount dismissed.");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "You currently do not have a mount in the world.");
                    }
                    return;
                }
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.LEASH) {
                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                        Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                        if (entity.isAlive()) {
                            entity.getBukkitEntity().remove();
                        }
                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                        }
                        EntityAPI.removePlayerMountList(player.getUniqueId());
                    }
                    if (CombatLog.isInCombat(player)) {
                        player.sendMessage(ChatColor.RED + "You cannot summon a mount while in Combat!");
                        return;
                    }
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack.getTag() == null || nmsStack.getTag().getString("mountType") == null) {
                        player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [NBTTag]");
                        player.closeInventory();
                        return;
                    }
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT, nmsStack.getTag().getString("mountType"), true);
                    player.sendMessage(ChatColor.GREEN + "Your Mount is being summoned into this world!");
                    final int[] count = {0};
                    Location startingLocation = player.getLocation();
                    final boolean[] cancelled = {false};
                    int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                        if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                            if (player.getLocation().distanceSquared(startingLocation) <= 4) {
                                if (!CombatLog.isInCombat(player)) {
                                    if (!cancelled[0]) {
                                        if (count[0] < 3) {
                                            count[0]++;
                                        } else {
                                            MountUtils.spawnMount(player.getUniqueId(), nmsStack.getTag().getString("mountType"), (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_MOUNT_SKIN, player.getUniqueId()));
                                        }
                                    }
                                } else {
                                    if (!cancelled[0]) {
                                        cancelled[0] = true;
                                        count[0] = 0;
                                        player.sendMessage(ChatColor.RED + "Combat has cancelled your mount summoning!");
                                    }
                                }
                            } else {
                                if (!cancelled[0]) {
                                    cancelled[0] = true;
                                    count[0] = 0;
                                    player.sendMessage(ChatColor.RED + "Movement has cancelled your mount summoning!");
                                }
                            }
                        }
                    }, 0L, 20L);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getScheduler().cancelTask(taskID), 65L);
                }
                break;
            case "Player Trail Selection":
                event.setCancelled(true);
                if (event.getCurrentItem().getType() == Material.BARRIER) {
                    PlayerMenus.openPlayerProfileMenu(player);
                    return;
                }
                if (event.getCurrentItem().getType() == Material.ARMOR_STAND) {
                    if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
                        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
                        player.sendMessage(ChatColor.AQUA + "You have disabled your trail.");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "You don't have a Player trail enabled.");
                    }
                    return;
                }
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.ARMOR_STAND) {
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack.getTag() == null || nmsStack.getTag().getString("playerTrailType") == null) {
                        player.sendMessage("Uh oh... Something went wrong with your Player trail! Please inform a staff member! [NBTTag]");
                        player.closeInventory();
                        return;
                    }
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_TRAIL, nmsStack.getTag().getString("playerTrailType"), true);
                    DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getByName(nmsStack.getTag().getString("playerTrailType")));
                    player.sendMessage(ChatColor.AQUA + "Enabling " + ChatColor.RED + ParticleAPI.ParticleEffect.getByName(nmsStack.getTag().getString("playerTrailType")).getDisplayName() + ChatColor.AQUA + " trail.");
                }
                break;
            case "Profile":
                event.setCancelled(true);
                switch (slot) {
                    case 0:
                        player.openInventory(StatsManager.getInventory(player));
                        break;
                    case 1:
                        PlayerMenus.openFriendInventory(player);
                        break;
                    case 6:
                        if (event.getClick() == ClickType.RIGHT) return;
                        PlayerMenus.openPlayerParticleMenu(player);
                        break;
                    case 7:
                        if (event.getClick() == ClickType.RIGHT) return;
                        PlayerMenus.openPlayerMountMenu(player);
                        break;
                    case 8:
                        if (event.getClick() == ClickType.RIGHT) return;
                        PlayerMenus.openPlayerPetMenu(player);
                        break;
                    case 16: {
                        if (event.getClick() == ClickType.RIGHT) return;
                        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                        if (!playerMounts.contains("MULE")) {
                            player.sendMessage(ChatColor.RED + "Purchase a storage mule from the Animal Tamer.");
                            return;
                        }
                        if (EntityAPI.hasMountOut(player.getUniqueId())) {
                            Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                            if (entity.isAlive()) {
                                entity.getBukkitEntity().remove();
                            }
                            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                            }
                            EntityAPI.removePlayerMountList(player.getUniqueId());
                            player.sendMessage(ChatColor.AQUA + " Mount dismissed.");
                        }
                        if (EntityAPI.hasPetOut(player.getUniqueId())) {
                            Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                            if (entity.isAlive()) {
                                entity.getBukkitEntity().remove();
                            }
                            if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                                DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                            }
                            EntityAPI.removePlayerPetList(player.getUniqueId());
                            player.sendMessage(ChatColor.AQUA + "Pet dismissed.");
                        }
                        if (CombatLog.isInCombat(player)) {
                            player.sendMessage(ChatColor.RED + "You cannot summon a storage mule while in Combat!");
                            return;
                        }
                        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                        if (nmsStack.getTag() == null || nmsStack.getTag().getString("mountType") == null) {
                            player.sendMessage("Uh oh... Something went wrong with your mount! Please inform a staff member! [NBTTag]");
                            player.closeInventory();
                            return;
                        }
                        player.sendMessage(ChatColor.GREEN + "Summoning storage mule.");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                                MountUtils.spawnMount(player.getUniqueId(), "MULE", "");
                            } else {
                                player.sendMessage(ChatColor.RED + "You just summoned your mount!");
                            }
                        }, 60L);
                        player.closeInventory();
                        break;
                        //SPAWN
                    }
                    case 17:
                        PlayerMenus.openPlayerMountSkinMenu(player);
                        break;
                    case 18:
                        NPCMenus.openECashPurchaseMenu(player);
                        break;
                    case 24:
                        PlayerMenus.openPlayerAchievementsMenu(player);
                        break;
                    case 26:
                        PlayerMenus.openToggleMenu(player);
                        break;
                    default:
                        break;
                }
                break;
            case "Wizard":
                GamePlayer gp = API.getGamePlayer(player);
                if (gp.getLevel() >= 10) {
                    if (gp.getStats().resetAmounts > 0) {
                        player.sendMessage(ChatColor.GREEN + "ONE stat reset available. Type 'yes' or 'y' to continue.");
                        player.closeInventory();
                        Chat.listenForMessage(player, e -> {
                            if (e.getMessage().equalsIgnoreCase("Yes") || e.getMessage().equalsIgnoreCase("y")) {
                                gp.getStats().freeResets -= 1;
                                player.sendMessage(ChatColor.GREEN + "Stats reset");
                            }
                        }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
                    } else {
                        player.sendMessage(ChatColor.RED + "You have already used your free stat reset for your character.");
                        player.sendMessage(ChatColor.YELLOW + "You may purchase more resets from the E-Cash vendor!");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You need to be level 10 to use your ONE reset.");
                }
                break;
            case "Toggles":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

                PlayerManager.PlayerToggles playerToggle = PlayerManager.PlayerToggles.getByCommandName(event.getCurrentItem().getItemMeta().getDisplayName().substring(3));
                if (playerToggle != null) {
                    playerToggle.setToggleState(player, !(boolean) DatabaseAPI.getInstance().getData(playerToggle.getDbField(), player.getUniqueId()));
                    PlayerMenus.openToggleMenu(player);
                } else {
                    PlayerMenus.openPlayerProfileMenu(player);
                }

                break;
            case "Item Vendor":
            case "Food Vendor":
                if (event.isShiftClick()) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getRawSlot() >= 18) {
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                ItemStack stack = event.getCurrentItem();
                if (stack == null || stack.getType() == Material.AIR) return;
                net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                if (!nms.getTag().hasKey("worth")) {
                    event.setCancelled(true);
                    return;
                }
                int price = nms.getTag().getInt("worth");
                if (BankMechanics.getInstance().takeGemsFromInventory(price, player)) {
                    ItemStack copy = stack.clone();
                    ArrayList<String> lore = new ArrayList<>();
                    ItemMeta meta = copy.getItemMeta();
                    if (meta.hasLore()) {
                        lore = (ArrayList<String>) meta.getLore();
                    }
                    for (int i = 0; i < lore.size(); i++) {
                        String current = lore.get(i);
                        if (current.contains("Price")) {
                            lore.remove(i);
                            break;
                        }
                    }
                    meta.setLore(lore);
                    copy.setItemMeta(meta);
                    player.getInventory().addItem(copy);
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have " + ChatColor.GREEN + price + "g");
                }
                break;
            case "Mount Skin Selection":
                event.setCancelled(true);
                if (event.getCurrentItem().getType() == Material.BARRIER) {
                    PlayerMenus.openPlayerProfileMenu(player);
                    return;
                }
                if (event.getCurrentItem().getType() == Material.ARMOR_STAND) {
                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                        Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                        if (entity.isAlive()) {
                            entity.getBukkitEntity().remove();
                        }
                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                        }
                        EntityAPI.removePlayerMountList(player.getUniqueId());
                        player.sendMessage(ChatColor.AQUA + "Mount skin removed. Please re-summon your mount.");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "Mount skin removed.");
                    }
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT_SKIN, "", true);
                    return;
                }
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && event.getCurrentItem().getType() != Material.BARRIER && event.getCurrentItem().getType() != Material.ARMOR_STAND) {
                    if (EntityAPI.hasMountOut(player.getUniqueId())) {
                        Entity entity = EntityAPI.getPlayerMount(player.getUniqueId());
                        if (entity.isAlive()) {
                            entity.getBukkitEntity().remove();
                        }
                        if (DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.containsKey(entity)) {
                            DonationEffects.getInstance().ENTITY_PARTICLE_EFFECTS.remove(entity);
                        }
                        EntityAPI.removePlayerMountList(player.getUniqueId());
                    }
                    net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(event.getCurrentItem());
                    if (nmsStack.getTag() == null || nmsStack.getTag().getString("skinType") == null) {
                        player.sendMessage("Uh oh... Something went wrong with your mount skin! Please inform a staff member! [NBTTag]");
                        player.closeInventory();
                        return;
                    }
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_MOUNT_SKIN, nmsStack.getTag().getString("skinType"), true);
                    player.sendMessage(ChatColor.AQUA + "Mount skin set. Please re-summon your mount.");
                }
                break;
            case "Achievements":
                event.setCancelled(true);
                if (slot > 9) return;
                switch (slot) {
                    case 0:
                        PlayerMenus.openPlayerProfileMenu(player);
                        break;
                    case 2:
                        PlayerMenus.openExplorationAchievementMenu(player);
                        break;
                    case 3:
                        PlayerMenus.openCombatAchievementMenu(player);
                        break;
                    case 4:
                        PlayerMenus.openCharacterAchievementMenu(player);
                        break;
                    case 5:
                        PlayerMenus.openCurrencyAchievementMenu(player);
                        break;
                    case 6:
                        PlayerMenus.openSocialAchievementMenu(player);
                        break;
                    case 7:
                        PlayerMenus.openRealmAchievementMenu(player);
                        break;
                    default:
                        break;
                }
                break;
            case "Exploration Achievements":
                event.setCancelled(true);
                if (slot == 0) {
                    PlayerMenus.openPlayerAchievementsMenu(player);
                    return;
                }
                break;
            case "Character Achievements":
                event.setCancelled(true);
                if (slot == 0) {
                    PlayerMenus.openPlayerAchievementsMenu(player);
                    return;
                }
                break;
            case "Social Achievements":
                event.setCancelled(true);
                if (slot == 0) {
                    PlayerMenus.openPlayerAchievementsMenu(player);
                    return;
                }
                break;
            case "Combat Achievements":
                event.setCancelled(true);
                if (slot == 0) {
                    PlayerMenus.openPlayerAchievementsMenu(player);
                    return;
                }
                break;
            case "Currency Achievements":
                event.setCancelled(true);
                if (slot == 0) {
                    PlayerMenus.openPlayerAchievementsMenu(player);
                    return;
                }
                break;
            case "Realm Achievements":
                event.setCancelled(true);
                if (slot == 0) {
                    PlayerMenus.openPlayerAchievementsMenu(player);
                    return;
                }
                break;


            // CUSTOMER SUPPORT @todo: Move to own class to clean up & take advantage of own methods for reusing vars.
            case "Support Tools":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    break;

                NBTTagCompound tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                String playerName = tag.getString("name");
                UUID uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) break;

                switch (slot) {
                    case 4:
                        break;
                    case 19:
                        if (!playerName.equalsIgnoreCase(player.getDisplayName())) {
                            SupportMenus.openRankMenu(player, playerName, uuid);
                        } else {
                            player.sendMessage(ChatColor.RED + "You " + ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "CANNOT" + ChatColor.RED + " change the rank of your own account.");
                            player.closeInventory();
                        }
                        break;
                    case 22:
                        SupportMenus.openLevelMenu(player, playerName, uuid);
                        break;
                    case 25:
                        SupportMenus.openECashMenu(player, playerName, uuid);
                        break;
                    case 28:
                        SupportMenus.openBankMenu(player, playerName, uuid);
                        break;
                    case 31:
                        SupportMenus.openHearthstoneMenu(player, playerName, uuid);
                        break;
                    case 34:
                        SupportMenus.openCosmeticsMenu(player, playerName, uuid);
                        break;

                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        break;
                }
                break;
            case "Support Tools (Rank)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;
                // Only developers can apply certain ranks (GM / Builder / Support)
                if (!Rank.isDev(player) && (slot == 29 || slot == 30 || slot == 32 || slot == 33)) return;

                String newRank;

                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 20:
                        newRank = "DEFAULT";
                        break;
                    case 21:
                        newRank = "SUB";
                        break;
                    case 22:
                        newRank = "SUB+";
                        break;
                    case 23:
                        newRank = "SUB++";
                        break;
                    case 24:
                        newRank = "PMOD";
                        break;
                    case 29:
                        newRank = "BUILDER";
                        break;
                    case 30:
                        newRank = "YOUTUBE";
                        break;
                    case 32:
                        newRank = "SUPPORT";
                        break;
                    case 33:
                        newRank = "GM";
                        break;
                    default:
                        return;
                }

                // Always update the database with the new rank.
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, newRank, true);

                if (Bukkit.getPlayer(playerName) != null) {
                    Rank.getInstance().setRank(uuid, newRank);
                } else {
                    API.updatePlayerData(uuid);
                }

                player.sendMessage(ChatColor.GREEN + "Successfully set the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + newRank + ChatColor.GREEN + ".");
                SupportMenus.openMainMenu(player, playerName);
                break;
            case "Support Tools (Level)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                String levelType = "add";
                String variableName = "experience";
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 21:
                        levelType = "add";
                        variableName = "experience";
                        break;
                    case 22:
                        levelType = "set";
                        variableName = "experience";
                        break;
                    case 23:
                        levelType = "remove";
                        variableName = "experience";
                        break;
                    case 30:
                        levelType = "add";
                        variableName = "level";
                        break;
                    case 31:
                        levelType = "set";
                        variableName = "level";
                        break;
                    case 32:
                        levelType = "remove";
                        variableName = "level";
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                player.sendMessage(ChatColor.YELLOW + "Please enter the amount you would to " + levelType + ":");
                player.closeInventory();
                final String customLevelType = levelType;
                final String finalVariableName = variableName;
                Chat.listenForMessage(player, customAmount -> {
                    if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                        try {
                            if (finalVariableName == "level") {
                                Support.modifyLevel(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customLevelType);
                            } else {
                                Support.modifyExp(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customLevelType);
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                        }
                    }
                }, null);

                break;
            case "Support Tools (E-Cash)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                boolean customInput = false;
                String ecashType = "add";
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 21:
                        customInput = true;
                        ecashType = "add";
                        break;
                    case 22:
                        customInput = true;
                        ecashType = "set";
                        break;
                    case 23:
                        customInput = true;
                        ecashType = "remove";
                        break;
                    case 29:
                        Support.modifyEcash(player, playerName, uuid, 500, "add");
                        break;
                    case 30:
                        Support.modifyEcash(player, playerName, uuid, 2500, "add");
                        break;
                    case 32:
                        Support.modifyEcash(player, playerName, uuid, 5000, "add");
                        break;
                    case 33:
                        Support.modifyEcash(player, playerName, uuid, 9999, "add");
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                if (customInput) {
                    player.sendMessage(ChatColor.YELLOW + "Please enter the amount you would to " + ecashType + ":");
                    player.closeInventory();
                    final String customEcashType = ecashType;
                    Chat.listenForMessage(player, customAmount -> {
                        if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                            try {
                                Support.modifyEcash(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customEcashType);
                            } catch (NumberFormatException e) {
                                player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                            }
                        }
                    }, null);
                }
                break;
            case "Support Tools (Bank)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                String bankType = "add";
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 21:
                        bankType = "add";
                        break;
                    case 22:
                        bankType = "set";
                        break;
                    case 23:
                        bankType = "remove";
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                player.sendMessage(ChatColor.YELLOW + "Please enter the amount you would to " + bankType + ":");
                player.closeInventory();
                final String customBankType = bankType;
                Chat.listenForMessage(player, customAmount -> {
                    if (!customAmount.getMessage().equalsIgnoreCase("cancel") && !customAmount.getMessage().equalsIgnoreCase("exit")) {
                        try {
                            Support.modifyGems(player, playerName, uuid, Integer.parseInt(customAmount.getMessage()), customBankType);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + customAmount.getMessage() + " is not a valid number.");
                        }
                    }
                }, null);

                break;
            case "Support Tools (Hearthstone)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                String hearthstoneLocation = null;
                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 18:
                        hearthstoneLocation = "Cyrennica";
                        break;
                    case 19:
                        hearthstoneLocation = "Harrison_Field";
                        break;
                    case 20:
                        hearthstoneLocation = "Dark_Oak";
                        break;
                    case 21:
                        hearthstoneLocation = "Trollsbane";
                        break;
                    case 22:
                        hearthstoneLocation = "Tripoli";
                        break;
                    case 23:
                        hearthstoneLocation = "Gloomy_Hollows";
                        break;
                    case 24:
                        hearthstoneLocation = "Crestguard";
                        break;
                    case 25:
                        hearthstoneLocation = "Deadpeaks";
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }

                if (hearthstoneLocation != null) {
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEARTHSTONE, hearthstoneLocation.toUpperCase(), true);
                    player.sendMessage(ChatColor.GREEN + "Successfully set the hearthstone of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + Utils.ucfirst(hearthstoneLocation).replace("_", " ") + ChatColor.GREEN + ".");
                    API.updatePlayerData(uuid);
                    SupportMenus.openMainMenu(player, playerName);
                }
                break;
            case "Support Tools (Cosmetics)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 19:
                        SupportMenus.openTrailsMenu(player, playerName, uuid);
                        break;
                    case 22:
                        SupportMenus.openMountsMenu(player, playerName, uuid);
                        break;
                    case 25:
                        SupportMenus.openPetsMenu(player, playerName, uuid);
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }
                break;
            case "Support Tools (Trails)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 0: // @todo: change me
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }
                break;
            case "Support Tools (Mounts)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 0: // @todo: change me
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }
                break;
            case "Support Tools (Pets)":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || !Rank.isSupport(player))
                    return;

                tag = CraftItemStack.asNMSCopy(event.getCurrentItem()).getTag();
                playerName = tag.getString("name");
                uuid = UUID.fromString(tag.getString("uuid"));

                // Only continue if the playerName & uuid aren't empty.
                if (playerName.isEmpty() || uuid.toString().isEmpty()) return;

                switch (slot) {
                    case 4:
                        SupportMenus.openMainMenu(player, playerName);
                        return;
                    case 0: // @todo: change me
                        break;
                    default:
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Uh oh!" + ChatColor.BLUE + " This feature is coming soon....");
                        return;
                }
                break;
            default:
                break;
            case "Game Master Toggles":
                event.setCancelled(true);
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

                switch (slot) {
                    case 0: // Invisible
                        if (API._hiddenPlayers.contains(player)) {
                            API._hiddenPlayers.remove(player);
                            for (Player player1 : Bukkit.getOnlinePlayers()) {
                                if (player1.getUniqueId().toString().equals(player.getUniqueId().toString())) {
                                    continue;
                                }
                                player1.showPlayer(player);
                            }
                            player.removePotionEffect(PotionEffectType.INVISIBILITY);
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You are now visible.");
                            player.setCustomNameVisible(true);
                            player.setGameMode(GameMode.SURVIVAL);
                        } else {
                            API._hiddenPlayers.add(player);
                            player.setCustomNameVisible(false);
                            player.hidePlayer(player);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
                            player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "You are now hidden.");
                            player.setGameMode(GameMode.SPECTATOR);
                        }
                        break;

                    case 1:
                        player.sendMessage(ChatColor.RED + "Coming soon."); // @todo: Alan - change this to toggle fight enabled mode.
                        break;

                    default:
                        break;
                }

                PlayerMenus.openGameMasterTogglesMenu(player);
                break;
        }
    }
}
