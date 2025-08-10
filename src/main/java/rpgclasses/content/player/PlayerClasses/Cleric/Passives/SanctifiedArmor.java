package rpgclasses.content.player.PlayerClasses.Cleric.Passives;

import aphorea.utils.magichealing.AphMagicHealingFunctions;
import necesse.entity.levelEvent.mobAbilityLevelEvent.MobManaChangeEvent;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;

public class SanctifiedArmor extends SimpleBuffPassive {
    public SanctifiedArmor(int levelMax, int requiredClassLevel) {
        super("sanctifiedarmor", "#9999cc", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new SanctifiedArmorBuff();
    }

    public static class SanctifiedArmorBuff extends PrincipalPassiveBuff implements AphMagicHealingFunctions {
        @Override
        public void onMagicalHealing(Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
            if (healer.isServer()) {
                ActiveBuff activeBuff = healer.buffManager.getBuff(this);
                int level = getLevel(activeBuff);
                target.getLevel().entityManager.addLevelEvent(new MobManaChangeEvent(target, realHealing * 0.02F * level));
            }
        }
    }
}
