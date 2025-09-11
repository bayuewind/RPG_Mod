package rpgclasses.content.player.PlayerClasses.Cleric.ActiveSkills;

import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.levelEvent.LevelEvent;
import necesse.entity.levelEvent.explosionEvent.ExplosionEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.particle.Particle;
import necesse.gfx.GameResources;
import rpgclasses.content.player.SkillsLogic.ActiveSkills.SimpleLevelEventActiveSkill;
import rpgclasses.data.MobData;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGBuffs;
import rpgclasses.registry.RPGDamageType;

import java.awt.*;

public class Judgment extends SimpleLevelEventActiveSkill {

    public Judgment(int levelMax, int requiredClassLevel) {
        super("judgment", "#ffff66", levelMax, requiredClassLevel);
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public LevelEvent getLevelEvent(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        return new JudgmentLevelEvent(player.x, player.y, 300, new GameDamage(RPGDamageType.HOLY, 5 * playerData.getLevel() + 3 * activeSkillLevel * (playerData.getIntelligence(player) + playerData.getGrace(player))), 0, player);
    }

    @Override
    public Class<? extends LevelEvent> getLevelEventClass() {
        return JudgmentLevelEvent.class;
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 20 + activeSkillLevel * 4;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"holydamage", "constrained", "manausage"};
    }

    public static class JudgmentLevelEvent extends ExplosionEvent implements Attacker {
        private int particleBuffer;
        protected ParticleTypeSwitcher explosionTypeSwitcher;

        public JudgmentLevelEvent() {
            this(0.0F, 0.0F, 50, new GameDamage(0), 0, null);
            this.destroysObjects = false;
            this.destroysTiles = false;
        }

        public JudgmentLevelEvent(float x, float y, int range, GameDamage damage, int toolTier, Mob owner) {
            super(x, y, range, damage, false, toolTier, owner);
            this.explosionTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
            this.targetRangeMod = 0.0F;
            this.destroysObjects = false;
            this.destroysTiles = false;
            this.hitsOwner = false;
        }

        @Override
        protected void playExplosionEffects() {
            SoundManager.playSound(GameResources.glyphTrapCharge, SoundEffect.effect(this.x, this.y).volume(3F).pitch(0.5F));
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
                                (int) (255.0F - 55.0F * clampedLifePercent),
                                (int) (225.0F - 55.0F * clampedLifePercent),
                                (int) (155.0F - 125.0F * clampedLifePercent)
                        ));
                    }).heightMoves(0.0F, 10.0F).lifeTime(lifeTime * 3);
                }
            }

        }

        @Override
        protected void onMobWasHit(Mob mob, float distance) {
            float mod = this.getDistanceMod(distance);
            float knockback = (float) this.knockback * mod;
            mob.isServerHit(this.getTotalMobDamage(mod), (float) mob.getX() - this.x, (float) mob.getY() - this.y, knockback, this);

            if (MobData.isWeakToHoly(mob, ownerMob)) {
                mob.buffManager.addBuff(new ActiveBuff(RPGBuffs.CONSTRAINED, mob, 5000, null), mob.isServer());
            }
        }
    }

}
