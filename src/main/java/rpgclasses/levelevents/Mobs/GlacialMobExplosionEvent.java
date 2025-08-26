package rpgclasses.levelevents.Mobs;

import necesse.engine.registries.BuffRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.levelEvent.explosionEvent.ExplosionEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;

import java.awt.*;

public class GlacialMobExplosionEvent extends ExplosionEvent implements Attacker {
    private int particleBuffer;
    protected ParticleTypeSwitcher explosionTypeSwitcher;

    public GlacialMobExplosionEvent() {
        this(0.0F, 0.0F, 100, 0.0F, null);
    }

    public GlacialMobExplosionEvent(float x, float y, int range, float toolTier, Mob owner) {
        super(x, y, range, new GameDamage(0), false, toolTier, owner);
        this.explosionTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
        this.targetRangeMod = 0.0F;
        this.hitsOwner = false;
    }

    @Override
    protected GameDamage getTotalObjectDamage(float targetDistance) {
        return super.getTotalObjectDamage(targetDistance).modDamage(10.0F);
    }

    @Override
    protected void playExplosionEffects() {
        SoundManager.playSound(GameResources.iceHit, SoundEffect.effect(this.x, this.y).volume(2F).pitch(1.5F));
    }

    @Override
    public float getParticleCount(float currentRange, float lastRange) {
        return super.getParticleCount(currentRange, lastRange) * 1.5F;
    }

    @Override
    public void spawnExplosionParticle(float x, float y, float dirX, float dirY, int lifeTime, float range) {
        if (this.particleBuffer < 10) {
            ++this.particleBuffer;
        } else {
            this.particleBuffer = 0;
            if (range <= (float) Math.max(this.range - 125, 25)) {
                float dx = dirX * (float) GameRandom.globalRandom.getIntBetween(140, 150);
                float dy = dirY * (float) GameRandom.globalRandom.getIntBetween(130, 140) * 0.8F;
                this.getLevel().entityManager.addParticle(x, y, this.explosionTypeSwitcher.next()).sprite(GameResources.puffParticles.sprite(GameRandom.globalRandom.getIntBetween(0, 4), 0, 12)).sizeFades(70, 100).givesLight(53.0F, 1.0F).movesFriction(dx * 0.05F, dy * 0.05F, 0.8F).color((options, lifeTime1, timeAlive, lifePercent) -> {
                    float clampedLifePercent = Math.max(0.0F, Math.min(1.0F, lifePercent));
                    options.color(new Color(
                            (int) (50.0F - 30.0F * clampedLifePercent),
                            (int) (100.0F - 80.0F * clampedLifePercent),
                            (int) (255.0F - 100.0F * clampedLifePercent)
                    ));
                }).heightMoves(0.0F, 10.0F).lifeTime(lifeTime * 3);
            }
        }
    }

    @Override
    protected void onMobWasHit(Mob mob, float distance) {
        float duration = distance <= 32 ? 10F : Math.max(10F / (distance / 32), 2F);
        mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.BROKEN_ARMOR, mob, duration, null), true);
        mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.FREEZING, mob, duration, null), true);
        mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.FROZEN_ENEMY, mob, 1000, null), true);
    }
}
