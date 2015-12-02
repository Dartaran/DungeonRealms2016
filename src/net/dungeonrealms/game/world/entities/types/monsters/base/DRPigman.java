package net.dungeonrealms.game.world.entities.types.monsters.base;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.Monster;
import net.dungeonrealms.game.world.items.Item.ItemModifier;
import net.dungeonrealms.game.world.items.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityPigZombie;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 18, 2015
 */
public class DRPigman extends EntityPigZombie implements Monster {

	/**
	 * @param name
	 */
	public DRPigman(World name) {
		super(name);
	}
	
	public EnumMonster enumMonster;

	/**
	 * @param world
	 * @param daemon
	 * @param tier
	 */
	public DRPigman(World world, EnumMonster mon, int tier) {
		super(world);
		enumMonster = mon;
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(14d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);

		setArmor(tier);
        String customName = enumMonster.getPrefix() + " " + enumMonster.name + " " + enumMonster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
	}

	@Override
	protected Item getLoot() {
		return null;
	}

	@Override
	protected void getRareDrop() {

	}

    private void setArmor(int tier) {
        ItemStack[] armor = API.getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getTierWeapon(tier);
        
        ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
        ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
        ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);

        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(1, CraftItemStack.asNMSCopy(armor0));
        this.setEquipment(2, CraftItemStack.asNMSCopy(armor1));
        this.setEquipment(3, CraftItemStack.asNMSCopy(armor2));
        this.setEquipment(4, this.getHead());
    }

	protected String getCustomEntityName() {
		return this.enumMonster.name;
	}

	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(enumMonster.mobHead);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
	}

    private ItemStack getTierWeapon(int tier) {
    	ItemStack item = new ItemGenerator().next(net.dungeonrealms.game.world.items.Item.ItemType.getById(new Random().nextInt(net.dungeonrealms.game.world.items.Item.ItemType.values().length - 2)), net.dungeonrealms.game.world.items.Item.ItemTier.getByTier(tier), ItemModifier.COMMON);
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

	@Override
	protected String z() {
		return "";
	}

	@Override
	protected String bo() {
		return "game.player.hurt";
	}

	@Override
	protected String bp() {
		return "";
	}

	@Override
	public void onMonsterAttack(Player p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMonsterDeath() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), enumMonster, this.getBukkitEntity());
		if(this.random.nextInt(100) < 33)
			this.getRareDrop();
		});
	}

	@Override
	public EnumMonster getEnum() {
		return this.enumMonster;
	}
}