package rpgclasses.projectiles;

import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.ParticleTypeSwitcher;
import necesse.entity.levelEvent.explosionEvent.ExplosionEvent;
import necesse.entity.mobs.Attacker;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.particle.Particle;
import necesse.entity.projectile.followingProjectile.FollowingProjectile;
import necesse.entity.trails.Trail;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptions;
import necesse.gfx.drawables.EntityDrawable;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import rpgclasses.RPGUtils;

import java.awt.*;
import java.util.List;

public class ExplosiveArrowProjectile extends FollowingProjectile {
    public ExplosiveArrowProjectile() {
    }

    public ExplosiveArrowProjectile(Level level, Mob owner, float x, float y, float targetX, float targetY, float speed, int distance, GameDamage damage, int knockback) {
        this.setLevel(level);
        this.setOwner(owner);
        this.x = x;
        this.y = y;
        this.setTarget(targetX, targetY);
        this.speed = speed;
        this.distance = distance;
        this.setDamage(damage);
        this.knockback = knockback;

        this.turnSpeed = 0.02F;
    }

    @Override
    public void init() {
        super.init();
        this.height = 18.0F;
        this.heightBasedOnDistance = true;
        this.setWidth(8.0F);
    }

    @Override
    public void updateTarget() {
        if (this.traveledDistance > 50F) {
            target = RPGUtils.findBestTarget(getOwner(), 1000);
        }
    }

    @Override
    public Trail getTrail() {
        return new Trail(this, this.getLevel(), new Color(150, 50, 0), 10.0F, 250, 18.0F);
    }

    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        if (!this.removed()) {
            GameLight light = level.getLightLevel(this);
            int drawX = camera.getDrawX(this.x) - this.texture.getWidth() / 2;
            int drawY = camera.getDrawY(this.y);
            final TextureDrawOptions options = this.texture.initDraw().light(light).rotate(this.getAngle(), this.texture.getWidth() / 2, 0).pos(drawX, drawY - (int) this.getHeight());
            list.add(new EntityDrawable(this) {
                public void draw(TickManager tickManager) {
                    options.draw();
                }
            });
            this.addShadowDrawables(tileList, drawX, drawY, light, this.getAngle(), 0);
        }
    }

    @Override
    protected void playHitSound(float x, float y) {
        SoundManager.playSound(GameResources.bowhit, SoundEffect.effect(x, y));
    }

    @Override
    public void remove() {
        super.remove();
        GameDamage damage = new GameDamage(DamageTypeRegistry.MAGIC, getDamage().damage);
        this.getLevel().entityManager.addLevelEvent(new ExplosiveArrowExplosionEvent(x, y, 100, damage, 0, getOwner()));
    }

    public static class ExplosiveArrowExplosionEvent extends ExplosionEvent implements Attacker {
        private int particleBuffer;
        protected ParticleTypeSwitcher explosionTypeSwitcher;

        public ExplosiveArrowExplosionEvent() {
            this(0.0F, 0.0F, 100, new GameDamage(0), 0.0F, null);
        }

        public ExplosiveArrowExplosionEvent(float x, float y, int range, GameDamage damage, float toolTier, Mob owner) {
            super(x, y, range, damage, false, toolTier, owner);
            this.explosionTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
            this.targetRangeMod = 0.0F;
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
    }

}
