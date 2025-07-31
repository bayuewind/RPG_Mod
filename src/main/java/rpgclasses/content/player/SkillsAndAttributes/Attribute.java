package rpgclasses.content.player.SkillsAndAttributes;

import aphorea.registry.AphModifiers;
import necesse.engine.localization.Localization;
import necesse.engine.modifiers.Modifier;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.BuffModifiers;
import necesse.gfx.gameTexture.GameTexture;
import rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs.FloatPercentModifierBuff;
import rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs.IntModifierBuff;
import rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs.ModifierBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.BasicPassive;
import rpgclasses.data.PlayerData;
import rpgclasses.registry.RPGModifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Attribute extends BasicPassive {
    public static Map<String, Attribute> attributes = new HashMap<>();
    public static List<Attribute> attributesList = new ArrayList<>();

    public static void registerCore() {
        registerAttribute(new Attribute(
                "endurance", "#4682B4",
                RPGModifiers.ENDURANCE_ATTR_FLAT,
                new IntModifierBuff(BuffModifiers.MAX_HEALTH_FLAT, 2),
                new IntModifierBuff(BuffModifiers.ARMOR_FLAT, 1)
        ));
        registerAttribute(new Attribute(
                "speed", "#FFD60A",
                RPGModifiers.SPEED_ATTR_FLAT,
                new FloatPercentModifierBuff(BuffModifiers.SPEED, 0.005F),
                new FloatPercentModifierBuff(RPGModifiers.DODGE_CHANCE, 0.002F)
        ));
        registerAttribute(new Attribute(
                "strength", "#D94F30",
                RPGModifiers.STRENGTH_ATTR_FLAT,
                new FloatPercentModifierBuff(BuffModifiers.MELEE_DAMAGE, 0.01F),
                new FloatPercentModifierBuff(BuffModifiers.RANGED_DAMAGE, 0.01F)
        ));
        registerAttribute(new Attribute(
                "intelligence", "#9D4EDD",
                RPGModifiers.INTELLIGENCE_ATTR_FLAT,
                new FloatPercentModifierBuff(BuffModifiers.MAGIC_DAMAGE, 0.01F),
                new FloatPercentModifierBuff(BuffModifiers.SUMMON_DAMAGE, 0.01F)
        ));
        registerAttribute(new Attribute(
                "grace", "#2ECC71",
                RPGModifiers.GRACE_ATTR_FLAT,
                new FloatPercentModifierBuff(AphModifiers.MAGIC_HEALING, 0.01F),
                new FloatPercentModifierBuff(BuffModifiers.LIFE_ESSENCE_GAIN, 0.01F)
        ));
    }

    public static void registerAttribute(Attribute attribute) {
        attributes.put(attribute.stringID, attribute);
        attributesList.add(attribute);
    }

    public static Attribute getRandom() {
        return GameRandom.globalRandom.getOneOf(attributesList);
    }

    public Modifier<Integer> ownModifier;

    public Attribute(String stringID, String color, Modifier<Integer> ownModifier, ModifierBuff<?>... modifierBuffs) {
        super(stringID, color, 0, 0, modifierBuffs);
        this.id = attributes.size();
        this.ownModifier = ownModifier;
    }

    public void initResources() {
        texture = GameTexture.fromFile("ui/attributes/" + stringID);
    }

    public List<String> getToolTipsText() {
        List<String> tooltips = new ArrayList<>();
        tooltips.add("§" + color + Localization.translate("attributes", stringID));
        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "eachlevel"));
        tooltips.add(" ");
        for (ModifierBuff<?> attributeModifier : attributeModifiers) {
            tooltips.add(attributeModifier.getTooltip());
        }
        return tooltips;
    }

    public int getLevel(PlayerData playerData, PlayerMob player) {
        return playerData.getAttributeLevel(id) + player.buffManager.getModifier(ownModifier);
    }

}
