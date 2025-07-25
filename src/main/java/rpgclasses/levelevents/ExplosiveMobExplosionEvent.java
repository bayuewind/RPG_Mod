package rpgclasses.levelevents;

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

public class ExplosiveMobExplosionEvent extends ExplosionEvent implements Attacker {
    private int particleBuffer;
    protected ParticleTypeSwitcher explosionTypeSwitcher;

    public ExplosiveMobExplosionEvent() {
        this(0.0F, 0.0F, 100, new GameDamage(0), 0.0F, null);
    }

    public ExplosiveMobExplosionEvent(float x, float y, int range, GameDamage damage, float toolTier, Mob owner) {
        super(x, y, range, damage, false, toolTier, owner);
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
        SoundManager.playSound(GameResources.explosionHeavy, SoundEffect.effect(this.x, this.y).volume(2.5F).pitch(1.5F));
        this.level.getClient().startCameraShake(this.x, this.y, 300, 40, 3.0F, 3.0F, true);
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
                    options.color(new Color((int) (255.0F - 55.0F * clampedLifePercent), (int) (225.0F - 200.0F * clampedLifePercent), (int) (155.0F - 125.0F * clampedLifePercent)));
                }).heightMoves(0.0F, 10.0F).lifeTime(lifeTime * 3);
            }
        }
    }

    @Override
    protected boolean canHitMob(Mob target) {
        return target.canBeHit(this.ownerMob);
    }

    @Override
    protected void onMobWasHit(Mob mob, float distance) {
        super.onMobWasHit(mob, distance);
        float duration = distance <= 32 ? 10F : Math.max(10F / (distance / 32), 2F);
        mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.BROKEN_ARMOR, mob, duration, null), true);
    }
}
