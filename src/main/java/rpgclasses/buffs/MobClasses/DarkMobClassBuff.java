package rpgclasses.buffs.MobClasses;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.data.MobData;
import rpgclasses.registry.RPGBuffs;

import java.awt.*;

public class DarkMobClassBuff extends MobClassBuff {
    @Override
    public void initModifiers(ActiveBuff activeBuff, int level) {
        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, level * 0.08F);
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(0, 0, 0)).givesLight().height(16.0F);
        }
    }

    @Override
    public void onWasHit(ActiveBuff activeBuff, MobWasHitEvent event) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            int range = 100 + mobData.level * 5;
            int duration = 4000 + mobData.level * 200;

            AphAreaList areaList = new AphAreaList(
                    new AphArea(range, new Color(0, 0, 0, 102)).setDebuffArea(duration, RPGBuffs.DarkCurse.getStringID())
            ).setOnlyVision(false);

            areaList.execute(owner, true);
        }
    }

    @Override
    public void onHasAttacked(ActiveBuff activeBuff, MobWasHitEvent event) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            int duration = 10000 + mobData.level * 500;
            ActiveBuff ab = new ActiveBuff(RPGBuffs.DarkCurse, event.target, duration, null);
            event.target.buffManager.addBuff(ab, true);
        }
    }
}
