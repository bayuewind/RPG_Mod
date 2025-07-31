package rpgclasses.content.player.PlayerClasses.Wizard.Passives.Shield;

import necesse.engine.registries.DamageTypeRegistry;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.PlayerMob;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;
import rpgclasses.levelevents.Mobs.FireExplosionLevelEvent;

import java.awt.*;

public class FireShield extends SimpleBuffPassive {
    public FireShield(int levelMax, int requiredClassLevel) {
        super("fireshield", "#ff3300", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new ArcaneShield.MagicShieldBuff(new Color(255, 51, 0), 26000, 0,
                (activeBuff, skillLevel, playerData) -> {
                    PlayerMob player = (PlayerMob) activeBuff.owner;
                    player.getLevel().entityManager.addLevelEvent(new FireExplosionLevelEvent(player.x, player.y, 250, new GameDamage(DamageTypeRegistry.MAGIC, playerData.getLevel() + playerData.getIntelligence(player) * skillLevel), 0, player, false));
                }
        );
    }
}
