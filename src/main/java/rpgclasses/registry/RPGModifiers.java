package rpgclasses.registry;

import necesse.engine.modifiers.Modifier;
import necesse.engine.modifiers.ModifierLimiter;
import necesse.entity.mobs.buffs.BuffModifiers;

public class RPGModifiers {
    public static final Modifier<Integer> ENDURANCE_ATTR_FLAT;
    public static final Modifier<Integer> SPEED_ATTR_FLAT;
    public static final Modifier<Integer> STRENGTH_ATTR_FLAT;
    public static final Modifier<Integer> INTELLIGENCE_ATTR_FLAT;
    public static final Modifier<Integer> GRACE_ATTR_FLAT;

    public static final Modifier<Float> DODGE_CHANCE;
    public static final Modifier<Float> RANGED_WEAPONS_ZOOM;
    public static final Modifier<Float> RANGED_WEAPONS_RANGE;
    public static final Modifier<Float> MOB_DETECTION_RANGE;
    public static final Modifier<Float> FOCUS_DAMAGE;
    public static final Modifier<Float> FOCUS_CHANCE;

    static {
        ENDURANCE_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "enduranceattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("enduranceattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("enduranceattrflat"));
        SPEED_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "speedattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("speedattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("speedattrflat"));
        STRENGTH_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "strengthattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("strengthattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("strengthattrflat"));
        INTELLIGENCE_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "intelligenceattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("intelligenceattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("intelligenceattrflat"));
        GRACE_ATTR_FLAT = new Modifier<>(BuffModifiers.LIST, "graceattrflat", 0, 0, Modifier.INT_ADD_APPEND, Modifier.NORMAL_FLAT_INT_PARSER("graceattrflat"), ModifierLimiter.NORMAL_FLAT_INT_LIMITER("graceattrflat"));

        DODGE_CHANCE = new Modifier<>(BuffModifiers.LIST, "dodgechance", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> v < 0.0F ? 0.0F : (v > 0.8F ? 0.8F : v), Modifier.NORMAL_PERC_PARSER("dodgechance"), ModifierLimiter.NORMAL_PERC_LIMITER("dodgechance"));
        RANGED_WEAPONS_ZOOM = new Modifier<>(BuffModifiers.LIST, "rangedweaponszoom", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, Modifier.NORMAL_FLAT_FLOAT_PARSER("rangedweaponszoom"), ModifierLimiter.NORMAL_FLAT_FLOAT_LIMITER("rangedweaponszoom"));
        RANGED_WEAPONS_RANGE = new Modifier<>(BuffModifiers.LIST, "rangedweaponsrange", 1.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.NORMAL_PERC_PARSER("rangedweaponsrange"), ModifierLimiter.NORMAL_PERC_LIMITER("rangedweaponsrange"));
        MOB_DETECTION_RANGE = new Modifier<>(BuffModifiers.LIST, "mobdetectionrange", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, Modifier.NORMAL_FLAT_FLOAT_PARSER("mobdetectionrange"), ModifierLimiter.NORMAL_FLAT_FLOAT_LIMITER("mobdetectionrange"));
        FOCUS_DAMAGE = new Modifier<>(BuffModifiers.LIST, "focusdamage", 0.2F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.NORMAL_PERC_PARSER("focusdamage"), ModifierLimiter.NORMAL_PERC_LIMITER("focusdamage"));
        FOCUS_CHANCE = new Modifier<>(BuffModifiers.LIST, "focuschance", 0.0F, 0.0F, Modifier.FLOAT_ADD_APPEND, (v) -> Math.max(0.0F, v), Modifier.NORMAL_PERC_PARSER("focuschance"), ModifierLimiter.NORMAL_PERC_LIMITER("focuschance"));
    }

}
