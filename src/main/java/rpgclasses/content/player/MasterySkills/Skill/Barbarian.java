package rpgclasses.content.player.MasterySkills.Skill;

import necesse.engine.modifiers.ModifierValue;
import necesse.entity.mobs.buffs.BuffModifiers;
import rpgclasses.content.player.MasterySkills.SimpleMastery;

public class Barbarian extends SimpleMastery {

    public Barbarian(String stringID, String color) {
        super(stringID, color,
                new ModifierValue<>(BuffModifiers.MAX_HEALTH, 1F),
                new ModifierValue<>(BuffModifiers.COMBAT_HEALTH_REGEN, 2F),
                new ModifierValue<>(BuffModifiers.ARMOR, -1F).max(-1F)
        );
    }
}
