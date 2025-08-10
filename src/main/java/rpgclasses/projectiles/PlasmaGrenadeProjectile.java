package rpgclasses.projectiles;

import necesse.engine.gameLoop.tickManager.TickManager;
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
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.level.maps.Level;
import necesse.level.maps.LevelObjectHit;
import rpgclasses.utils.RPGUtils;

import java.awt.*;
import java.util.List;

public class PlasmaGrenadeProjectile extends FollowingProjectile {
    public PlasmaGrenadeProjectile() {
    }

    public PlasmaGrenadeProjectile(Level level, Mob owner, float x, float y, float targetX, float targetY, float speed, int distance, GameDamage damage, int knockback) {
        this.setLevel(level);
        this.setOwner(owner);
        this.x = x;
        this.y = y;
        this.setTarget(targetX, targetY);
        this.speed = speed;
        this.distance = distance;
        this.setDamage(damage);
        this.knockback = knockback;
    }

    public void init() {
        super.init();
        this.turnSpeed = 0.5F;
        this.givesLight = true;
        this.height = 18.0F;
        this.trailOffset = 0F;
        this.setWidth(1.0F, true);
        this.piercing = 0;
        this.bouncing = 0;
        this.doesImpactDamage = false;
    }

    public Color getParticleColor() {
        return new Color(0, 255, 255);
    }

    public Trail getTrail() {
        return new Trail(this, this.getLevel(), new Color(0, 255, 255), 2.0F, 500, this.getHeight());
    }

    @Override
    protected Color getWallHitColor() {
        return new Color(0, 255, 255);
    }

    @Override
    public void updateTarget() {
        if (this.traveledDistance > 50F) {
            target = RPGUtils.findBestTarget(getOwner(), 1000);
        }
    }

    @Override
    public float getTurnSpeed(int targetX, int targetY, float delta) {
        return super.getTurnSpeed(targetX, targetY, delta) * 0.002F * (traveledDistance - 50F);
    }

    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, OrderableDrawables overlayList, Level level, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
    }

    @Override
    public void doHitLogic(Mob mob, LevelObjectHit object, float x, float y) {
        super.doHitLogic(mob, object, x, y);
        getLevel().entityManager.addLevelEvent(new PlasmaGrenadeExplosionLevelEvent(x, y, 50, getDamage(), 0, getOwner()));
    }

    public static class PlasmaGrenadeExplosionLevelEvent extends ExplosionEvent implements Attacker {
        private int particleBuffer;
        protected ParticleTypeSwitcher explosionTypeSwitcher;

        public PlasmaGrenadeExplosionLevelEvent() {
            this(0.0F, 0.0F, 50, new GameDamage(0), 0, null);
            this.destroysObjects = false;
            this.destroysTiles = false;
            this.hitsOwner = false;
        }

        public PlasmaGrenadeExplosionLevelEvent(float x, float y, int range, GameDamage damage, int toolTier, Mob owner) {
            super(x, y, range, damage, false, toolTier, owner);
            this.explosionTypeSwitcher = new ParticleTypeSwitcher(Particle.GType.IMPORTANT_COSMETIC, Particle.GType.COSMETIC, Particle.GType.CRITICAL);
            this.targetRangeMod = 0.0F;
            this.destroysObjects = false;
            this.destroysTiles = false;
            this.hitsOwner = false;
        }

        @Override
        protected void playExplosionEffects() {
            SoundManager.playSound(GameResources.explosionLight, SoundEffect.effect(this.x, this.y).volume(0.8F).pitch(1F));
            this.level.getClient().startCameraShake(this.x, this.y, 100, 40, 0.5F, 0.5F, true);
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
                        options.color(new Color(0, (int) (255 - 128 * clampedLifePercent), (int) (255 - 128 * clampedLifePercent)));
                    }).heightMoves(0.0F, 10.0F).lifeTime(lifeTime * 3);
                }
            }

        }

        @Override
        protected boolean canHitMob(Mob target) {
            return target.canTakeDamage() && target.canBeTargeted(ownerMob, ((PlayerMob) ownerMob).getNetworkClient());
        }
    }

}
