package rpgclasses.content.player.PlayerClasses.Cleric.Passives;

import aphorea.utils.area.AphArea;
import aphorea.utils.area.AphAreaList;
import aphorea.utils.magichealing.AphMagicHealingFunctions;
import necesse.entity.mobs.GameDamage;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.buffs.ActiveBuff;
import necesse.inventory.InventoryItem;
import necesse.inventory.item.toolItem.ToolItem;
import org.jetbrains.annotations.Nullable;
import rpgclasses.buffs.Skill.PrincipalPassiveBuff;
import rpgclasses.content.player.SkillsAndAttributes.Passives.SimpleBuffPassive;
import rpgclasses.registry.RPGDamageType;

import java.awt.*;

public class RadiantExpansion extends SimpleBuffPassive {
    public RadiantExpansion(int levelMax, int requiredClassLevel) {
        super("radiantexpansion", "#ffff00", levelMax, requiredClassLevel);
    }

    @Override
    public PrincipalPassiveBuff getBuff() {
        return new SanctifiedMindBuff();
    }

    public static class SanctifiedMindBuff extends PrincipalPassiveBuff implements AphMagicHealingFunctions {
        @Override
        public void onMagicalHealing(Mob healer, Mob target, int healing, int realHealing, @Nullable ToolItem toolItem, @Nullable InventoryItem item) {
            ActiveBuff activeBuff = healer.buffManager.getBuff(this);
            int level = getLevel(activeBuff);
            int damage = (int) (realHealing * 0.05F * level);

            if (damage > 0) {
                AphAreaList areaList = new AphAreaList(
                        new AphArea(150, new Color(255, 255, 0))
                                .setDamageArea(new GameDamage(RPGDamageType.HOLY, damage))
                );
                areaList.execute(healer, target.x, target.y, false);
            }
        }
    }


    @Override
    public String[] getExtraTooltips() {
        return new String[]{"holydamage"};
    }
}
