package net.dungeonrealms.game.mechanics;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.world.items.EnumItem;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nick on 9/18/2015.
 */
public class ItemManager {
    /**
     * returns hearthstone
     *
     * @param name
     * @param lore
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createHearthStone(String name, String[] lore) {
        ItemStack rawStack = new ItemStack(Material.QUARTZ);
        ItemMeta meta = rawStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("hearthstone"));
        nmsStack.setTag(tag);
    	return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }
    
    public static ItemStack createOrbofAlteration(){
    	ItemStack rawStack = createItem(Material.MAGMA_CREAM, ChatColor.LIGHT_PURPLE.toString() + "Orb of Alteration", new String[]{(ChatColor.GRAY.toString() + "Randomizes bonus stats of selected equipment")});
    	net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
    	nms.getTag().setString("type", "orb");
    	return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }
    
    public static ItemStack createWeaponEnchant(int tier){
    	String material = getMatString(tier);
    	ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() +ChatColor.BOLD.toString() + "Scroll: " + API.getTierColor(tier) + "Enchant "+material + " Weapon" , new String[]{ChatColor.RED + "+5% DMG", ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Weapon will VANISH if enchant above +3 FAILS."});
    	net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
    	nms.getTag().setString("type", "weaponenchant");
    	nms.getTag().setInt("tier", tier);
    	return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }
    
    public static ItemStack createArmorEnchant(int tier){
    	String material = getMatString(tier);
    	ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() +ChatColor.BOLD.toString() + "Scroll: " + API.getTierColor(tier) + "Enchant "+ material + " Armor" , new String[]{ChatColor.RED + "+5% HP",ChatColor.RED + "+5% HP REGEN", ChatColor.GRAY.toString() + ChatColor.ITALIC + "    - OR -", ChatColor.RED + "+1% ENERGY REGEN" ,ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "Armor will VANISH if enchant above +3 FAILS."});
    	net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
    	nms.getTag().setString("type", "armorenchant");
    	nms.getTag().setInt("tier", tier);
    	return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }
    
    /**
	 * @param tier
	 * @return
	 */
	private static String getMatString(int tier) {
		switch(tier){
		case 1:
			return "Leather";
		case 2:
			return "Chainmail";
		case 3: 
			return "Iron";
		case 4:
			return "Diamond";
		case 5:
			return "Gold";
		}
		return null;
	}

	public static ItemStack createProtectScroll(int tier){
    	String material = getMatString(tier);
    	ItemStack rawStack = createItem(Material.EMPTY_MAP, ChatColor.WHITE.toString() +ChatColor.BOLD.toString() + "White Scroll: " + API.getTierColor(tier) + "Protect " + material + " Equipment" , new String[]{
    			ChatColor.GRAY.toString() + "Apply to any T" + tier + " item to " + ChatColor.UNDERLINE + " prevent " + ChatColor.GRAY + " it from being destroyed if the next enchantment scroll fails."});
    	net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(rawStack);
    	nms.getTag().setString("type", "protection");
    	nms.getTag().setInt("tier", tier);
    	return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }
    
