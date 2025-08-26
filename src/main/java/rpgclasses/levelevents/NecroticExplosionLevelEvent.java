package rpgclasses.levelevents;

import necesse.engine.network.PacketReader;
import necesse.engine.network.PacketWriter;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.levelEvent.explosionEvent.ExplosionEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.buffs.MagicPoisonBuff;

import java.awt.*;

public class NecroticExplosionLevelEvent extends ExplosionEvent implements Attacker {
    private int particleBuffer;
    protected ParticleTypeSwitcher explosionTypeSwitcher;

    float poisonDamage;

    public NecroticExplosionLevelEvent() {
        this(0.0F, 0.0F, 50, new GameDamage(0), 0, 0, null, false);
        this.destroysObjects = false;
        this.destroysTiles = false;
    }

    public NecroticExplosionLevelEvent(float x, float y, int range, GameDamage damage, float poisonDamage, int toolTier, Mob owner, boolean hitsOwner) {
        super(x, y, range, damage, false, toolTier, owner);
        this.explosionTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
        this.targetRangeMod = 0.0F;
        this.destroysObjects = false;
        this.destroysTiles = false;
        this.hitsOwner = hitsOwner;

        this.poisonDamage = poisonDamage;
    }

    @Override
    public void setupSpawnPacket(PacketWriter writer) {
        super.setupSpawnPacket(writer);
        writer.putNextFloat(poisonDamage);
    }

    @Override
    public void applySpawnPacket(PacketReader reader) {
        super.applySpawnPacket(reader);
        poisonDamage = reader.getNextFloat();
    }

    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addFloat("poisonDamage", poisonDamage);
    }

    @Override
    public void applyLoadData(LoadData load) {
        super.applyLoadData(load);
        poisonDamage = load.getFloat("poisonDamage");
    }

    @Override
    protected void playExplosionEffects() {
        SoundManager.playSound(GameResources.explosionLight, SoundEffect.effect(this.x, this.y).volume(1F).pitch(1F));
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
                            (int) (10 + 70 * clampedLifePercent),
                            (int) (40 + 215 * clampedLifePercent),
                            (int) (10 + 110 * clampedLifePercent)
                    ));
                }).heightMoves(0.0F, 10.0F).lifeTime(lifeTime * 3);
            }
        }

    }

    @Override
    protected void onMobWasHit(Mob mob, float distance) {
        if (damage.damage > 0) {
            float mod = this.getDistanceMod(distance);
            GameDamage damage = this.getTotalMobDamage(mod);
            float knockback = (float) this.knockback * mod;
            mob.isServerHit(damage, (float) mob.getX() - this.x, (float) mob.getY() - this.y, knockback, this);
        }

        MagicPoisonBuff.apply(ownerMob, mob, poisonDamage, 10F);
    }
}
