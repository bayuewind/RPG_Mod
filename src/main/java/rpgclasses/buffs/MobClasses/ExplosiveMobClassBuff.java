package rpgclasses.buffs.MobClasses;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.MobWasHitEvent;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.data.MobData;
import rpgclasses.levelevents.Mobs.ExplosiveMobExplosionEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ExplosiveMobClassBuff extends MobClassBuff {
    public Map<Mob, Long> cooldowns = new HashMap<>();

    @Override
    public void initModifiers(ActiveBuff activeBuff, int level) {
        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, 0.04F + level * 0.01F);
        activeBuff.setModifier(BuffModifiers.SPEED, 0.02F + level * 0.005F);
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            long lastExplosion = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            long cooldown = getCooldown(mobData.levelScaling());
            if (owner.isVisible() && (now - lastExplosion) > cooldown) {
                owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(255, 0, 0)).givesLight(0.0F, 0.5F).height(16.0F);
            }
        }
    }

    @Override
    public void onWasHit(ActiveBuff activeBuff, MobWasHitEvent event) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            long lastExplosion = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            long cooldown = getCooldown(mobData.levelScaling());
            if ((now - lastExplosion) > cooldown) {
                int range = 100 + mobData.levelScaling() * 3;
                GameDamage damage = new GameDamage(mobData.levelScaling() * 3);
                owner.getLevel().entityManager.addLevelEvent(new ExplosiveMobExplosionEvent(owner.x, owner.y, range, damage, 0, owner));
                cooldowns.put(owner, now);
            }
        }
    }

    public long getCooldown(int level) {
        return Math.max(10000 - level * 200, 2000);
    }
}
