package rpgclasses.content.player.PlayerClasses.Necromancer.Passives;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;

public class LifeLeech extends SimpleBuffPassive {
    public LifeLeech(int levelMax, int requiredClassLevel) {
        super("lifeleech", "#993333", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new PrincipalPassiveBuff() {
            @Override
            public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
                super.onHasAttacked(activeBuff, event);
                if (event.damage > 0 && !event.wasPrevented && event.damageType == DamageTypeRegistry.SUMMON) {
                    float healing = event.damage * 0.001F * getLevel(activeBuff) + activeBuff.getGndData().getFloat("healthDot");
                    int trueHealing = (int) healing;

                    activeBuff.getGndData().setFloat("healthDot", healing - trueHealing);

                    if (healing > 0) activeBuff.owner.setHealth(activeBuff.owner.getHealth() + trueHealing);
                }
            }
        };
    }

}
