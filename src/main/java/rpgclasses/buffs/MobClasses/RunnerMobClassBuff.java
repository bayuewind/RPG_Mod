package rpgclasses.buffs.MobClasses;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.data.MobData;

public class RunnerMobClassBuff extends MobClassBuff {
    @Override
    public void initModifiers(ActiveBuff activeBuff, int level) {
        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, MobData.levelScaling(level) * 0.015F);
        activeBuff.setModifier(BuffModifiers.SPEED, MobData.levelScaling(level) * 0.05F);
    }
}
