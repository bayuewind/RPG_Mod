package rpgclasses.mobs.mount;

import necesse.engine.gameLoop.tickManager.Performance;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.modifiers.ModifierValue;
import necesse.engine.network.Packet;
import necesse.engine.network.packet.PacketMobMovement;
import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.registries.MobRegistry;
import necesse.engine.save.LoadData;
import necesse.engine.save.SaveData;
import necesse.engine.seasons.GameSeasons;
import necesse.engine.seasons.SeasonalHat;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.*;
import necesse.entity.mobs.ability.EmptyMobAbility;
import necesse.entity.mobs.ai.behaviourTree.AINode;
import necesse.entity.mobs.ai.behaviourTree.AINodeResult;
import necesse.entity.mobs.ai.behaviourTree.BehaviourTreeAI;
import necesse.entity.mobs.ai.behaviourTree.Blackboard;
import necesse.entity.mobs.ai.behaviourTree.composites.SequenceAINode;
import necesse.entity.mobs.ai.behaviourTree.event.AIEvent;
import necesse.entity.mobs.ai.behaviourTree.leaves.ConfusedWandererAINode;
import necesse.entity.mobs.ai.behaviourTree.util.AIMover;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.entity.particle.Particle;
import necesse.entity.particle.SmokePuffParticle;
import necesse.entity.projectile.AncientBoneProjectile;
import necesse.entity.projectile.Projectile;
import necesse.gfx.GameResources;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.human.HumanDrawOptions;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.Item;
import necesse.inventory.item.armorItem.ArmorItem;
import necesse.level.maps.Level;
import necesse.level.maps.light.GameLight;
import org.jetbrains.annotations.NotNull;
import rpgclasses.RPGUtils;
import rpgclasses.content.player.SkillsAndAttributes.Skill;
import rpgclasses.data.PlayerData;
import rpgclasses.data.PlayerDataList;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Stream;

public class LichSkeletonMob extends Mob implements MountAbility {
    public long removeAtTime;
    protected EmptyMobAbility boneAbility;
    protected long boneCooldownTime;
    protected SeasonalHat hat;

    public LichSkeletonMob() {
        super(100);

        this.setSpeed(40.0F);
        this.setFriction(3.0F);
        this.setKnockbackModifier(0.4F);

        this.collision = new Rectangle(-10, -7, 20, 14);
        this.hitBox = new Rectangle(-14, -12, 28, 24);
        this.selectBox = new Rectangle(-14, -41, 28, 48);
        this.swimMaskMove = 16;
        this.swimMaskOffset = -2;
        this.swimSinkOffset = -4;

        this.registerAbility(this.boneAbility = new EmptyMobAbility() {
            protected void run() {
                if (LichSkeletonMob.this.getTime() >= LichSkeletonMob.this.boneCooldownTime) {
                    LichSkeletonMob.this.boneCooldownTime = LichSkeletonMob.this.getTime() + 1000L;
                    if (LichSkeletonMob.this.isServer()) {
                        PlayerMob player = (PlayerMob) LichSkeletonMob.this.getRider();
                        PlayerData playerData = PlayerDataList.getPlayerData(player);
                        Projectile projectile = getProjectile(player, playerData);
                        projectile.resetUniqueID(new GameRandom(Item.getRandomAttackSeed(GameRandom.globalRandom)));

                        player.getLevel().entityManager.projectiles.addHidden(projectile);
                        player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
                    } else if (LichSkeletonMob.this.isClient()) {
                        SoundManager.playSound(GameResources.swing2, SoundEffect.effect(LichSkeletonMob.this).volume(0.7F).pitch(1.2F));
                    }
                }

            }
        });
    }

    @Override
    public void runMountAbility(PlayerMob player, Packet content) {
        this.boneAbility.executePacket(null);
    }

    @Override
    public boolean canRunMountAbility(PlayerMob player, Packet content) {
        return true;
    }

    private static @NotNull Projectile getProjectile(PlayerMob player, PlayerData playerData) {
        Mob target = RPGUtils.findBestTarget(player, 600);

        float targetX;
        float targetY;

        if (target == null) {
            Point2D.Float dir = Skill.getDir(player);
            targetX = dir.x * 100 + player.x;
            targetY = dir.y * 100 + player.y;
        } else {
            targetX = target.x;
            targetY = target.y;
        }

        return new AncientBoneProjectile(player.x, player.y, targetX, targetY, new GameDamage(DamageTypeRegistry.MAGIC, playerData.getStrength(player)), player);
    }


    @Override
    public void addSaveData(SaveData save) {
        super.addSaveData(save);
        save.addLong("removeAtTime", this.removeAtTime);
    }

    @Override
    public void applyLoadData(LoadData save) {
        super.applyLoadData(save);
        this.removeAtTime = save.getLong("removeAtTime");
    }

    @Override
    public void init() {
        super.init();
        SequenceAINode<LichSkeletonMob> sequenceAI = new SequenceAINode<>();
        this.ai = new BehaviourTreeAI<>(this, sequenceAI, new AIMover());
        sequenceAI.addChild(new AINode<LichSkeletonMob>() {
            private long ticksToNextBork = 0L;

            protected void onRootSet(AINode<LichSkeletonMob> root, LichSkeletonMob mob, Blackboard<LichSkeletonMob> blackboard) {
                this.setNextAbilityTime();
            }

            private void setNextAbilityTime() {
                this.ticksToNextBork = GameRandom.globalRandom.getIntBetween(40, 80);
            }

            public void init(LichSkeletonMob mob, Blackboard<LichSkeletonMob> blackboard) {
            }

            public AINodeResult tick(LichSkeletonMob mob, Blackboard<LichSkeletonMob> blackboard) {
                if (this.ticksToNextBork-- <= 0L) {
                    this.setNextAbilityTime();
                    LichSkeletonMob.this.boneAbility.runAndSend();
                }

                return AINodeResult.SUCCESS;
            }
        });
        ConfusedWandererAINode<LichSkeletonMob> confusedWandererAINode = new ConfusedWandererAINode<>();
        confusedWandererAINode.confusionTimer = Long.MAX_VALUE;
        sequenceAI.addChild(confusedWandererAINode);
        if (this.isClient()) {
            Level level = this.getLevel();
            level.entityManager.addParticle(new SmokePuffParticle(level, this.x, this.y + 5.0F, new Color(102, 0, 255)), Particle.GType.IMPORTANT_COSMETIC);
        }
        this.hat = GameSeasons.getHat(new GameRandom(this.getUniqueID()));

    }

