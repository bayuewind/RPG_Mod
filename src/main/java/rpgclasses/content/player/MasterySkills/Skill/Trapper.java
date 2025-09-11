package rpgclasses.content.player.MasterySkills.Skill;

import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.MobBeforeHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.MasteryBuff;
import rpgclasses.content.player.MasterySkills.Mastery;
import rpgclasses.registry.RPGBuffs;

public class Trapper extends Mastery {

    public Trapper(String stringID, String color) {
        super(stringID, color);
    }

    @Override
    public MasteryBuff masteryBuff() {
        return new MasteryBuff() {
            @Override
            public void onBeforeAttacked(ActiveBuff buff, MobBeforeHitEvent event) {
                super.onBeforeAttacked(buff, event);
                if (event.target.buffManager.hasBuff(RPGBuffs.TRAPPED) ||
                        event.target.buffManager.hasBuff(RPGBuffs.CONSTRAINED)
                ) {
                    event.damage = event.damage.modDamage(1.5F);
                }
            }
        };
    }
}