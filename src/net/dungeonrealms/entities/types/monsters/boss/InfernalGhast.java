package net.dungeonrealms.entities.types.monsters.boss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumBoss;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityGhast;

/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalGhast extends EntityGhast implements Boss {

	private InfernalAbyss boss;

	/**
	 * @param infernalAbyss
	 */
	public InfernalGhast(InfernalAbyss infernalAbyss) {
		super(infernalAbyss.getWorld());
		this.getBukkitEntity().setCustomNameVisible(true);
		int level = Utils.getRandomFromTier(getEnumBoss().tier);
		MetadataUtils.registerEntityMetadata(this, EnumEntityType.HOSTILE_MOB, getEnumBoss().tier, level);
		this.getBukkitEntity().setMetadata("boss",
		        new FixedMetadataValue(DungeonRealms.getInstance(), getEnumBoss().nameid));
		EntityStats.setBossRandomStats(this, level, getEnumBoss().tier);
		this.getBukkitEntity()
		        .setCustomName(ChatColor.RED.toString() + ChatColor.UNDERLINE.toString() + getEnumBoss().name);
		this.boss = infernalAbyss;
		int health = boss.getBukkitEntity().getMetadata("currentHP").get(0).asInt();
		int maxHealth = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt();
		this.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), health));
		this.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHealth));
	}

	public void init() {
		int health = boss.getBukkitEntity().getMetadata("currentHP").get(0).asInt();
		int maxHealth = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt();
		this.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), health));
//		HealthHandler.getInstance().setMonsterHPLive((LivingEntity) this, health);
		this.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHealth));
	}

	@Override
	public EnumBoss getEnumBoss() {
		return EnumBoss.InfernalGhast;
	}

	@Override
	public void onBossDeath() {
		say(this.getBukkitEntity(), "Try this on for size!");
		boss.setLocation(locX, locY, locZ, 1, 1);
		int maxHP = boss.getBukkitEntity().getMetadata("maxHP").get(0).asInt() / 2;
		boss.getBukkitEntity().setMetadata("currentHP", new FixedMetadataValue(DungeonRealms.getInstance(), maxHP));
//		HealthHandler.getInstance().setMonsterHPLive((LivingEntity) boss, (HealthHandler.getInstance().getMonsterMaxHPLive((LivingEntity) boss) / 2));
		boss.isInvulnerable(DamageSource.FALL);
	}

	@Override
	public void onBossHit(LivingEntity en) {
	}

	/**
	 */
	public void setArmor(ItemStack[] armor, ItemStack weapon) {
		// weapon.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
		this.setEquipment(1, CraftItemStack.asNMSCopy(armor[0]));
		this.setEquipment(2, CraftItemStack.asNMSCopy(armor[1]));
		this.setEquipment(3, CraftItemStack.asNMSCopy(armor[2]));
	}

}