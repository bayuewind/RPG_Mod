package rpgclasses.registry;

import necesse.engine.registries.LevelEventRegistry;
import rpgclasses.content.player.PlayerClasses.Wizard.Passives.FlamingSteps;
import rpgclasses.levelevents.FireExplosionLevelEvent;
import rpgclasses.levelevents.IceExplosionLevelEvent;
import rpgclasses.levelevents.Mobs.ExplosiveMobExplosionEvent;
import rpgclasses.levelevents.Mobs.GlacialMobExplosionEvent;
import rpgclasses.levelevents.NecroticExplosionLevelEvent;
import rpgclasses.projectiles.ExplosiveArrowProjectile;
import rpgclasses.projectiles.PlasmaGrenadeProjectile;

public class RPGLevelEvents {

    public static void registerCore() {
        // Generic
        LevelEventRegistry.registerEvent("iceexplosionevent", IceExplosionLevelEvent.class);
        LevelEventRegistry.registerEvent("fireexplosionevent", FireExplosionLevelEvent.class);
        LevelEventRegistry.registerEvent("necroticexplosionevent", NecroticExplosionLevelEvent.class);

        // Mob Classes
        LevelEventRegistry.registerEvent("explosivemobexplosionevent", ExplosiveMobExplosionEvent.class);
        LevelEventRegistry.registerEvent("glacialmobexplosionevent", GlacialMobExplosionEvent.class);

        // Skills
        LevelEventRegistry.registerEvent("plasmagrenadeexplosion", PlasmaGrenadeProjectile.PlasmaGrenadeExplosionLevelEvent.class);
        LevelEventRegistry.registerEvent("flamingstepsevent", FlamingSteps.FlamingStepsLevelEvent.class);

        // Projectiles
        LevelEventRegistry.registerEvent("explosivearrowexplosionevent", ExplosiveArrowProjectile.ExplosiveArrowExplosionEvent.class);

    }

}
