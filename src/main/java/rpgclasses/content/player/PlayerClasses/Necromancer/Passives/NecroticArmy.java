package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.engine.registries.MobRegistry;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.itemAttacker.FollowPosition;
import rpgclasses.RPGUtils;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;
import rpgclasses.mobs.summons.damageable.DamageableFollowingMob;

public class NecroticArmy extends SimpleBuffPassive {
    public NecroticArmy(int levelMax, int requiredClassLevel) {
        super("necroticarmy", "#669966", levelMax, requiredClassLevel);
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"necromancerskeleton"};
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (activeBuff.owner.isServer() && !event.wasPrevented && 0 >= event.target.getHealth() && RPGUtils.isValidTarget(activeBuff.owner, event.target)) {
                    PlayerMob player = (PlayerMob) activeBuff.owner;
                    PlayerData playerData = PlayerDataList.getPlayerData(player);

                    DamageableFollowingMob mob = (DamageableFollowingMob) MobRegistry.getMob("necromancerskeleton", player.getLevel());
                    player.serverFollowersManager.addFollower(stringID, mob, FollowPosition.WALK_CLOSE, null, 1, Integer.MAX_VALUE, null, true);

                    mob.updateStats(player, playerData, getLevel(activeBuff) * 0.03F);

                    player.getLevel().entityManager.addMob(mob, event.target.x, event.target.y);
                }
            }
        };
    }
}
