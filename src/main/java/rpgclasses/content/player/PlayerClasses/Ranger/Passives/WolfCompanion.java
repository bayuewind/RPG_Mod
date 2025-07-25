package rpgclasses.content.player.PlayerClasses.Ranger.Passives;

import necesse.engine.registries.MobRegistry;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.Passive;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;
import rpgclasses.mobs.summons.pasive.PassiveSummonedMob;

public class WolfCompanion extends SimpleBuffPassive {

    public WolfCompanion(int levelMax, int requiredClassLevel) {
        super("wolfcompanion", "#E6D9CC", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new WolfCompanionBuff(this, getBuffStringID());
    }

    public static class WolfCompanionBuff extends PrincipalPassiveBuff {
        public String buffStringID;
        public Passive passive;

        public WolfCompanionBuff(Passive passive, String buffStringID) {
            this.passive = passive;
            this.buffStringID = buffStringID;
        }

        @Override
        public void serverTick(ActiveBuff buff) {
            super.serverTick(buff);
            if (buff.owner.isPlayer) {
                PlayerMob player = (PlayerMob) buff.owner;
                if (player.serverFollowersManager.getFollowerCount(buffStringID) == 0) {
                    PassiveSummonedMob mob = (PassiveSummonedMob) MobRegistry.getMob("rangerwolf", buff.owner.getLevel());
                    player.serverFollowersManager.addFollower(buffStringID, mob, FollowPosition.WALK_CLOSE, buffStringID, 1, 1, null, true);
                    mob.setPassive(passive);
                    mob.getLevel().entityManager.addMob(mob, buff.owner.x, buff.owner.y);
                }
            }

        }
    }

}
