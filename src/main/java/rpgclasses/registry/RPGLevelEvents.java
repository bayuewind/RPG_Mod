package rpgclasses.registry;

import necesse.engine.registries.LevelEventRegistry;
import rpgclasses.levelevents.ExplosiveMobExplosionEvent;
import rpgclasses.levelevents.GlacialMobExplosionEvent;
import rpgclasses.projectiles.ExplosiveArrow;
import rpgclasses.projectiles.PlasmaGrenade;

public class RPGLevelEvents {

    public static void registerCore() {
        // Mob Classes
        LevelEventRegistry.registerEvent("explosivemobexplosionevent", ExplosiveMobExplosionEvent.class);
        LevelEventRegistry.registerEvent("glacialmobexplosionevent", GlacialMobExplosionEvent.class);

        //Skills
        LevelEventRegistry.registerEvent("plasmagrenadeexplosion", PlasmaGrenade.PlasmaGrenadeExplosionLevelEvent.class);

        // Projectiles
        LevelEventRegistry.registerEvent("explosivearrowexplosionevent", ExplosiveArrow.ExplosiveArrowExplosionEvent.class);

    }

}
