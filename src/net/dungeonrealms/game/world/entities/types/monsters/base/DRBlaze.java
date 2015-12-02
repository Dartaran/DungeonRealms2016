package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.Monster;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.ItemGenerator;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by Chase on Oct 4, 2015
 */
public abstract class DRBlaze extends net.minecraft.server.v1_8_R3.EntityBlaze implements Monster {

	protected String name;
	protected String mobHead;
	protected EnumEntityType entityType;
	protected EnumMonster monsterType;
	public DRBlaze(World world, EnumMonster monster, int tier, EnumEntityType entityType, boolean setArmor) {
		this(world);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(14d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
		monsterType = monster;
		this.name = monster.name;
		this.mobHead = monster.mobHead;
		this.entityType = entityType;
		if (setArmor)
			setArmor(tier);
		this.getBukkitEntity().setCustomNameVisible(true);
        String customName = monster.getPrefix() + " " + name + " " + monster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
		setStats();
	}
	
	@Override
	protected abstract void getRareDrop();

	protected DRBlaze(World world) {
		super(world);
	}

	protected abstract void setStats();

	public static Object getPrivateField(String fieldName, Class clazz, Object object) {
		Field field;
		Object o = null;
		try {
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			o = field.get(object);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return o;
	}
	protected String getCustomEntityName() {
		return this.name;
	}
	protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(mobHead);
		head.setItemMeta(meta);
		return CraftItemStack.asNMSCopy(head);
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

    private ItemStack getTierWeapon(int tier) {
    	ItemStack item =new ItemGenerator().next(net.dungeonrealms.game.world.items.Item.ItemType.STAFF, net.dungeonrealms.game.world.items.Item.ItemTier.getByTier(tier), Item.ItemModifier.COMMON);
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
    	
    	
    }
    
	@Override
	public abstract EnumMonster getEnum();

	@Override
	public void onMonsterDeath() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
			this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity());
			if (new Random().nextInt(99) < 3) {
				this.getRareDrop();
			}
		});
	}
}