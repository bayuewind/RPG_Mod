package rpgclasses.buffs.MobClasses;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;

public class WarriorMobClassBuff extends MobClassBuff {
    @Override
    public void initModifiers(ActiveBuff activeBuff, int level) {
        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, 0.12F + level * 0.03F);
        activeBuff.setModifier(BuffModifiers.SPEED, 0.04F + level * 0.01F);
    }
}
