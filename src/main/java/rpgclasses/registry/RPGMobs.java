package rpgclasses.registry;

import necesse.engine.registries.MobRegistry;
import rpgclasses.mobs.summons.pasive.RangerWolfMob;

public class RPGMobs {

    public static void registerCore() {
        MobRegistry.registerMob("rangerwolf", RangerWolfMob.class, false);
    }

}
