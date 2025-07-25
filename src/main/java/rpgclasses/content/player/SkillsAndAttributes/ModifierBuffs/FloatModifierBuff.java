package rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs;

import necesse.engine.localization.Localization;
import necesse.engine.modifiers.Modifier;

public class FloatModifierBuff extends ModifierBuff<Float> {
    public FloatModifierBuff(Modifier<Float> modifier, float value) {
        super(modifier, value);
    }

    public String getTooltip() {
        return Localization.translate("buffmodifiers", getLocalizationString(), "mod", (value > 0 ? "+" : "") + value);
    }

    @Override
    public Float scaledValue(int attributeLevel) {
        return attributeLevel * value;
    }
}