    @Override
    public void tickSendSyncPackets() {
        if (this.isServer() && this.sendNextMovementPacket) {
            Mob rider = this.getRider();
            if (rider != null && !rider.isPlayer) {
                ++this.moveSent;
                this.getLevel().getServer().network.sendToClientsWithEntity(new PacketMobMovement(this, this.nextMovementPacketDirect), this);
                this.nextMovementPacketDirect = false;
            }

            this.movementUpdateTime = this.getTime();
            this.sendNextMovementPacket = false;
        }

        super.tickSendSyncPackets();
    }

    @Override
    public void tickCurrentMovement(float delta) {
        this.moveX = 0.0F;
        this.moveY = 0.0F;
        Mob mounted = this.getRider();
        if (this.isMounted() && mounted != null && mounted.isPlayer) {
            this.setDir(mounted.getDir());
            this.moveX = mounted.moveX;
            this.moveY = mounted.moveY;
        } else if (this.currentMovement != null) {
            this.hasArrivedAtTarget = this.currentMovement.tick(this);
            if (this.stopMoveWhenArrive && this.hasArrivedAtTarget) {
                this.stopMoving();
            }
        } else {
            this.hasArrivedAtTarget = true;
        }

    }

    @Override
    public void serverTick() {
        super.serverTick();
        Performance.record(this.getLevel().tickManager(), "ai", () -> {
            if (!this.isMounted() || this.getRider() == null || !this.getRider().isPlayer) {
                this.ai.tick();
            }

        });
        Mob rider;
        if (this.getTime() <= this.removeAtTime && this.isMounted()) {
            rider = this.getRider();
            if (rider != null && !rider.buffManager.hasBuff("lichborn2passivebuff")) {
                this.remove();
            }
        } else {
            rider = this.getRider();
            if (rider != null && !rider.isPlayer && rider.ai != null) {
                rider.ai.blackboard.mover.stopMoving(rider);
                rider.ai.blackboard.submitEvent("resetPathTime", new AIEvent());
            }

            this.remove();
        }

    }

    @Override
    public void remove(float knockbackX, float knockbackY, Attacker attacker, boolean isDeath) {
        Level level = this.getLevel();
        level.entityManager.addParticle(new SmokePuffParticle(level, this.x, this.y + 5.0F, new Color(102, 0, 255)), Particle.GType.IMPORTANT_COSMETIC);
        super.remove(knockbackX, knockbackY, attacker, isDeath);
    }

    @Override
    public boolean isVisible() {
        return this.isMounted();
    }

    @Override
    public boolean canLevelInteract() {
        return this.isMounted();
    }

    @Override
    public boolean canPushMob(Mob other) {
        return this.isMounted();
    }

    @Override
    public boolean canBePushed(Mob other) {
        return this.isMounted();
    }

    @Override
    public boolean canTakeDamage() {
        return false;
    }

    @Override
    public void addDrawables(List<MobDrawable> list, OrderableDrawables tileList, OrderableDrawables topList, Level level, int x, int y, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        super.addDrawables(list, tileList, topList, level, x, y, tickManager, camera, perspective);
        if (this.isMounted()) {
            GameLight light = level.getLightLevel(x / 32, y / 32);
            int drawX = camera.getDrawX(x) - 22 - 10;
            int drawY = camera.getDrawY(y) - 44 - 7;
            int dir = this.getDir();
            Point sprite = this.getAnimSprite(x, y, dir);
            drawY += this.getBobbing(x, y);
            drawY += this.getLevel().getTile(x / 32, y / 32).getMobSinkingAmount(this);
            MaskShaderOptions swimMask = this.getSwimMaskShaderOptions(this.inLiquidFloat(x, y));
            HumanDrawOptions humanDrawOptions = (new HumanDrawOptions(level, MobRegistry.Textures.ancientSkeleton)).sprite(sprite).dir(dir).mask(swimMask).light(light);

            if (this.hat != null) {
                humanDrawOptions.hatTexture(this.hat.getDrawOptions(), ArmorItem.HairDrawMode.NO_HAIR);
            }

            final DrawOptions drawOptions = humanDrawOptions.pos(drawX, drawY);
            list.add(new MobDrawable() {
                public void draw(TickManager tickManager) {
                    drawOptions.draw();
                }
            });
            this.addShadowDrawables(tileList, x, y, light, camera);
        }
    }

    @Override
    public int getRockSpeed() {
        return 20;
    }

    @Override
    public boolean shouldDrawRider() {
        return false;
    }

    @Override
    public boolean forceFollowRiderLevelChange(Mob rider) {
        return true;
    }

    public
    @Override GameMessage getMountDismountError(Mob rider, InventoryItem item) {
        return new StaticMessage("");
    }

    @Override
    public Stream<ModifierValue<?>> getDefaultRiderModifiers() {
        return Stream.of(new ModifierValue<>(BuffModifiers.INTIMIDATED, true));
    }
}
