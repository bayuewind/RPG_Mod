package rpgclasses.registry;

import aphorea.registry.AphBuffs;
import necesse.engine.registries.BuffRegistry;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.entity.mobs.buffs.staticBuffs.Buff;
import rpgclasses.buffs.*;
import rpgclasses.buffs.Passive.GrabbedObjectBuff;
import rpgclasses.buffs.Passive.HolyDamageDealtBuff;
import rpgclasses.buffs.Passive.ModifiersBuff;
import rpgclasses.buffs.Passive.OverlevelClassBuff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RPGBuffs {

    public static List<String> debuffs = new ArrayList<>();

    static {
        debuffs.addAll(
                Arrays.stream(new String[]{
                        "tremorhappening",
                        "swampspores",

                        "onfire",
                        "ablaze",
                        "brokenarmor",
                        "spiderwebslow",
                        "webpotionslow",
                        "spidervenom",
                        "necroticpoison",
                        "necroticslow",
                        "ivypoison",
                        "widowpoison",
                        "chilled",
                        "freezing",
                        "frostburn",
                        "frostslow",
                        "snowcovered",
                        "snowcoveredslow",
                        "flourcoveredslow",
                        "haunted",
                        "emeraldpoison",
                        "spidercharmpoison",
                        "slimepoison",
                        "sandknifewound",
                        "crystallize",
                        "bloodgrimoiredebuff",
                        "slimegreatbowdebuff",
                        "crushingdarkness",
                        "netted",
                        "dryadhaunted",
                        "dryadpossessed",
                        "spirithaunted",
                        "spiritpossessed",
                        "stunneddamagetakenincreased",
                        "spiritcorrupted",

                        // AphoreaMod
                        "stunbuff",
                        "fallenstunbuff",
                        "stickybuff",
                        "honeyedbuff",
                        "cursedbuff",

                        // RPG Mod
                        "trappedbuff",
                        "markedbuff",
                        "darkcursebuff",
                        "magicpoisonbuff",
                        "ignitedbuff",
                        "constrainedbuff"
                }).collect(Collectors.toList())
        );
    }

    public static Buff Trapped;
    public static Buff Marked;
    public static Buff DarkCurse;
    public static Buff MagicPoison;
    public static Buff Ignited;
    public static Buff Constrained;

    public static class PASSIVES {
        public static ModifiersBuff Modifiers;
        public static HolyDamageDealtBuff HolyDamage;
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
        BuffRegistry.registerBuff("holydamagedealtbuff", PASSIVES.HolyDamage = new HolyDamageDealtBuff());
        BuffRegistry.registerBuff("overlevelclassbuff", PASSIVES.OverlevelClass = new OverlevelClassBuff());
        BuffRegistry.registerBuff("grabbedobjectbuff", PASSIVES.GrabbedObject = new GrabbedObjectBuff());
    }

    public static void applyStop(Mob target, float duration) {
        applyStop(target, (int) (duration * 1000));
    }

    public static void applyStop(Mob target, int duration) {
        ActiveBuff ab = new ActiveBuff(AphBuffs.STOP, target, duration, null);
        target.buffManager.addBuff(ab, true);
    }

    public static void applyStun(Mob target, float duration) {
        applyStun(target, (int) (duration * 1000));
    }

    public static void applyStun(Mob target, int duration) {
        ActiveBuff ab = new ActiveBuff(AphBuffs.STUN, target, duration, null);
        target.buffManager.addBuff(ab, true);
    }

    public static void purify(Mob target, boolean updatePacket) {
        List<String> buffsToRemove = new ArrayList<>();

        for (ActiveBuff activeBuff : target.buffManager.getBuffs().values()) {
            String buffStringID = activeBuff.buff.getStringID();
            if (debuffs.contains(buffStringID)) {
                buffsToRemove.add(buffStringID);
            }
        }

        for (String id : buffsToRemove) {
            target.buffManager.removeBuff(id, updatePacket);
        }
    }

}