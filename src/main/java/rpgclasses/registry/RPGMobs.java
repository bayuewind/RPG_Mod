package rpgclasses.registry;

import necesse.engine.registries.MobRegistry;
import rpgclasses.mobs.summons.DancingFlameMob;
import rpgclasses.mobs.summons.pasivesummon.RangerWolfMob;

public class RPGMobs {

    public static void registerCore() {

        // Summons
        MobRegistry.registerMob("rangerwolf", RangerWolfMob.class, false);
        MobRegistry.registerMob("dancingflame", DancingFlameMob.class, false);
    }

}
