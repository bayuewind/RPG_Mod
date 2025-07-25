package rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs;

import necesse.engine.localization.Localization;
import necesse.engine.modifiers.Modifier;

public class IntModifierBuff extends ModifierBuff<Integer> {
    public IntModifierBuff(Modifier<Integer> modifier, int value) {
        super(modifier, value);
    }

    public String getTooltip() {
        return Localization.translate("buffmodifiers", getLocalizationString(), "mod", (value > 0 ? "+" : "") + value);
    }

    @Override
    public Integer scaledValue(int attributeLevel) {
        return attributeLevel * value;
    }
}