    /**
     * Creates a random Teleport book
     *
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createRandomTeleportBook() {
        ItemStack rawStack = new ItemStack(Material.BOOK);
        ItemMeta meta = rawStack.getItemMeta();
        String teleportLocation = TeleportAPI.getRandomTeleportString();
        String displayName = TeleportAPI.getDisplayNameOfLocation(teleportLocation);
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + teleportLocation);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "(Right-Click) Teleport to " + displayName));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("teleport"));
        tag.set("usage", new NBTTagString(teleportLocation));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Creates a Teleport book using location for a String
     *
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createTeleportBook(String location) {
        ItemStack rawStack = new ItemStack(Material.BOOK);
        ItemMeta meta = rawStack.getItemMeta();
        String displayName = TeleportAPI.getDisplayNameOfLocation(location);
        meta.setDisplayName(ChatColor.WHITE.toString() + ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + location);
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "(Right-Click) Teleport to " + displayName));
        rawStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("teleport"));
        tag.set("usage", new NBTTagString(location));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }
    
    /**
     * Creates a scrap piece based on
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createArmorScrap(int tier) {
        ItemStack rawStack = null;
        String name = "";
        switch (tier) {
            case 1:
                rawStack = new ItemStack(Material.LEATHER, 64);
                name = ChatColor.WHITE + "Leather";
                break;
            case 2:
                rawStack = new ItemStack(Material.IRON_FENCE, 64);
                name = ChatColor.GREEN+ "Chain";
                break;
            case 3:
                rawStack = new ItemStack(Material.IRON_INGOT, 64);
                name = ChatColor.AQUA + "Iron";
                break;
            case 4:
                rawStack = new ItemStack(Material.DIAMOND, 64);
                name = ChatColor.LIGHT_PURPLE + "Diamond";
                break;
            case 5:
                rawStack = new ItemStack(Material.GOLD_INGOT, 64);
                name = ChatColor.YELLOW + "Gold";
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name + " Scrap");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Repairs 3% durability on " + name + ChatColor.GRAY + " equipment."));
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("scrap"));
            tag.setInt("itemTier", tier);
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return null;
    }

    /**
     * Creates a potion based on the
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createHealthPotion(int tier, boolean fromShop, boolean isSplashPotion) {
        String name = "";
        int healAmount = 0;
        switch (tier) {
            case 1:
                if (!isSplashPotion) {
                    name = ChatColor.WHITE + "Poor Elixir of Singular Healing";
                } else {
                    name = ChatColor.WHITE + "Poor Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(30, 100);
                } else {
                    healAmount = 75;
                }
                break;
            case 2:
                if (!isSplashPotion) {
                    name = ChatColor.GREEN + "Inferior Elixir of Singular Healing";
                } else {
                    name = ChatColor.GREEN + "Inferior Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(270, 375);
                } else {
                    healAmount = 325;
                }
                break;
            case 3:
                if (!isSplashPotion) {
                    name = ChatColor.AQUA + "Modest Elixir of Singular Healing";
                } else {
                    name = ChatColor.AQUA + "Modest Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(800, 1000);
                } else {
                    healAmount = 900;
                }
                break;
            case 4:
                if (!isSplashPotion) {
                    name = ChatColor.LIGHT_PURPLE + "Superior Elixir of Singular Healing";
                } else {
                    name = ChatColor.LIGHT_PURPLE + "Superior Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(2000, 2400);
                } else {
                    healAmount = 2250;
                }
                break;
            case 5:
                if (!isSplashPotion) {
                    name = ChatColor.YELLOW + "Legendary Elixir of Singular Healing";
                } else {
                    name = ChatColor.YELLOW + "Legendary Elixir of Splash Healing";
                }
                if (!fromShop) {
                    healAmount = RandomHelper.getRandomNumberBetween(3700, 4300);
                } else {
                    healAmount = 4000;
                }
                break;
            default:
                break;
        }
        if (!fromShop) {
            healAmount = (((healAmount + 5) / 10) * 10);
        }
        if (!isSplashPotion) {
            ItemStack rawStack = new ItemStack(Material.POTION, 1, (short) 5);
            PotionMeta potionMeta = (PotionMeta) rawStack.getItemMeta();
            potionMeta.setDisplayName(name);
            potionMeta.setLore(Collections.singletonList(ChatColor.GRAY + "An Elixir that heals for " + ChatColor.RED + ChatColor.BOLD + healAmount + ChatColor.GRAY + "HP."));
            rawStack.setItemMeta(potionMeta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("healthPotion"));
            tag.setInt("itemTier", tier);
            tag.setInt("healAmount", healAmount);
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        } else {
            healAmount *= 0.65;
            Potion potion = new Potion(PotionType.INSTANT_HEAL, 1);
            potion.setSplash(true);
            ItemStack rawStack = potion.toItemStack(1);
            PotionMeta potionMeta = (PotionMeta) rawStack.getItemMeta();
            potionMeta.setDisplayName(name);
            potionMeta.setLore(Collections.singletonList(ChatColor.GRAY + "An Elixir that heals for " + ChatColor.RED + ChatColor.BOLD + healAmount + ChatColor.GRAY + "HP in a " + ChatColor.RED + ChatColor.BOLD + "4x4" + ChatColor.GRAY + " Area."));
            rawStack.setItemMeta(potionMeta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("splashHealthPotion"));
            tag.setInt("itemTier", tier);
            tag.setInt("healAmount", healAmount);
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }
    }

    public static ItemStack createHealingFood(int tier, Item.ItemModifier modifier) {
        ItemStack rawStack = null;
        String name = "";
        String description = "";
        int healAmount = 0;
        switch (tier) {
            case 1:
                switch (modifier) {
                    case COMMON:
                        name = ChatColor.WHITE + "Plowed Potato";
                        description = ChatColor.GRAY + "The staple crop of Andulucia.";
                        healAmount = 9;
                        rawStack = new ItemStack(Material.POTATO_ITEM, 1);
                        break;
                    case RARE:
                        name = ChatColor.WHITE + "Loaded Potato Skin";
                        description = ChatColor.GRAY + "Extremely Tasty.";
                        healAmount = 16;
                        rawStack = new ItemStack(Material.BAKED_POTATO, 1);
                        break;
                    case LEGENDARY:
                        name = ChatColor.WHITE + "Fresh Apple";
                        description = ChatColor.GRAY + "Fresh from the local Apple Tree.";
                        healAmount = 25;
                        rawStack = new ItemStack(Material.APPLE, 1);
                        break;
                }
                break;
            case 2:
                switch (modifier) {
                    case COMMON:
                        name = ChatColor.GREEN + "Uncooked Chicken";
                        description = ChatColor.GRAY + "This may or may not be safe to eat...";
                        healAmount = 42;
                        rawStack = new ItemStack(Material.RAW_CHICKEN, 1);
                        break;
                    case RARE:
                        name = ChatColor.GREEN + "Roast Chicken";
                        description = ChatColor.GRAY + "Warm and toasty. Delicious too.";
                        healAmount = 55;
                        rawStack = new ItemStack(Material.COOKED_CHICKEN, 1);
                        break;
                    case LEGENDARY:
                        name = ChatColor.GREEN + "Pumpkin Pie";
                        description = ChatColor.GRAY + "The spookiest meal you'll ever eat.";
                        healAmount = 70;
                        rawStack = new ItemStack(Material.PUMPKIN_PIE, 1);
                        break;
                }
                break;
            case 3:
                switch (modifier) {
                    case COMMON:
                        name = ChatColor.AQUA + "Salted Pork";
                        description = ChatColor.GRAY + "Bringing in the bacon.";
                        healAmount = 90;
                        rawStack = new ItemStack(Material.PORK, 1);
                        break;
                    case RARE:
                        name = ChatColor.AQUA + "Seasoned Pork";
                        description = ChatColor.GRAY + "Bacon. Except tastier (is that possible?).";
                        healAmount = 150;
                        rawStack = new ItemStack(Material.GRILLED_PORK, 1);
                        break;
                    case LEGENDARY:
                        name = ChatColor.AQUA + "Mushroom Soup";
                        description = ChatColor.GRAY + "I hope these are the correct mushrooms.";
                        healAmount = 190;
                        rawStack = new ItemStack(Material.MUSHROOM_SOUP, 1);
                        break;
                }
                break;
            case 4:
                switch (modifier) {
                    case COMMON:
                        name = ChatColor.LIGHT_PURPLE + "Frozen Steak";
                        description = ChatColor.GRAY + "Stop complaining. Your dog would love this.";
                        healAmount = 300;
                        rawStack = new ItemStack(Material.RAW_BEEF, 1);
                        break;
                    case RARE:
                        name = ChatColor.LIGHT_PURPLE + "Rare Sizzling Steak";
                        description = ChatColor.GRAY + "Real men take their steaks rare.";
                        healAmount = 400;
                        rawStack = new ItemStack(Material.COOKED_BEEF, 1);
                        break;
                    case LEGENDARY:
                        name = ChatColor.LIGHT_PURPLE + "Grilled Rabbit";
                        description = ChatColor.GRAY + "Aww, look at the cute little bunny.";
                        healAmount = 500;
                        rawStack = new ItemStack(Material.COOKED_MUTTON, 1);
                        break;
                }
                break;
            case 5:
                switch (modifier) {
                    case COMMON:
                        name = ChatColor.YELLOW + "King's Apple";
                        description = ChatColor.GRAY + "A meal fit for a King.";
                        healAmount = 700;
                        rawStack = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 0);
                        break;
                    case RARE:
                        name = ChatColor.YELLOW + "Enchanted King's Apple";
                        description = ChatColor.GRAY + "A powerful King's battle snack.";
                        healAmount = 1000;
                        rawStack = new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1);
                        break;
                    case LEGENDARY:
                        name = ChatColor.YELLOW + "Golden Carrot";
                        description = ChatColor.GRAY + "Now this is just a waste of useful gold ore.";
                        healAmount = 1350;
                        rawStack = new ItemStack(Material.GOLDEN_CARROT, 1, (short) 0);
                        break;
                }
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name);
            List<String> itemLore = new ArrayList<>();
            itemLore.add(ChatColor.RED + "+" + ChatColor.BOLD + healAmount + "HP/s" + ChatColor.RED + " for " + ChatColor.BOLD + "15 " + ChatColor.RED + "Seconds.");
            itemLore.add(ChatColor.RED.toString() + ChatColor.BOLD + "Sprinting will cancel the effect!");
            itemLore.add(description);
            itemLore.add(modifier.getName());
            meta.setLore(itemLore);
            rawStack.setItemMeta(meta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("healingFood"));
            tag.setInt("itemTier", tier);
            tag.set("itemModifier", new NBTTagInt(modifier.getId()));
            tag.setInt("healAmount", healAmount);
            tag.set("AttributeModifiers", new NBTTagList());
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return null;
    }

    /**
     * Creates a pickaxe based on
     * given tier
     *
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack createPickaxe(int tier) {
        ItemStack rawStack = null;
        String name = "";
        ArrayList<String> lore = new ArrayList<>();
        
        String expBar = ChatColor.RED + "||||||||||" + "||||||||||" + "||||||||||";
        int lvl = Mining.getTierLvl(tier);
        lore.add(ChatColor.GRAY.toString() + "Level: " + ChatColor.WHITE.toString() + lvl);
        lore.add(ChatColor.WHITE.toString() + 0 + ChatColor.GRAY + "/" + ChatColor.GRAY + Mining.getEXPNeeded(lvl));
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);
        switch (tier) {
            case 1:
                rawStack = new ItemStack(Material.WOOD_PICKAXE);
                name = ChatColor.WHITE + "Novice Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of sturdy wood.");
                break;
            case 2:
                rawStack = new ItemStack(Material.STONE_PICKAXE);
                name = ChatColor.GREEN.toString() + "Expert Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of cave stone.");
                break;
            case 3:
                rawStack = new ItemStack(Material.IRON_PICKAXE);
                name = ChatColor.AQUA.toString() + "Expert Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of forged iron.");
                break;
            case 4:
                rawStack = new ItemStack(Material.DIAMOND_PICKAXE);
                name = ChatColor.LIGHT_PURPLE.toString() + "Supreme Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of hardened diamond.");
                break;
            case 5:
                rawStack = new ItemStack(Material.GOLD_PICKAXE);
                name = ChatColor.YELLOW.toString() + "Master Pickaxe";
                lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A pickaxe made out of reinforced gold.");
                break;
            default:
                break;
        }
        if (rawStack != null) {
            ItemMeta meta = rawStack.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(lore);
            rawStack.setItemMeta(meta);
            RepairAPI.setCustomItemDurability(rawStack, 1500);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
            NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
            tag.set("type", new NBTTagString("pick"));
            tag.setInt("itemTier", tier);
            tag.setInt("XP", 0);
            tag.setInt("maxXP", Mining.getEXPNeeded(lvl));
            tag.setInt("level", lvl);
            tag.set("AttributeModifiers", new NBTTagList());
            nmsStack.setTag(tag);
            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        }
        return null;
    }

    public static ItemStack createItem(EnumItem enumItem) {
        ItemStack stack = null;
        net.minecraft.server.v1_8_R3.ItemStack nms = null;
        switch (enumItem) {
            case StorageExpansion:
                stack = createItem(Material.ENDER_CHEST, ChatColor.GREEN + "Storage Expansion", new String[]{ChatColor.GRAY + "Increase storage space by 1 row.", ChatColor.RED.toString() + ChatColor.BOLD + "Max of 6"});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "upgrade");
                break;
            case RepairHammer:
                stack = createItem(Material.ANVIL, ChatColor.GREEN + "Repair Hammer", new String[]{ChatColor.GRAY + "Fully repair a single item."});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "repair");
                break;
            case RetrainingBook:
                stack = createItem(Material.ENCHANTED_BOOK, ChatColor.GREEN + "Retraining Book", new String[]{ChatColor.GRAY + "Right click to reset your stat", ChatColor.GRAY + "allocated points to free points."});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "reset");
                break;
            case MedalOfGathering:
                stack = createItem(Material.YELLOW_FLOWER, ChatColor.GREEN + "Medal of Gathering", new String[]{ChatColor.GRAY + "Increase storage space by 1 row." + ChatColor.RED + ChatColor.BOLD + "Max of 6"});
                nms = CraftItemStack.asNMSCopy(stack);
                nms.getTag().setString("type", "gathering");
                break;
            case CharacterJournal:

                break;
        }
        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nms));
    }


    public static ItemStack createFishingPole(int tier) {
        ItemStack rawStack = null;
        String name = "";
        ArrayList<String> lore = new ArrayList<>();
        rawStack = new ItemStack(Material.FISHING_ROD, 1);
        ItemMeta meta = rawStack.getItemMeta();
        String expBar = ChatColor.RED + "||||||||||" + "||||||||||" + "||||||||||";
        int lvl = Mining.getTierLvl(tier);
        lore.add(ChatColor.GRAY.toString() + "Level: " + ChatColor.WHITE.toString() + lvl);
        lore.add(ChatColor.WHITE.toString() + 0 + ChatColor.GRAY + "/" + ChatColor.GRAY + Fishing.getMaxXP(lvl));
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);
        lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "A fishing rod made of wood and thread.");

        switch (tier) {
            case 1:
                name = ChatColor.WHITE + "Novice Fishingrod";
                break;
            case 2:
                name = ChatColor.GREEN.toString() + "Basic Fishingrod";
                break;
            case 3:
                name = ChatColor.AQUA.toString() + "Advanced Fishingrod";
                break;
            case 4:
                name = ChatColor.LIGHT_PURPLE.toString() + "Expert Fishingrod";
                break;
            case 5:
                name = ChatColor.YELLOW.toString() + "Master Fishingrod";
                break;
            default:
                break;
        }
        meta.setDisplayName(name);
        meta.setLore(lore);
        rawStack.setItemMeta(meta);
        RepairAPI.setCustomItemDurability(rawStack, 1500);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("rod"));
        tag.setInt("itemTier", tier);
        tag.setInt("XP", 0);
        tag.setInt("maxXP", Fishing.getMaxXP(tier));
        nmsStack.setTag(tag);
        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }

    /**
     * @param m
     * @param name
     * @param lore
     * @return ItemStack
     */
    public static ItemStack createItem(Material m, String name, String[] lore) {
        ItemStack is = new ItemStack(m, 1);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return is;
    }

