package rpgclasses.buffs.MobClasses;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.data.MobData;

public class WarriorMobClassBuff extends MobClassBuff {
    @Override
    public void initModifiers(ActiveBuff activeBuff, int level) {
        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, MobData.levelScaling(level) * 0.03F);
        activeBuff.setModifier(BuffModifiers.SPEED, MobData.levelScaling(level) * 0.01F);
    }
}
