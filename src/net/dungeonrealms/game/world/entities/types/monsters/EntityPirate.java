package net.dungeonrealms.game.world.entities.types.monsters;

import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Nick on 9/17/2015.
 */
public class EntityPirate extends DRZombie{

    public EntityPirate(World world, EnumMonster enumMons, int tier) {
        super(world, enumMons, tier, EnumEntityType.HOSTILE_MOB, true);
    }

    /**
     * @return
     */
    public static String getRandomHead() {
        String[] list = new String[]{"samsamsam1234"};
        return list[Utils.randInt(0, list.length - 1)];
    }

    public EntityPirate(World world) {
        super(world);
    }

    @Override
    public void setStats() {

    }

    @Override
    protected void getRareDrop() {
    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}
    
    @Override
    protected String z() {
        return "mob.zombie.say";
    }

    @Override
    protected String bo() {
        return "game.player.hurt";
    }

    @Override
    protected String bp() {
        return "mob.zombie.death";
    }
    
//    @Override
//	public void onMonsterDeath(){
//		getLoot();
//	}
}