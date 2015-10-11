package net.dungeonrealms.handlers;

import net.dungeonrealms.DungeonRealms;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Created by Kieran on 9/24/2015.
 */
public class EnergyHandler {

    private static EnergyHandler instance = null;

    public static EnergyHandler getInstance() {
        if (instance == null) {
            instance = new EnergyHandler();
        }
        return instance;
    }

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::regenerateAllPlayerEnergy, 40, 3L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::removePlayerEnergySprint, 40, 9L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::addStarvingPotionEffect, 40, 15L);
    }

    /**
     * Handles players logging out,
     * removes metadata from the player
     *
     * @param player
     * @since 1.0
     */
    public void handleLogoutEvents(Player player) {
        if (player.hasMetadata("starving")) {
            player.removeMetadata("starving", DungeonRealms.getInstance());
        }
        if (player.hasMetadata("sprinting")) {
            player.removeMetadata("sprinting", DungeonRealms.getInstance());
        }
    }

    /**
     * Handles players logging in,
     * adds metadata to the player if
     * applicable (no food level).
     *
     * @param player
     * @since 1.0
     */
    public void handleLoginEvents(Player player) {
        if (player.getFoodLevel() <= 0) {
            if (!(player.hasMetadata("starving"))) {
                player.setMetadata("starving", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "**STARVING**"), 20L);
            }
        }
    }

    /**
     * Handles the regeneration of energy
     * for all players applicable on the
     * server.
     *
     * @since 1.0
     */
    private void regenerateAllPlayerEnergy() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getPlayerCurrentEnergy(player.getUniqueId()) == 1.0F) {
                continue;
            }
            if (getPlayerCurrentEnergy(player.getUniqueId()) > 1.0F) {
                player.setExp(1.0F);
                updatePlayerEnergyBar(player.getUniqueId());
                continue;
            }
            float regenAmount = getPlayerEnergyRegenerationAmount(player.getUniqueId());
            if (!(player.hasPotionEffect(PotionEffectType.SLOW_DIGGING))) {
                if (player.hasMetadata("starving")) {
                    regenAmount = 0.07F;
                }
                regenAmount = regenAmount / 6.3F;
                //TODO: FISH SOMETHING GIVES REGEN I DUNNO PROFESSION MECHANICS.
                addEnergyToPlayerAndUpdate(player.getUniqueId(), regenAmount);
            }
        }
    }

    /**
     * Returns the players current
     * energy value
     *
     * @param uuid
     * @return float
     * @since 1.0
     */
    public static float getPlayerCurrentEnergy(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player.getExp();
    }

    /**
     * Updates the players Minecraft EXP bar
     * with our custom energy values
     *
     * @param uuid
     * @since 1.0
     */
    private static void updatePlayerEnergyBar(UUID uuid) {
        float currExp = getPlayerCurrentEnergy(uuid);
        double percent = currExp * 100.00D;
        if (percent > 100) {
            percent = 100;
        }
        if (percent < 0) {
            percent = 0;
        }
        Bukkit.getPlayer(uuid).setLevel(((int) percent));
    }

    /**
     * Returns the players current
     * energy regeneration value
     *
     * @param uuid
     * @return float
     * @since 1.0
     */
    private float getPlayerEnergyRegenerationAmount(UUID uuid) {
        float regenAmount = 0.15F;
        Player player = Bukkit.getPlayer(uuid);
        EntityEquipment playerEquipment = player.getEquipment();
        ItemStack[] playerArmor = playerEquipment.getArmorContents();
        NBTTagCompound nmsTags[] = new NBTTagCompound[4];
        if (playerArmor[3].getType() != null && playerArmor[3].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[3]).getTag() != null) {
                nmsTags[0] = CraftItemStack.asNMSCopy(playerArmor[3]).getTag();
            }
        }
        if (playerArmor[2].getType() != null && playerArmor[2].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[2]).getTag() != null) {
                nmsTags[1] = CraftItemStack.asNMSCopy(playerArmor[2]).getTag();
            }
        }
        if (playerArmor[1].getType() != null && playerArmor[1].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[1]).getTag() != null) {
                nmsTags[2] = CraftItemStack.asNMSCopy(playerArmor[1]).getTag();
            }
        }
        if (playerArmor[0] != null && playerArmor[0].getType() != Material.AIR) {
            if (CraftItemStack.asNMSCopy(playerArmor[0]).getTag() != null) {
                nmsTags[3] = CraftItemStack.asNMSCopy(playerArmor[0]).getTag();
            }
        }
        for (NBTTagCompound nmsTag : nmsTags) {
            if (nmsTag == null) {
                regenAmount += 0;
            } else {
                if (nmsTag.getInt("energyRegen") != 0) {
                    regenAmount += (regenAmount / 100.F) * (nmsTag.getInt("energyRegen") + 2);
                }
                if (nmsTag.getInt("intellect") != 0) {
                    regenAmount += ((nmsTag.getInt("intellect") * 0.015F) / 100.0F);
                }
            }
        }
        return regenAmount;
    }

    /**
     * Adds energy to the defined player
     *
     * @param uuid
     * @param amountToAdd
     * @since 1.0
     */
    private static void addEnergyToPlayerAndUpdate(UUID uuid, float amountToAdd) {
        if (getPlayerCurrentEnergy(uuid) == 1) {
            return;
        }
        Bukkit.getPlayer(uuid).setExp(Bukkit.getPlayer(uuid).getExp() + amountToAdd);
        updatePlayerEnergyBar(uuid);
    }

    /**
     * Handles the removal of energy while
     * players are sprinting
     *
     * @return float
     * @since 1.0
     */
    private void removePlayerEnergySprint() {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.isSprinting() || player.hasMetadata("sprinting")).forEach(player -> {
            removeEnergyFromPlayerAndUpdate(player.getUniqueId(), 0.135F);
            if (getPlayerCurrentEnergy(player.getUniqueId()) <= 0 || player.hasMetadata("starving")) {
                int playerFood = player.getFoodLevel();
                player.setSprinting(false);
                player.removeMetadata("sprinting", DungeonRealms.getInstance());
                if (!player.hasPotionEffect(PotionEffectType.SLOW)) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 10)), 0L);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "**EXHAUSTED**");
                }
                player.setFoodLevel(1);
                player.setFoodLevel(playerFood);
                //TODO: THIS IS A SUPER SKETCHY WAY OF PREVENTING LEFT-CONTROL SPRINTING FROM OVERRIDING. As its CLIENTSIDE setSprinting(false) only cancels it for one tick
                //TODO: Since this is a plugin and not a mod, we can't toggle keypresses clientside. RIP.
            }
        });
    }

    /**
     * Handles the removal of energy from
     * a player and updates their bar.
     * Used when auto-attacking etc.
     *
     * @param uuid
     * @param amountToRemove
     * @since 1.0
     */
    public static void removeEnergyFromPlayerAndUpdate(UUID uuid, float amountToRemove) {
        Player player = Bukkit.getPlayer(uuid);
        if (player.isOp()) return;
        if (player.hasMetadata("last_energy_remove")) {
            if ((System.currentTimeMillis() - player.getMetadata("last_energy_remove").get(0).asLong()) < 100) {
                return;
            }
        }
        player.setMetadata("last_energy_remove", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        if (getPlayerCurrentEnergy(uuid) <= 0) return;
        if ((getPlayerCurrentEnergy(uuid) - amountToRemove) <= 0) {
            player.setExp(0.0F);
            updatePlayerEnergyBar(uuid);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 50, 4)), 0L);
            return;
        }
        player.setExp((getPlayerCurrentEnergy(uuid) - amountToRemove));
        updatePlayerEnergyBar(uuid);
    }

    /**
     * Adds the hunger potion effect
     * to a player and "starving" as
     * metadata when they have 0 food level.
     *
     * @since 1.0
     */
    private void addStarvingPotionEffect() {
        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPotionEffect(PotionEffectType.HUNGER) && player.hasMetadata("starving")).forEach(player -> {
            if (player.getFoodLevel() <= 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0)), 0L);
            } else {
                player.removeMetadata("starving", DungeonRealms.getInstance());
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> player.removePotionEffect(PotionEffectType.HUNGER), 0L);
            }
        });
    }

    /**
     * Returns the energy cost
     * of an item
     *
     * @param itemStack
     * @return float
     * @since 1.0
     */
    public static float getWeaponSwingEnergyCost(ItemStack itemStack) {
        Material material = itemStack.getType();

        switch (material) {
            case AIR:
                return 0.045F;
            case WOOD_SWORD:
                return 0.054F;
            case STONE_SWORD:
                return 0.064F;
            case IRON_SWORD:
                return 0.075F;
            case DIAMOND_SWORD:
                return 0.113F;
            case GOLD_SWORD:
                return 0.123F;
            case WOOD_AXE:
                return 0.07062F;
            case STONE_AXE:
                return 0.08393F;
            case IRON_AXE:
                return 0.099F;
            case DIAMOND_AXE:
                return 0.1243F;
            case GOLD_AXE:
                return 0.1353F;
            case WOOD_SPADE:
                return 0.0642F;
            case STONE_SPADE:
                return 0.0763F;
            case IRON_SPADE:
                return 0.09F;
            case DIAMOND_SPADE:
                return 0.113F;
            case GOLD_SPADE:
                return 0.123F;
            case WOOD_HOE:
                return 0.11F;
            case STONE_HOE:
                return 0.12F;
            case IRON_HOE:
                return 0.13F;
            case DIAMOND_HOE:
                return 0.14F;
            case GOLD_HOE:
                return 0.15F;
            case BOW:
                return 0.093F;
            default:
                return 0.1F;
        }
    }
}