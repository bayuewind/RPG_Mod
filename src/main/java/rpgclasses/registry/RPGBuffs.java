package rpgclasses.registry;

import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import rpgclasses.buffs.*;
import rpgclasses.buffs.Passive.GrabbedObjectBuff;
import rpgclasses.buffs.Passive.ModifiersBuff;
import rpgclasses.buffs.Passive.OverlevelClassBuff;

public class RPGBuffs {

    public static Buff Trapped;
    public static Buff Marked;
    public static Buff DarkCurse;
    public static Buff MagicPoison;
    public static Buff Ignited;
    public static Buff Constrained;

    public static class PASSIVES {
        public static ModifiersBuff Modifiers;
        public static OverlevelClassBuff OverlevelClass;
        public static GrabbedObjectBuff GrabbedObject;
    }

    public static void registerCore() {
        BuffRegistry.registerBuff("trappedbuff", Trapped = new TrappedBuff());
        BuffRegistry.registerBuff("markedbuff", Marked = new MarkedBuff());
        BuffRegistry.registerBuff("darkcursebuff", DarkCurse = new DarkCurseBuff());
        BuffRegistry.registerBuff("magicpoisonbuff", MagicPoison = new MagicPoisonBuff());
        BuffRegistry.registerBuff("ignitedbuff", Ignited = new IgnitedBuff());
        BuffRegistry.registerBuff("constrainedbuff", Constrained = new ConstrainedBuff());

        // Passive Buffs
        BuffRegistry.registerBuff("modifiersbuff", PASSIVES.Modifiers = new ModifiersBuff());
        BuffRegistry.registerBuff("overlevelclassbuff", PASSIVES.OverlevelClass = new OverlevelClassBuff());
        BuffRegistry.registerBuff("grabbedobjectbuff", PASSIVES.GrabbedObject = new GrabbedObjectBuff());
    }

}