    /**
     * @param m
     * @param name
     * @param lore
     * @return ItemStack
     */
    public static ItemStack createItemWithData(Material m, String name, String[] lore, short i) {
        ItemStack is = new ItemStack(m, 1, i);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null)
            meta.setLore(Arrays.asList(lore));
        is.setItemMeta(meta);
        return is;
    }

    /**
     * Creates a Character Journal for p
     *
     * @return ItemStack
     */
    public static ItemStack createCharacterJournal(Player p) {
        ItemStack stack = createItem(Material.WRITTEN_BOOK, ChatColor.GREEN.toString() + ChatColor.BOLD + "Character Journal", new String[]{ChatColor.GREEN + "Left Click: " + ChatColor.GRAY + "Invite to Party", ChatColor.GREEN + "Sneak-Left Click: " + ChatColor.GRAY + "Setup Shop"});
        BookMeta bm = (BookMeta) stack.getItemMeta();
        List<String> pages = new ArrayList<>();
        String page1_string = "";
        String page2_string = "";
        String page3_string = "";
        String page4_string = "";
        String new_line = "\n" + ChatColor.WHITE.toString() + "`" + "\n";
        GamePlayer gp = API.getGamePlayer(p);
        String pretty_align = ChatColor.DARK_GREEN + ChatColor.UNDERLINE.toString() + gp.getPlayerAlignment().name();
        DecimalFormat df = new DecimalFormat("#.##");
        PlayerStats stats = gp.getStats();

        page1_string = ChatColor.BLACK.toString() + "" + ChatColor.BOLD.toString() + ChatColor.UNDERLINE.toString() + "  Your Character" + "\n" + new_line
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "Alignment: " + pretty_align + new_line
                + ChatColor.BLACK.toString() + gp.getPlayerAlignment().description + new_line + ChatColor.BLACK.toString() + "   " + gp.getPlayerCurrentHP()
                + " / " + gp.getPlayerMaxHP() + "" + ChatColor.BOLD.toString() + " HP" + "\n" + ChatColor.BLACK.toString()
                + "   " + Math.round(gp.getStats().getDPS()) + "% " + ChatColor.BOLD.toString() + "DPS" + "\n" + ChatColor.BLACK.toString()
                + "   " + (HealthHandler.getInstance().getPlayerHPRegenLive(p)) + " " + ChatColor.BOLD.toString() + "HP/s" + "\n" + ChatColor.BLACK.toString()
                + "   " + EnergyHandler.getInstance().getPlayerEnergyPercentage(p.getUniqueId()) + "% " + ChatColor.BOLD.toString() + "Energy/s" + "\n" + ChatColor.BLACK.toString()
                + "   " + DatabaseAPI.getInstance().getData(EnumData.ECASH, p.getUniqueId()) + ChatColor.BOLD.toString() + " E-CASH" + "\n" + ChatColor.BLACK.toString() 
                + "   " + gp.getPlayerLuck() + ChatColor.BOLD.toString() + " LUCK";

        page2_string = ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "  ** LEVEL/EXP **\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "       LEVEL\n" + "          " + ChatColor.BLACK + gp.getLevel() + "\n\n" + ChatColor.BLACK + ChatColor.BOLD
                + "          XP" + "\n" + ChatColor.BLACK + "       " + (int) gp.getExperience() + "/"
                + gp.getEXPNeeded(gp.getLevel());

        page3_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.strPoints + " Strength"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Warrior'" + "\n"
//                + ChatColor.BLACK.toString() + "+" + df.format("STR * 0.03") + "% Armor" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getBlock() * 100) + "% Block" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getAxeDMG() * 100) + "% Axe DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getPolearmDMG() * 100) + "% Polearm DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.dexPoints + " Dexterity" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Archer'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getDodge() * 100) + "% Dodge" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getBowDMG() * 100) + "% Bow DMG" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getCritChance() * 100) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getArmorPen() * 100) + "% Armor Pen.";

        page4_string = ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.vitPoints + " Vitality"
                + "\n" + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Defender'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getVitHP() * 100) + "% Health" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getHPRegen() * 100) + "   HP/s" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getSwordDMG() * 100) + "% Sword DMG" + "\n" + "\n"
                + ChatColor.BLACK.toString() + ChatColor.BOLD.toString() + "+ " + stats.intPoints + " Intellect" + "\n"
                + ChatColor.BLACK.toString() + "   " + ChatColor.UNDERLINE.toString() + "'The Mage'" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getEnergyRegen() * 100) + "% Energy" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getCritChance() * 100) + "% Critical Hit" + "\n"
                + ChatColor.BLACK.toString() + "+" + df.format(stats.getStaffDMG() * 100) + "% Staff DMG";


        bm.setAuthor("King Bulwark");
        pages.add(page1_string);
        pages.add(page2_string);
        pages.add(page3_string);
        pages.add(page4_string);
        bm.setPages(pages);
        stack.setItemMeta(bm);
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        nms.getTag().setString("type", "important");
        nms.getTag().setString("journal", "true");
        return CraftItemStack.asBukkitCopy(nms);
    }


    /**
     * returns playerProfile
     *
     * @param player
     * @param displayName
     * @param lore
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack getPlayerProfile(Player player, String displayName, String[] lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(displayName);
        meta.setLore(Arrays.asList(lore));
        skull.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(skull);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("important"));
        tag.set("usage", new NBTTagString("profile"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Remove the cost of gems from itemstacks lore.
     *
     * @param stack
     */
    public static ItemStack removePrice(ItemStack stack) {
        ItemStack item = stack.clone();
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                if (line.contains("Price") || line.contains("Gems")) {
                    lore.remove(i);
                    break;
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}