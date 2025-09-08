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
import rpgclasses.buffs.IgnitedBuff;

import java.awt.*;

public class IceExplosionLevelEvent extends ExplosionEvent implements Attacker {
    private int particleBuffer;
    protected ParticleTypeSwitcher explosionTypeSwitcher;

    public IceExplosionLevelEvent() {
        this(0.0F, 0.0F, 50, new GameDamage(0), 0, null, false);
        this.destroysObjects = false;
        this.destroysTiles = false;
    }

    public IceExplosionLevelEvent(float x, float y, int range, GameDamage damage, int toolTier, Mob owner, boolean hitsOwner) {
        super(x, y, range, damage, false, toolTier, owner);
        this.explosionTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
        this.targetRangeMod = 0.0F;
        this.destroysObjects = false;
        this.destroysTiles = false;
        this.hitsOwner = hitsOwner;
    }

    @Override
    protected void playExplosionEffects() {
        SoundManager.playSound(GameResources.explosionHeavy, SoundEffect.effect(this.x, this.y).volume(0.5F).pitch(0.5F));
        this.level.getClient().startCameraShake(this.x, this.y, 200, 40, 1F, 0.8F, true);
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
                float dx = dirX * (float) GameRandom.globalRandom.getIntBetween(40, 50);
                float dy = dirY * (float) GameRandom.globalRandom.getIntBetween(40, 50) * 0.8F;
                this.getLevel().entityManager.addParticle(x, y, this.explosionTypeSwitcher.next()).sprite(GameResources.puffParticles.sprite(GameRandom.globalRandom.getIntBetween(0, 4), 0, 12)).sizeFades(70, 100).givesLight(180F, 1F).movesFriction(dx * 0.05F, dy * 0.05F, 0.8F).color((options, lifeTime1, timeAlive, lifePercent) -> {
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
        boolean isOwner = mob == ownerMob;
        if(!isOwner || distance < (range / 2F)) {
            float mod = this.getDistanceMod(distance);
            GameDamage damage = this.getTotalMobDamage(mod);
            if(isOwner) damage = damage.modDamage(0.25F);
            float knockback = (float)this.knockback * mod;
            mob.isServerHit(damage, (float)mob.getX() - this.x, (float)mob.getY() - this.y, knockback, this);
            mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.Debuffs.FREEZING, mob, 5F, null), true);
            if (!mob.isBoss()) mob.buffManager.addBuff(new ActiveBuff(BuffRegistry.FROZEN_ENEMY, mob, 1F, null), true);
        }
    }
}
