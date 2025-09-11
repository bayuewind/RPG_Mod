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
import rpgclasses.content.player.SkillsLogic.ActiveSkills.ActiveSkill;
import rpgclasses.data.PlayerData;
import rpgclasses.projectiles.IceBallProjectile;
import rpgclasses.utils.RPGUtils;

public class Iceball extends ActiveSkill {

    public Iceball(int levelMax, int requiredClassLevel) {
        super("iceball", "#00ccff", levelMax, requiredClassLevel);
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
        SoundManager.playSound(GameResources.magicbolt1, SoundEffect.effect(player));
    }

    private static Projectile getProjectile(PlayerMob player, PlayerData playerData, int activeSkillLevel) {
        Mob target = RPGUtils.findBestTarget(player, 600);

        if (target == null) return null;

        return new IceBallProjectile(player.getLevel(), player, player.x, player.y, target.x, target.y, 100, (int) player.getDistance(target), new GameDamage(DamageTypeRegistry.MAGIC, 5 * playerData.getLevel() + 5 * playerData.getIntelligence(player) * activeSkillLevel), 100);
    }

    @Override
    public String canActive(PlayerMob player, PlayerData playerData, boolean isInUSe) {
        return RPGUtils.anyTarget(player, 600) ? null : "notarget";
    }

    @Override
    public float manaUsage(PlayerMob player, int activeSkillLevel) {
        return 50 + activeSkillLevel * 10;
    }

    @Override
    public int getBaseCooldown() {
        return 15000;
    }

    @Override
    public String[] getExtraTooltips() {
        return new String[]{"iceball", "manausage"};
    }
}
