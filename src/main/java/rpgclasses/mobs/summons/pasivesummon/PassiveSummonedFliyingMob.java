package rpgclasses.mobs.summons.pasivesummon;

import necesse.level.maps.CollisionFilter;

abstract public class PassiveSummonedFliyingMob extends PassiveSummonedMob {

    public PassiveSummonedFliyingMob(int health) {
        super(health);
    }

    public int getFlyingHeight() {
        return 50;
    }

    public CollisionFilter getLevelCollisionFilter() {
        return null;
    }

}
