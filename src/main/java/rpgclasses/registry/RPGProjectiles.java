package rpgclasses.registry;

import necesse.engine.registries.ProjectileRegistry;
import rpgclasses.projectiles.*;

public class RPGProjectiles {

    public static void registerCore() {
        ProjectileRegistry.registerProjectile("objectprojectile", ObjectProjectile.class, null, null);

        ProjectileRegistry.registerProjectile("lethalarrow", LethalArrow.class, "lethalarrow", "arrow_shadow");
        ProjectileRegistry.registerProjectile("venomarrow", VenomArrow.class, "lethalarrow", "arrow_shadow");
        ProjectileRegistry.registerProjectile("explosivearrow", ExplosiveArrow.class, "explosivearrow", "arrow_shadow");

        ProjectileRegistry.registerProjectile("plasmagrenade", PlasmaGrenade.class, null, null);
    }

}
