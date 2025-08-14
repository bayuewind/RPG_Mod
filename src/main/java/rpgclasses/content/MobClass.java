package rpgclasses.content;


import necesse.engine.localization.Localization;
import necesse.engine.registries.BuffRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.RPGConfig;
import rpgclasses.buffs.MobClasses.*;

import java.util.*;

public class MobClass {
    public static Map<String, MobClass> allClasses = new HashMap<>();
    public static ArrayList<MobClass> allClassesList = new ArrayList<>();

    public static MobClass bossClass;
    public static List<MobClass> basicClasses = new ArrayList<>();
    public static List<MobClass> uncommonClasses = new ArrayList<>();
    public static List<MobClass> rareClasses = new ArrayList<>();
    public static List<MobClass> mythicClasses = new ArrayList<>();

    public static void registerCore() {
        registerBossClass(new MobClass("boss", "0", 0.04F, RPGConfig.getBossKillBonus(), BossMobClassBuff.class));
        registerMobClass(basicClasses, new MobClass("warrior", "#993333", 0.02F, WarriorMobClassBuff.class));
        registerMobClass(basicClasses, new MobClass("tank", "#666666", 0.05F, TankMobClassBuff.class));
        registerMobClass(basicClasses, new MobClass("runner", "#339966", 0.01F, RunnerMobClassBuff.class));
        registerMobClass(uncommonClasses, new MobClass("healer", "#00ff00", 0.04F, 1.2F, HealerMobClassBuff.class));
        registerMobClass(uncommonClasses, new MobClass("explosive", "#ff0000", 0.03F, 1.2F, ExplosiveMobClassBuff.class));
        registerMobClass(uncommonClasses, new MobClass("glacial", "#00ffff", 0.03F, 1.2F, GlacialMobClassBuff.class));
        registerMobClass(rareClasses, new MobClass("flash", "#ffff00", 0.01F, 2F, FlashMobClassBuff.class));
        registerMobClass(rareClasses, new MobClass("dark", "#000000", 0.05F, 2F, DarkMobClassBuff.class));
        registerMobClass(mythicClasses, new MobClass("legend", "#ff6600", 0.25F, 5F, LegendMobClassBuff.class));
    }

    public static void registerBossClass(MobClass mobClass) {
        bossClass = mobClass;
        registerMobClass(mobClass);
    }

    public static void registerMobClass(List<MobClass> classList, MobClass mobClass) {
        classList.add(mobClass);
        registerMobClass(mobClass);
    }

    public static void registerMobClass(MobClass mobClass) {
        allClasses.put(mobClass.stringID, mobClass);
        allClassesList.add(mobClass);
        try {
            BuffRegistry.registerBuff(mobClass.buffStringID(), mobClass.classBuff.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public static MobClass getRandomClass() {
        List<MobClass> list;

        int random = GameRandom.globalRandom.getIntBetween(0, 999);
        if (random < 5) {
            list = mythicClasses;
        } else if (random < 55) {
            list = rareClasses;
        } else if (random < 205) {
            list = uncommonClasses;
        } else {
            list = basicClasses;
        }

        return getRandomClass(list);
    }

    public static MobClass getRandomClass(List<MobClass> list) {
        return list.get(GameRandom.globalRandom.getIntBetween(0, list.size() - 1));
    }

    public final int id;
    public final String stringID;
    public final String color;
    public final float healthPerLevel;
    public final float expMod;
    public final Class<? extends MobClassBuff> classBuff;

    public float baseHealthMod;

    public MobClass(String stringID, String color, float healthPerLevel, float expMod, Class<? extends MobClassBuff> classBuff) {
        this.id = allClasses.size();
        this.stringID = stringID;
        this.color = color;
        this.healthPerLevel = healthPerLevel;
        this.expMod = expMod;
        this.classBuff = classBuff;

        this.baseHealthMod = healthPerLevel * 4;
    }

    public MobClass(String stringID, String color, float healthPerLevel, Class<? extends MobClassBuff> classBuff) {
        this(stringID, color, healthPerLevel, 1, classBuff);
    }

    public MobClass setBaseHealthMod(float baseHealthMod) {
        this.baseHealthMod = baseHealthMod;
        return this;
    }

    public String getName() {
        return Localization.translate("mobclass", stringID);
    }

    public void giveBuff(Mob mob, int classLevel) {
        ActiveBuff ab = new ActiveBuff(buffStringID(), mob, 3600F, null);
        MobClassBuff.setMobLevel(ab, classLevel);
        mob.buffManager.addBuff(ab, true);
    }

    public void setMaxHealth(Mob mob, int classLevel) {
        if (healthPerLevel > 0) {
            mob.setMaxHealth((int) (mob.getMaxHealth() * (1F + healthPerLevel * classLevel)));
            mob.setHealthHidden(mob.getMaxHealth());
        }
    }

    public String buffStringID() {
        return stringID + "mobclassbuff";
    }

    public boolean is(String stringID) {
        return Objects.equals(this.stringID, stringID);
    }
}
