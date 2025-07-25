package rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs;

import necesse.engine.localization.Localization;
import necesse.engine.modifiers.Modifier;

public class FloatPercentModifierBuff extends ModifierBuff<Float> {
    public final boolean add;

    public FloatPercentModifierBuff(Modifier<Float> modifier, float value, boolean add) {
        super(modifier, value);
        this.add = add;
    }

    public FloatPercentModifierBuff(Modifier<Float> modifier, float value) {
        this(modifier, value, true);
    }

    public String getTooltip() {
        return Localization.translate("buffmodifiers", getLocalizationString(), "mod", (add ? "+" : (value > 0 ? "-" : "")) + (value * 100));
    }

    @Override
    public Float scaledValue(int attributeLevel) {
        return add ? attributeLevel * value : 1F - (attributeLevel * value);
    }
}
