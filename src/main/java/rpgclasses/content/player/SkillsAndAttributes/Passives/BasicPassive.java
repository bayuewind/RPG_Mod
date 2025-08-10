package rpgclasses.content.player.SkillsAndAttributes.Passives;

import necesse.engine.localization.Localization;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.mobs.buffs.ActiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.ModifierBuffs.ModifierBuff;
import rpgclasses.data.PlayerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicPassive extends Passive {
    public List<ModifierBuff<?>> attributeModifiers = new ArrayList<>();
    public String[] extraTooltips;

    public BasicPassive(String stringID, String color, int levelMax, int requiredClassLevel, String[] extraTooltips, ModifierBuff<?>... modifierBuffs) {
        super(stringID, color, levelMax, requiredClassLevel);
        Collections.addAll(attributeModifiers, modifierBuffs);
        this.extraTooltips = extraTooltips;
    }

    public BasicPassive(String stringID, String color, int levelMax, int requiredClassLevel, ModifierBuff<?>... modifierBuffs) {
        this(stringID, color, levelMax, requiredClassLevel, new String[0], modifierBuffs);
    }

    public List<String> getToolTipsText() {
        List<String> tooltips = new ArrayList<>();
        tooltips.add("§" + color + Localization.translate("passives", stringID));
        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "eachlevel"));
        tooltips.add(" ");
        for (ModifierBuff<?> attributeModifier : attributeModifiers) {
            tooltips.add(attributeModifier.getTooltip());
        }
        if (requiredClassLevel > 1) {
            tooltips.add(" ");
            tooltips.add(Localization.translate("ui", "requiredclasslevel", "level", requiredClassLevel));
        }
        tooltips.add(" ");
        tooltips.add(Localization.translate("ui", "maxlevel", "level", levelMax));

        return tooltips;
    }

    public void applyBuff(ActiveBuff activeBuff, int level) {
        for (ModifierBuff<?> attributeModifier : attributeModifiers) {
            attributeModifier.applyBuff(activeBuff, level);
        }
    }

    @Override
    public boolean isBasic() {
        return true;
    }

    @Override
    public void registerSkillBuffs() {
    }

    @Override
    public void givePassiveBuff(PlayerMob player, PlayerData playerData, int level) {
    }

    @Override
    public void removePassiveBuffs(PlayerMob player) {
    }

    @Override
    public boolean containsComplexTooltips() {
        return false;
    }

    @Override
    public String[] getExtraTooltips() {
        return extraTooltips;
    }
}
