package rpgclasses.buffs.MobClasses;

import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import rpgclasses.data.MobData;
import rpgclasses.levelevents.GlacialMobExplosionEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GlacialMobClassBuff extends MobClassBuff {
    public Map<Mob, Long> cooldowns = new HashMap<>();

    @Override
    public void initModifiers(ActiveBuff activeBuff, int level) {
        activeBuff.setModifier(BuffModifiers.ALL_DAMAGE, level * 0.02F);
        activeBuff.setModifier(BuffModifiers.SPEED, level * 0.01F);
    }

    @Override
    public void clientTick(ActiveBuff activeBuff) {
        super.clientTick(activeBuff);
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            long lastExplosion = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            float cooldown = getCooldown(mobData.level);
            if (owner.isVisible() && GameRandom.globalRandom.getChance(Math.min(cooldown, (now - lastExplosion) / cooldown))) {
                owner.getLevel().entityManager.addParticle(owner.x + (float) (GameRandom.globalRandom.nextGaussian() * 6.0), owner.y + (float) (GameRandom.globalRandom.nextGaussian() * 8.0), Particle.GType.IMPORTANT_COSMETIC).movesConstant(owner.dx / 10.0F, owner.dy / 10.0F).color(new Color(0, 255, 255)).givesLight(180F, 0.5F).height(16.0F);
            }
        }
    }

    @Override
    public void serverTick(ActiveBuff activeBuff) {
        Mob owner = activeBuff.owner;
        MobData mobData = MobData.getMob(owner);
        if (mobData != null) {
            long lastExplosion = cooldowns.getOrDefault(owner, 0L);
            long now = owner.getTime();
            long cooldown = getCooldown(mobData.level);
            if ((now - lastExplosion) > cooldown) {
                int range = 50 + mobData.level * 4;
                owner.getLevel().entityManager.addLevelEvent(new GlacialMobExplosionEvent(owner.x, owner.y, range, 0, owner));
                cooldowns.put(owner, now);
            }
        }
    }

    public long getCooldown(int level) {
        return Math.max(5000 - level * 100, 1000);
    }
}
