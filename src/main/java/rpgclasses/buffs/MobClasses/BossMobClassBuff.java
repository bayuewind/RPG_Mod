package rpgclasses.buffs.MobClasses;

import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;

public class BossMobClassBuff extends MobClassBuff {
    @Override
    public void initModifiers(ActiveBuff activeBuff, int level) {
        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, level * 0.02F);
    }
}
