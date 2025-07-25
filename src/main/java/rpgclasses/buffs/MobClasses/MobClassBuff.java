package rpgclasses.buffs.MobClasses;

import necesse.entity.mobs.WormMobBody;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffEventSubscriber;
import necesse.entity.mobs.buffs.staticBuffs.Buff;

abstract public class MobClassBuff extends Buff {
    public MobClassBuff() {
        this.canCancel = false;
    }

    @Override
    public void init(ActiveBuff activeBuff, BuffEventSubscriber buffEventSubscriber) {
        initModifiers(activeBuff, getMobLevel(activeBuff));
    }

    abstract public void initModifiers(ActiveBuff activeBuff, int level);

    public static void setMobLevel(ActiveBuff activeBuff, int level) {
        activeBuff.getGndData().setInt("moblevel", level);
    }

    public static int getMobLevel(ActiveBuff activeBuff) {
        return activeBuff.getGndData().getInt("moblevel", 0);
    }

    @Override
    public void onRemoved(ActiveBuff activeBuff) {
        activeBuff.owner.buffManager.addBuff(new ActiveBuff(this.getStringID(), activeBuff.owner, 3600F, null), false);
    }
}
