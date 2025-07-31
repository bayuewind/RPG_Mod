package rpgclasses.content.player.PlayerClasses.Wizard.ActiveSkills;

import necesse.engine.network.packet.PacketSpawnProjectile;
import necesse.engine.registries.DamageTypeRegistry;
import necesse.engine.sound.SoundEffect;
import necesse.engine.sound.SoundManager;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.projectile.Projectile;
import necesse.gfx.GameResources;
import org.jetbrains.annotations.NotNull;
import rpgclasses.RPGUtils;
import rpgclasses.content.player.SkillsAndAttributes.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.projectiles.FireballProjectile;

import java.awt.geom.Point2D;

public class Fireball extends ActiveSkill {

    public Fireball(int levelMax, int requiredClassLevel) {
        super("fireball", "#ff3300", levelMax, requiredClassLevel);
    }

    @Override
    public void runServer(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runServer(player, playerData, activeSkillLevel, seed, isInUse);

        Projectile projectile = getProjectile(player, playerData, activeSkillLevel);
        projectile.resetUniqueID(new GameRandom(seed));

        player.getLevel().entityManager.projectiles.addHidden(projectile);
        player.getServer().network.sendToClientsWithEntity(new PacketSpawnProjectile(projectile), projectile);
    }

    @Override
    public void runClient(PlayerMob player, PlayerData playerData, int activeSkillLevel, int seed, boolean isInUse) {
        super.runClient(player, playerData, activeSkillLevel, seed, isInUse);
        SoundManager.playSound(GameResources.firespell1, SoundEffect.effect(player));
    }

    private static @NotNull Projectile getProjectile(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        Mob target = RPGUtils.findBestTarget(player, 1000);

        float targetX;
        float targetY;

        if (target == null) {
            Point2D.Float dir = getDir(player);
            targetX = dir.x * 100 + player.x;
            targetY = dir.y * 100 + player.y;
            target = player;
        } else {
            targetX = target.x;
            targetY = target.y;
        }

        return new FireballProjectile(player.getLevel(), player, player.x, player.y, targetX, targetY, 100, (int) player.getDistance(target), new GameDamage(DamageTypeRegistry.MAGIC, 5 * playerData.getLevel() + 5 * playerData.getIntelligence(player) * activeSkillLevel), 100);
    }

    @Override
    public float manaUsage(int activeSkillLevel) {
        return 80 + activeSkillLevel * 16;
    }

    @Override
    public int getBaseCooldown() {
        return 20000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"fireball", "manausage"};
    }
}
