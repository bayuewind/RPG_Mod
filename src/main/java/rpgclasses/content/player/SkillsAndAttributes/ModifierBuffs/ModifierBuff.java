package rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs;

import necesse.engine.modifiers.Modifier;
import necesse.entity.mobs.buffs.ActiveBuff;

import java.util.Objects;

abstract public class ModifierBuff<T> {
    public final Modifier<T> modifier;
    public final T value;

    public ModifierBuff(Modifier<T> modifier, T value) {
        this.modifier = modifier;
        this.value = value;
    }

    public void applyBuff(ActiveBuff activeBuff, int attributeLevel) {
        activeBuff.addModifier(modifier, scaledValue(attributeLevel));
    }

    public String getLocalizationString() {
        String stringID = modifier.stringID;
        if (stringID.startsWith("rangedam") || (stringID.startsWith("range") && !stringID.startsWith("ranged"))) {
            return stringID.replace("range", "ranged");
        } else if (Objects.equals(stringID, "attackmovementmod")) {
            return "attackmovement";
        } else if (Objects.equals(stringID, "incomingdamagemod")) {
            return "incdamage";
        } else if (Objects.equals(stringID, "staminacapacity")) {
            return "staminacap";
        }
        return stringID;
    }

    abstract public String getTooltip();

    abstract public T scaledValue(int attributeLevel);
}
